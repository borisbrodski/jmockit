/*
 * JMockit Coverage
 * Copyright (c) 2007-2008 Rogério Liesenfeld
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

abstract class ListWithFilesAndPercentages
{
   protected final PrintWriter output;
   private final int baseIndentationLevel;

   protected ListWithFilesAndPercentages(PrintWriter output, int baseIndentationLevel)
   {
      this.output = output;
      this.baseIndentationLevel = baseIndentationLevel;
   }

   protected abstract String getHRefToFile(String filePath);
   protected abstract String getFileNameForDisplay(String filePath);
   protected abstract void writeInternalTableForChildren(String filePath);
   protected abstract int getCoveragePercentageForFile(String filePath);

   final int writeMetricForEachFile(List<String> filePaths)
   {
      if (filePaths.isEmpty()) {
         return 0;
      }

      Collections.sort(filePaths);

      int percentageSum = 0;

      for (String filePath : filePaths) {
         printIndent(2); output.println("<tr>");
         printIndent(3); output.print("<td class='file'>");

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

         writeInternalTableForChildren(filePath);

         int filePercentage = getCoveragePercentageForFile(filePath);
         percentageSum += filePercentage;
         printIndent(3); output.print("<td class='coverage' style='background-color:#");
         output.print(percentageColor(filePercentage));
         output.print("'>");
         output.print(filePercentage);
         output.println("%</td>");
         printIndent(2); output.println("</tr>");
      }

      return percentageSum / filePaths.size();
   }

   private String percentageColor(int percentage)
   {
      if (percentage == 0) {
         return "ff0000";
      }
      else if (percentage == 100) {
         return "00ff00";
      }
      else {
         int green = 0xFF * percentage / 100;
         int red = 0xFF - green;

         StringBuilder color = new StringBuilder(6);
         String hex = Integer.toHexString(red);

         if (hex.length() == 1) {
            color.append('0');
         }

         color.append(hex);

         hex = Integer.toHexString(green);

         if (hex.length() == 1) {
            color.append('0');
         }

         color.append(hex);
         color.append("00");

         return color.toString();
      }
   }

   final void printIndent(int level)
   {
      for (int i = 0; i < baseIndentationLevel + level; i++) {
         output.write("  ");
      }
   }
}