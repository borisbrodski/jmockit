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

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.output.*;

final class OutputFileGenerator extends Thread
{
   private static final String COVERAGE_PREFIX = "jmockit-coverage-";

   private final Runnable onRun;
   private final String outputFormat;
   private final String outputDir;
   private final String[] sourceDirs;
   private String[] classPath;

   OutputFileGenerator(Runnable onRun, String outputFormat, String outputDir, String[] srcDirs)
   {
      this.onRun = onRun;
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

      if (isAvailableInTheClasspath("xmlbasic")) {
         result = "xml-nocp";
      }
      else if (isAvailableInTheClasspath("xmlfull")) {
         result = "xml";
      }

      if (isAvailableInTheClasspath("htmlbasic")) {
         result += " html-nocp";
      }
      else if (isAvailableInTheClasspath("htmlfull")) {
         result += " html";
      }

      if (isAvailableInTheClasspath("merge")) {
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
      return
         outputFormat.contains("xml") && !outputFormat.contains("xml-nocp") ||
         outputFormat.contains("html") && !outputFormat.contains("html-nocp");
   }

   @Override
   public void run()
   {
      onRun.run();
      createOutputDirIfSpecifiedButNotExists();

      CoverageData coverageData = CoverageData.instance();

      try {
         generateAccretionDataFileIfRequested(coverageData);
         generateXMLOutputFileIfRequested(coverageData);
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
      if (outputFormat.contains("merge")) {
         new AccretionFile(outputDir).generate(newData);
      }
   }

   private void generateXMLOutputFileIfRequested(CoverageData coverageData) throws IOException
   {
      if (outputFormat.contains("xml-nocp")) {
         new BasicXmlWriter(coverageData).writeToXmlFile(outputDir);
      }
      else if (outputFormat.contains("xml")) {
         new FullXmlWriter(coverageData).writeToXmlFile(outputDir);
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
