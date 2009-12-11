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
   private boolean firstColumnWithDoubleSpan;

   protected ListWithFilesAndPercentages(OutputFile output, String baseIndentation)
   {
      this.output = output;
      this.baseIndentation = baseIndentation;
   }

   protected abstract void writeMetricsForFile(String packageName, String fileName);
   protected abstract String getHRefToFile(String filePath);
   protected abstract String getFileNameForDisplay(String filePath);

   protected abstract int getTotalSegments(String filePath);
   protected abstract int getCoveredSegments(String filePath);
   protected abstract int getCodeCoveragePercentageForFile(String filePath);

   protected abstract int getTotalPaths(String filePath);
   protected abstract int getCoveredPaths(String filePath);
   protected abstract int getPathCoveragePercentageForFile(String filePath);

   final void writeMetricsForEachFile(String packageName, List<String> fileNames)
   {
      if (fileNames.isEmpty()) {
         return;
      }

      Collections.sort(fileNames);

      totalSegments = 0;
      coveredSegments = 0;
      totalPaths = 0;
      coveredPaths = 0;

      for (String fileName : fileNames) {
         printIndent();
         output.println("<tr>");

         writeMetricsForFile(packageName, fileName);

         printIndent();
         output.println("</tr>");
      }
   }

   final void writeTableCellWithFileName(String filePath)
   {
      printIndent();
      output.write("  <td class='file'>");

      String href = getHRefToFile(filePath);

      if (href != null) {
         output.write("<a href='");
         output.write(href);
         output.write("'>");
      }

      output.write(getFileNameForDisplay(filePath));

      if (href != null) {
         output.write("</a>");
      }

      output.println("</td>");
   }

   final void writeCodeCoveragePercentageForFile(String filePath)
   {
      int fileCodePercentage = getCodeCoveragePercentageForFile(filePath);

      totalSegments += getTotalSegments(filePath);
      coveredSegments += getCoveredSegments(filePath);

      printIndentOneLevelDeeper();
      printCoveragePercentage(true, fileCodePercentage);
   }

   final void writePathCoveragePercentageForFile(String filePath)
   {
      int filePathPercentage = getPathCoveragePercentageForFile(filePath);

      totalPaths += getTotalPaths(filePath);
      coveredPaths += getCoveredPaths(filePath);

      printIndentOneLevelDeeper();
      printCoveragePercentage(false, filePathPercentage);
   }

   final void printCoveragePercentage(boolean firstColumn, int percentage)
   {
      if (percentage >= 0) {
         output.write("<td class='coverage' style='background-color:#");
         output.write(CoveragePercentage.percentageColor(percentage));
         output.write("'>");
         output.print(percentage);
         output.println("%</td>");
      }
      else if (firstColumn) {
         output.println("<td colspan='2' class='coverage nocode'>N/A</td>");
         firstColumnWithDoubleSpan = true;
      }
      else if (firstColumnWithDoubleSpan) {
         firstColumnWithDoubleSpan = false;
      }
      else {
         output.println("<td class='coverage nocode'>N/A</td>");
      }
   }

   final void printIndent()
   {
      output.write(baseIndentation);
   }

   private void printIndentOneLevelDeeper()
   {
      printIndent();
      output.write("  ");
   }
}