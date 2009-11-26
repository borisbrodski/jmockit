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
package mockit.coverage.reporting;

import java.util.*;

import mockit.coverage.*;

abstract class ListWithFilesAndPercentages
{
   protected final OutputFile output;
   private final String baseIndentation;
   int totalSegments;
   int coveredSegments;
   int totalPaths;
   int coveredPaths;

   protected ListWithFilesAndPercentages(OutputFile output, String baseIndentation)
   {
      this.output = output;
      this.baseIndentation = baseIndentation;
   }

   protected abstract String getHRefToFile(String filePath);
   protected abstract String getFileNameForDisplay(String filePath);
   protected abstract void writeInternalTableForChildren(String filePath);

   protected abstract int getTotalSegments(String filePath);
   protected abstract int getCoveredSegments(String filePath);
   protected abstract int getCodeCoveragePercentageForFile(String filePath);

   protected abstract int getTotalPaths(String filePath);
   protected abstract int getCoveredPaths(String filePath);
   protected abstract int getPathCoveragePercentageForFile(String filePath);

   final void writeMetricForEachFile(List<String> filePaths)
   {
      if (filePaths.isEmpty()) {
         return;
      }

      Collections.sort(filePaths);

      totalSegments = 0;
      coveredSegments = 0;
      totalPaths = 0;
      coveredPaths = 0;

      for (String filePath : filePaths) {
         printIndent();
         output.println("<tr>");

         writeTableCellWithFileName(filePath);
         writeInternalTableForChildren(filePath);
         writeCodeCoveragePercentageForFile(filePath);
         writePathCoveragePercentageForFile(filePath);

         printIndent();
         output.println("</tr>");
      }
   }

   private void writeTableCellWithFileName(String filePath)
   {
      printIndentOneLevelDeeper();
      output.print("<td class='file'>");

      String href = getHRefToFile(filePath);

      if (href != null) {
         output.print("<a href='");
         output.print(href);
         output.print("'>");
      }

      output.print(getFileNameForDisplay(filePath));

      if (href != null) {
         output.print("</a>");
      }

      output.println("</td>");
   }

   private void writeCodeCoveragePercentageForFile(String filePath)
   {
      int fileCodePercentage = getCodeCoveragePercentageForFile(filePath);

      totalSegments += getTotalSegments(filePath);
      coveredSegments += getCoveredSegments(filePath);

      printIndentOneLevelDeeper();
      printCoveragePercentage(fileCodePercentage);
   }

   private void writePathCoveragePercentageForFile(String filePath)
   {
      int filePathPercentage = getPathCoveragePercentageForFile(filePath);

      totalPaths += getTotalPaths(filePath);
      coveredPaths += getCoveredPaths(filePath);

      printIndentOneLevelDeeper();
      printCoveragePercentage(filePathPercentage);
   }

   final void printCoveragePercentage(int percentage)
   {
      output.print("<td class='coverage' style='background-color:#");
      output.print(CoveragePercentage.percentageColor(percentage));
      output.print("'>");
      output.print(percentage);
      output.println("%</td>");
   }

   final void printIndent()
   {
      output.write(baseIndentation);
   }

   final void printIndentOneLevelDeeper()
   {
      printIndent();
      output.write("  ");
   }
}