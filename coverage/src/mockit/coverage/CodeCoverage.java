/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
