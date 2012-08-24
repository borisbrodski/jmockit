/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.lang.instrument.*;
import java.security.*;

import mockit.coverage.data.*;
import mockit.coverage.modification.*;
import mockit.coverage.standalone.*;

public final class CodeCoverage implements ClassFileTransformer
{
   private static CodeCoverage instance;

   private final ClassModification classModification;
   private final OutputFileGenerator outputGenerator;

   public static void main(String[] args)
   {
      OutputFileGenerator generator = createOutputFileGenerator();
      generator.generateAggregateReportFromInputFiles(args);
   }

   private static OutputFileGenerator createOutputFileGenerator()
   {
      OutputFileGenerator generator = new OutputFileGenerator();
      CoverageData.instance().setWithCallPoints(generator.isWithCallPoints());
      return generator;
   }

   @SuppressWarnings("UnusedDeclaration")
   public CodeCoverage() { this(false); }

   private CodeCoverage(final boolean standaloneMode)
   {
      classModification = new ClassModification();
      outputGenerator = createOutputFileGenerator();

      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run()
         {
            Startup.instrumentation().removeTransformer(CodeCoverage.this);

            if (!standaloneMode) {
               if (outputGenerator.isOutputToBeGenerated()) {
                  outputGenerator.generate();
               }

               new CoverageCheck().verifyThresholds();
            }
         }
      });
   }

   public static CodeCoverage createInStandaloneMode()
   {
      instance = new CodeCoverage(true);
      return instance;
   }

   public static void resetConfiguration()
   {
      Startup.instrumentation().removeTransformer(instance);
      CoverageData.instance().clear();
      Startup.instrumentation().addTransformer(createInStandaloneMode());
   }

   public static void generateOutput(boolean resetState)
   {
      instance.outputGenerator.generate();

      if (resetState) {
         CoverageData.instance().reset();
      }
   }

   public byte[] transform(
      ClassLoader loader, String internalClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] originalClassfile)
   {
      if (loader == null || classBeingRedefined != null || protectionDomain == null) {
         return null;
      }

      String className = internalClassName.replace('/', '.');

      return classModification.modifyClass(className, protectionDomain, originalClassfile);
   }
}
