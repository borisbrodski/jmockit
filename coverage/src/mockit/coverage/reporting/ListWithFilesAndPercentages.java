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

import java.io.*;
import java.util.*;

import mockit.coverage.*;

abstract class ListWithFilesAndPercentages
{
   protected final PrintWriter output;
   private final int baseIndentationLevel;
   int totalSegments;
   int coveredSegments;
   int totalPaths;
   int coveredPaths;

   protected ListWithFilesAndPercentages(PrintWriter output, int baseIndentationLevel)
   {
      this.output = output;
      this.baseIndentationLevel = baseIndentationLevel;
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
         printIndent(2);
         output.println("<tr>");

         writeTableCellWithFileName(filePath);
         writeInternalTableForChildren(filePath);
         writeCodeCoveragePercentageForFile(filePath);
         writePathCoveragePercentageForFile(filePath);

         printIndent(2);
         output.println("</tr>");
      }
   }

   private void writeTableCellWithFileName(String filePath)
   {
      printIndent(3);
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

      printIndent(3);
      printCoveragePercentage(fileCodePercentage);
   }

   private void writePathCoveragePercentageForFile(String filePath)
   {
      int filePathPercentage = getPathCoveragePercentageForFile(filePath);

      totalPaths += getTotalPaths(filePath);
      coveredPaths += getCoveredPaths(filePath);

      printIndent(3);
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

   final void printIndent(int level)
   {
      for (int i = 0; i < baseIndentationLevel + level; i++) {
         output.write("  ");
      }
   }
}