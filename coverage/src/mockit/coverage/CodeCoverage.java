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

public final class CodeCoverage implements ClassFileTransformer, Runnable
{
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
   public CodeCoverage() { this(true); }

   public CodeCoverage(boolean generateOutputOnJVMShutdown)
   {
      classModification = new ClassModification();
      outputGenerator = createOutputFileGenerator();

      if (outputGenerator.isOutputToBeGenerated()) {
         outputGenerator.onRun = this;

         if (generateOutputOnJVMShutdown) {
            Runtime.getRuntime().addShutdownHook(outputGenerator);
         }
      }
   }

   public void generateOutput()
   {
      outputGenerator.generate();
   }

   public void run()
   {
      Startup.instrumentation().removeTransformer(this);
   }

   public byte[] transform(
      ClassLoader loader, String internalClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] originalClassfile)
   {
      if (classBeingRedefined != null || protectionDomain == null) {
         return null;
      }

      String className = internalClassName.replace('/', '.');

      return classModification.modifyClass(className, protectionDomain, originalClassfile);
   }
}
