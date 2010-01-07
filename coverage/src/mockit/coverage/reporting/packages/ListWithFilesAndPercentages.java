/*
 * JMockit Coverage
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.coverage.reporting.packages;

import java.io.*;
import java.util.*;

import mockit.coverage.*;

abstract class ListWithFilesAndPercentages
{
   protected final PrintWriter output;
   private final String baseIndent;
   final int[] totalItems = new int[3];
   final int[] coveredItems = new int[3];
   private boolean firstColumnWithDoubleSpan;

   protected ListWithFilesAndPercentages(PrintWriter output, String baseIndent)
   {
      this.output = output;
      this.baseIndent = baseIndent;
   }

   final void writeMetricsForEachFile(String packageName, List<String> fileNames)
   {
      if (fileNames.isEmpty()) {
         return;
      }

      Collections.sort(fileNames);
      Arrays.fill(totalItems, 0);
      Arrays.fill(coveredItems, 0);

      for (String fileName : fileNames) {
         printIndent();
         output.println("<tr>");

         writeMetricsForFile(packageName, fileName);

         printIndent();
         output.println("</tr>");
      }
   }

   final void printIndent()
   {
      output.write(baseIndent);
   }

   protected abstract void writeMetricsForFile(String packageName, String fileName);

   final void printCoveragePercentage(boolean firstColumn, int percentage)
   {
      if (percentage >= 0) {
         printIndent();
         output.write("  <td class='coverage' style='background-color:#");
         output.write(CoveragePercentage.percentageColor(percentage));
         output.write("'>");
         output.print(percentage);
         output.println("%</td>");
      }
      else if (firstColumn) {
         printIndent();
         output.println("  <td colspan='2' class='coverage nocode'>N/A</td>");
         firstColumnWithDoubleSpan = true;
      }
      else if (firstColumnWithDoubleSpan) {
         firstColumnWithDoubleSpan = false;
      }
      else {
         printIndent();
         output.println("  <td class='coverage nocode'>N/A</td>");
      }
   }
}