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

import java.lang.instrument.*;
import java.security.*;

import mockit.coverage.data.*;
import mockit.internal.startup.*;

public final class CodeCoverage implements ClassFileTransformer, Runnable
{
   private static final String[] NO_ARGS = new String[0];

   private final ClassModification classModification;

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

      classModification = new ClassModification(args);
      classModification.redefineClassesAlreadyLoadedForCoverage();

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

      return classModification.modifyClass(className, protectionDomain, originalClassfile);
   }
}
