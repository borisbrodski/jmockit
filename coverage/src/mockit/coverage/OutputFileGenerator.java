/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.io.*;

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;

final class OutputFileGenerator extends Thread
{
   private static final String COVERAGE_PREFIX = "jmockit-coverage-";

   Runnable onRun;
   private final String outputFormat;
   private final String outputDir;
   private final String[] sourceDirs;
   private String[] classPath;

   OutputFileGenerator(String outputFormat, String outputDir, String[] srcDirs)
   {
      this.outputFormat = getOutputFormat(outputFormat);
      this.outputDir = outputDir.length() > 0 ? outputDir : getCoverageProperty("outputDir");

      if (srcDirs.length > 0) {
         sourceDirs = srcDirs;
      }
      else {
         String commaSeparatedDirs = System.getProperty(COVERAGE_PREFIX + "srcDirs");

         if (commaSeparatedDirs == null) {
            sourceDirs = srcDirs;
         }
         else if (commaSeparatedDirs.length() == 0) {
            sourceDirs = null;
         }
         else {
            sourceDirs = commaSeparatedDirs.split(",");
         }
      }
   }

   private String getCoverageProperty(String suffix)
   {
      return System.getProperty(COVERAGE_PREFIX + suffix, "");
   }

   private String getOutputFormat(String specifiedFormat)
   {
      if (specifiedFormat.length() > 0) {
         return specifiedFormat;
      }

      String format = getCoverageProperty("output");

      if (format.length() == 0) {
         format = outputFormatFromClasspath();
      }

      if (format.length() == 0) {
         format = "html-nocp";
      }

      return format;
   }

   private String outputFormatFromClasspath()
   {
      classPath = System.getProperty("java.class.path").split(File.pathSeparator);
      String result = "";

      if (isAvailableInTheClasspath("htmlbasic")) {
         result += " html-nocp";
      }
      else if (isAvailableInTheClasspath("htmlfull")) {
         result += " html";
      }

      if (isAvailableInTheClasspath("serial")) {
         result += " serial";
      }
      else if (isAvailableInTheClasspath("merge")) {
         result += " merge";
      }

      return result;
   }

   private boolean isAvailableInTheClasspath(String jarFileNameSuffix)
   {
      for (String cpEntry : classPath) {
         if (cpEntry.endsWith(".jar")) {
            int p = cpEntry.indexOf(COVERAGE_PREFIX);

            if (p >= 0 && cpEntry.substring(p).contains(jarFileNameSuffix)) {
               return true;
            }
         }
      }

      return false;
   }

   boolean isOutputToBeGenerated()
   {
      return outputFormat.length() > 0;
   }

   boolean isWithCallPoints()
   {
      return outputFormat.contains("html") && !outputFormat.contains("html-nocp");
   }

   @Override
   public void run()
   {
      onRun.run();
      createOutputDirIfSpecifiedButNotExists();

      CoverageData coverageData = CoverageData.instance();

      try {
         generateAccretionDataFileIfRequested(coverageData);
         generateHTMLReportIfRequested(coverageData);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   void generateAggregateReportFromInputFiles(String commaSeparatedPaths)
   {
      createOutputDirIfSpecifiedButNotExists();

      String[] inputPaths = commaSeparatedPaths.split(",");

      try {
         CoverageData coverageData = new DataFileMerging(inputPaths).merge();
         generateHTMLReportIfRequested(coverageData);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   private void createOutputDirIfSpecifiedButNotExists()
   {
      if (outputDir.length() > 0) {
         File outDir = new File(outputDir);

         if (!outDir.exists()) {
            boolean dirCreated = outDir.mkdir();
            assert dirCreated : "Failed to create specified output dir: " + outputDir;
         }
      }
   }

   private void generateAccretionDataFileIfRequested(CoverageData newData)
      throws IOException, ClassNotFoundException
   {
      if (outputFormat.contains("serial")) {
         new AccretionFile(outputDir, newData).generate();
      }
      else if (outputFormat.contains("merge")) {
         AccretionFile accretionFile = new AccretionFile(outputDir, newData);
         accretionFile.mergeDataFromExistingFileIfAny();
         accretionFile.generate();
      }
   }

   private void generateHTMLReportIfRequested(CoverageData coverageData) throws IOException
   {
      if (outputFormat.contains("html-nocp")) {
         new BasicCoverageReport(outputDir, sourceDirs, coverageData).generate();
      }
      else if (outputFormat.contains("html")) {
         new FullCoverageReport(outputDir, sourceDirs, coverageData).generate();
      }
   }
}
