/*
 * JMockit Coverage
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mockit.coverage;

import java.io.*;
import java.lang.instrument.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;

import mockit.coverage.data.*;
import mockit.internal.startup.*;
import mockit.internal.state.TestRun;

import org.objectweb.asm2.*;

public final class CodeCoverage implements ClassFileTransformer, Runnable
{
   static final class VisitInterruptedException extends RuntimeException {}
   static final VisitInterruptedException CLASS_IGNORED = new VisitInterruptedException();
   private static final String[] NO_ARGS = new String[0];

   private final Set<String> modifiedClasses = new HashSet<String>();
   private final Pattern classNameRegex;

   public CodeCoverage(String argsSeparatedByColon)
   {
      String[] args = argsSeparatedByColon == null ? NO_ARGS : argsSeparatedByColon.split(":");

      classNameRegex = getClassNameRegex(args);

      redefineClassesAlreadyLoadedForCoverage();
      setUpOutputFileGenerators(args);
   }

   private Pattern getClassNameRegex(String[] args)
   {
      String regex = args.length == 0 ? "" : args[0];

      if (regex.length() == 0) {
         regex = System.getProperty("jmockit-coverage-classes", "");
      }

      return regex.length() == 0 ? null : Pattern.compile(regex);
   }

   private void setUpOutputFileGenerators(String[] args)
   {
      int argCount = args.length;
      String outputFormat = argCount <= 1 ? "" : args[1];
      String outputDir = argCount <= 2 ? "" : args[2];
      String[] sourceDirs = argCount <= 3 ? NO_ARGS : args[3].split(",");

      OutputFileGenerator outputFileGenerator =
         new OutputFileGenerator(this, outputFormat, outputDir, sourceDirs);

      if (outputFileGenerator.isOutputToBeGenerated()) {
         CoverageData.instance().setWithCallPoints(outputFileGenerator.isWithCallPoints());
         Runtime.getRuntime().addShutdownHook(outputFileGenerator);
      }
   }

   public void run()
   {
      Startup.instrumentation().removeTransformer(this);
   }

   public byte[] transform(
      ClassLoader loader, String internalClassName, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] originalClassfile)
   {
      if (classBeingRedefined != null) {
         return originalClassfile;
      }

      String className = internalClassName.replace('/', '.');
      boolean modifyClassForCoverage = isToBeConsideredForCoverage(className, protectionDomain);

      if (modifyClassForCoverage) {
         try {
            byte[] modifiedClassfile = modifyClassForCoverage(new ClassReader(originalClassfile));
            registerClassAsModifiedForCoverage(className, modifiedClassfile);
            return modifiedClassfile;
         }
         catch (VisitInterruptedException ignore) {
            // Ignore the class if the modification was refused for some reason.
         }
         catch (RuntimeException e) {
            e.printStackTrace();
         }
         catch (AssertionError e) {
            e.printStackTrace();
         }
      }

      return originalClassfile;
   }

   private boolean isToBeConsideredForCoverage(String className, ProtectionDomain protectionDomain)
   {
      if (modifiedClasses.contains(className)) {
         return false;
      }

      if (classNameRegex != null) {
         return classNameRegex.matcher(className).matches();
      }

      if (className.endsWith("Test") || className.startsWith("mockit.")) {
         return false;
      }

      int p = className.lastIndexOf('$');

      if (p > 0 && className.substring(0, p).endsWith("Test")) {
         return false;
      }

      CodeSource codeSource = protectionDomain.getCodeSource();

      if (codeSource == null) {
         return false;
      }

      String codeLocation = codeSource.getLocation().getPath();

      return !codeLocation.endsWith(".jar") && !codeLocation.endsWith("/test-classes/");
   }

   private void registerClassAsModifiedForCoverage(String className, byte[] modifiedClassfile)
   {
      modifiedClasses.add(className);
      TestRun.mockFixture().addFixedClass(className, modifiedClassfile);
   }

   private void redefineClassesAlreadyLoadedForCoverage()
   {
      Class<?>[] loadedClasses =
         Startup.instrumentation().getInitiatedClasses(CodeCoverage.class.getClassLoader());

      for (Class<?> loadedClass : loadedClasses) {
         if (
            !loadedClass.isAnnotation() && !loadedClass.isSynthetic() &&
            isToBeConsideredForCoverage(loadedClass.getName(), loadedClass.getProtectionDomain())
         ) {
            redefineClassForCoverage(loadedClass);
         }
      }
   }

   private void redefineClassForCoverage(Class<?> loadedClass)
   {
      String className = loadedClass.getName();
      byte[] modifiedClassfile = readAndRedefineClassForCoverage(loadedClass);

      if (modifiedClassfile != null) {
         registerClassAsModifiedForCoverage(className, modifiedClassfile);
      }
   }

   private byte[] readAndRedefineClassForCoverage(Class<?> loadedClass)
   {
      try {
         ClassReader cr = new ClassReader(loadedClass.getName());
         byte[] modifiedClassfile = modifyClassForCoverage(cr);

         ClassDefinition[] classDefs = { new ClassDefinition(loadedClass, modifiedClassfile) };
         Startup.instrumentation().redefineClasses(classDefs);

         return modifiedClassfile;
      }
      catch (IOException ignore) {
         // Ignore the class if the ".class" file can't be located.
      }
      catch (VisitInterruptedException ignore) {
         // Ignore the class if the modification was refused for some reason.
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
      catch (UnmodifiableClassException e) {
         throw new RuntimeException(e);
      }
      catch (RuntimeException e) {
         e.printStackTrace();
      }
      catch (AssertionError e) {
         e.printStackTrace();
      }

      return null;
   }

   private byte[] modifyClassForCoverage(ClassReader cr)
   {
      CoverageModifier modifier = new CoverageModifier(cr);
      cr.accept(modifier, false);
      return modifier.toByteArray();
   }
}
