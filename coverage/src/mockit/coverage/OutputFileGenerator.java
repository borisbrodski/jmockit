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

   OutputFileGenerator(String outputFormat, String outputDir, String[] sourceDirs)
   {
      this.outputFormat = outputFormat.length() > 0 ? outputFormat : outputFormatFromClasspath();
      this.outputDir = outputDir;
      this.sourceDirs = sourceDirs;
   }

   private String outputFormatFromClasspath()
   {
      String result = "";

      if (availableInTheClasspath("mockit.coverage.output.BasicXmlWriter")) {
         result = "xml-nocp";
      }
      else if (availableInTheClasspath("mockit.coverage.output.FullXmlWriter")) {
         result = "xml";
      }
      
      if (availableInTheClasspath("mockit.coverage.reporting.BasicCoverageReport")) {
         result += "html-nocp";
      }
      else if (availableInTheClasspath("mockit.coverage.reporting.FullCoverageReport")) {
         result += "html";
      }

      return result;
   }

   private boolean availableInTheClasspath(String outputGeneratorClassName)
   {
      try {
         Class.forName(outputGeneratorClassName);
         return true;
      }
      catch (ClassNotFoundException ignore) {
         return false;
      }
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
      CoverageData coverageData = CoverageData.instance();

      try {
         if (outputFormat.contains("xml-nocp")) {
            new BasicXmlWriter(coverageData).writeToXmlFile(outputDir);
         }
         else if (outputFormat.contains("xml")) {
            new FullXmlWriter(coverageData).writeToXmlFile(outputDir);
         }

         if (outputFormat.contains("html-nocp")) {
            new BasicCoverageReport(outputDir, sourceDirs, coverageData).generate();
         }
         else if (outputFormat.contains("html")) {
            new FullCoverageReport(outputDir, sourceDirs, coverageData).generate();
         }
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
