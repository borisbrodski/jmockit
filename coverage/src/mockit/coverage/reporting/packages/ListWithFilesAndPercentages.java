/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.packages;

import java.io.*;
import java.util.*;

import mockit.coverage.*;

abstract class ListWithFilesAndPercentages
{
   private static final String[] METRIC_ITEM_NAMES = {"Line segments", "Paths", "Fields"};

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
         writeMetricsForFile(packageName, fileName);
      }
   }

   protected final void writeRowStart()
   {
      printIndent();
      output.println("<tr>");
   }

   protected final void writeRowClose()
   {
      printIndent();
      output.println("</tr>");
   }

   final void printIndent()
   {
      output.write(baseIndent);
   }

   protected abstract void writeMetricsForFile(String packageName, String fileName);

   final void printCoveragePercentage(Metrics metric, int covered, int total, int percentage)
   {
      if (total > 0) {
         printIndent();
         output.write("  <td class='coverage' style='background-color:#");
         output.write(CoveragePercentage.percentageColor(covered, total));
         output.write("' title='");
         output.write(METRIC_ITEM_NAMES[metric.ordinal()]);
         output.write(": ");
         output.print(covered);
         output.write('/');
         output.print(total);
         output.write("'>");
         writePercentageValue(covered, total, percentage);
         output.println("%</td>");
      }
      else if (metric == Metrics.LineCoverage) {
         printIndent();
         output.print("  <td class='coverage nocode'>N/A</td>");

         if (Metrics.PathCoverage.isActive()) {
            output.println("<td class='coverage nocode'>N/A</td>");
         }
         else {
            output.println();
         }

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

   private void writePercentageValue(int covered, int total, int percentage)
   {
      if (percentage < 100) {
         output.print(percentage);
      }
      else if (covered == total) {
         output.print("100");
      }
      else {
         output.print(">99");
      }
   }
}