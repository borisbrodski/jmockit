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

import mockit.coverage.reporting.*;
import mockit.coverage.output.*;

final class OutputFileGenerator extends Thread
{
   private final String outputFormat;
   private final String outputDir;
   private final String[] sourceDirs;
   private String[] classPath;

   OutputFileGenerator(String outputFormat, String outputDir, String[] sourceDirs)
   {
      this.outputDir = outputDir;
      this.sourceDirs = sourceDirs;

      String format = outputFormat.length() > 0 ? outputFormat : outputFormatFromClasspath();

      if (format.length() == 0) {
         format = "html-nocp";
      }

      this.outputFormat = format;
   }

   private String outputFormatFromClasspath()
   {
      classPath = System.getProperty("java.class.path").split(File.pathSeparator);
      String result = "";

      if (availableInTheClasspath("xmlbasic")) {
         result = "xml-nocp";
      }
      else if (availableInTheClasspath("xmlfull")) {
         result = "xml";
      }

      if (availableInTheClasspath("htmlbasic")) {
         result += " html-nocp";
      }
      else if (availableInTheClasspath("htmlfull")) {
         result += " html";
      }

      if (availableInTheClasspath("merge")) {
         result += " merge";
      }

      return result;
   }

   private boolean availableInTheClasspath(String jarFileNameSuffix)
   {
      String desiredJarFile = "jmockit-coverage-" + jarFileNameSuffix + ".jar";

      for (String cpEntry : classPath) {
         if (cpEntry.endsWith(desiredJarFile)) {
            return true;
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
      createOutputDirIfSpecifiedButNotExists();

      CoverageData coverageData = CoverageData.instance();

      try {
         mergeCoverageDataFromPreviousTestRunIfRequestedAndAvailable(coverageData);
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

   private void mergeCoverageDataFromPreviousTestRunIfRequestedAndAvailable(CoverageData data)
      throws IOException, ClassNotFoundException
   {
      if (outputFormat.contains("merge")) {
         String parentDir = outputDir.length() == 0 ? null : outputDir;
         File dataFile = new File(parentDir, "coverage.ser");

         if (dataFile.exists()) {
            CoverageData previousData = CoverageData.readDataFromFile(dataFile);
            data.merge(previousData);
         }

         data.writeDataToFile(dataFile);
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
