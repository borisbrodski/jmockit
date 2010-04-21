/*
 * JMockit Coverage
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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

import mockit.coverage.data.*;
import mockit.external.asm.*;
import mockit.internal.startup.*;
import mockit.internal.state.TestRun;

public final class CodeCoverage implements ClassFileTransformer, Runnable
{
   static final class VisitInterruptedException extends RuntimeException {}
   static final VisitInterruptedException CLASS_IGNORED = new VisitInterruptedException();
   private static final String[] NO_ARGS = new String[0];

   private final Set<String> modifiedClasses;
   private final ClassSelection classSelection;

   public static void main(String[] args)
   {
      OutputFileGenerator generator = createOutputFileGenerator(args);
      generator.generateAggregateReportFromInputFiles(args[0]);
   }

   private static OutputFileGenerator createOutputFileGenerator(String[] args)
   {
      int argCount = args.length;
      String outputFormat = argCount <= 1 ? "" : args[1];
      String outputDir = argCount <= 2 ? "" : args[2];
      String[] sourceDirs = argCount <= 3 ? NO_ARGS : args[3].split(",");

      return new OutputFileGenerator(outputFormat, outputDir, sourceDirs);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public CodeCoverage(String argsSeparatedByColon)
   {
      String[] args = argsSeparatedByColon == null ? NO_ARGS : argsSeparatedByColon.split(":");

      modifiedClasses = new HashSet<String>();
      classSelection = new ClassSelection(args);

      redefineClassesAlreadyLoadedForCoverage();
      setUpOutputFileGenerator(args);
   }

   private void setUpOutputFileGenerator(String[] args)
   {
      OutputFileGenerator generator = createOutputFileGenerator(args);

      if (generator.isOutputToBeGenerated()) {
         CoverageData.instance().setWithCallPoints(generator.isWithCallPoints());
         Runtime.getRuntime().addShutdownHook(generator);
         generator.onRun = this;
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
            byte[] modifiedClassfile = modifyClassForCoverage(className, originalClassfile);
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
      return
         !modifiedClasses.contains(className) &&
         classSelection.isSelected(className, protectionDomain);
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
      byte[] modifiedClassfile = readAndModifyClassForCoverage(loadedClass);

      if (modifiedClassfile != null) {
         redefineClassForCoverage(loadedClass, modifiedClassfile);
         registerClassAsModifiedForCoverage(className, modifiedClassfile);
      }
   }

   private byte[] readAndModifyClassForCoverage(Class<?> loadedClass)
   {
      try {
         return modifyClassForCoverage(loadedClass.getName(), null);
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

      return null;
   }

   private byte[] modifyClassForCoverage(String className, byte[] classBytecode)
   {
      byte[] modifiedBytecode = CoverageModifier.recoverModifiedByteCodeIfAvailable(className);

      if (modifiedBytecode != null) {
         return modifiedBytecode;
      }

      ClassReader cr;

      if (classBytecode == null) {
         try {
            cr = new ClassReader(className);
         }
         catch (IOException e) {
            // Ignore the class if the ".class" file can't be located.
            return null;
         }
      }
      else {
         cr = new ClassReader(classBytecode);
      }

      CoverageModifier modifier = new CoverageModifier(cr);
      cr.accept(modifier, false);
      return modifier.toByteArray();
   }

   private void redefineClassForCoverage(Class<?> loadedClass, byte[] modifiedClassfile)
   {
      ClassDefinition[] classDefs = { new ClassDefinition(loadedClass, modifiedClassfile) };

      try {
         Startup.instrumentation().redefineClasses(classDefs);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
      catch (UnmodifiableClassException e) {
         throw new RuntimeException(e);
      }
   }
}
