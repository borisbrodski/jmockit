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
import mockit.coverage.data.*;

final class PackageCoverageReport extends ListWithFilesAndPercentages
{
   private final Map<String, FileCoverageData> filesToFileData;
   private final boolean withSourceFiles;
   private char[] fileNameWithSpaces;
   private String filePath;

   PackageCoverageReport(
      PrintWriter output, Map<String, FileCoverageData> filesToFileData, boolean withSourceFiles)
   {
      super(output, "          ");
      this.filesToFileData = filesToFileData;
      this.withSourceFiles = withSourceFiles;
   }

   void setMaxFileNameLength(int maxLength)
   {
      fileNameWithSpaces = new char[maxLength];
   }

   @Override
   protected void writeMetricsForFile(String packageName, String fileName)
   {
      filePath = packageName.length() == 0 ? fileName : packageName + '/' + fileName;
      printIndent();
      output.write("  <td class='file'>");

      int fileNameLength = buildFileNameWithTrailingSpaces(fileName);
      FileCoverageData fileData = filesToFileData.get(filePath);

      if (fileData == null) {
         writeTableCellsWithFileNameAndUnknownCoverageMetrics();
      }
      else {
         writeTableCellWithFileName(fileNameLength);

         if (Metrics.LINE_COVERAGE) {
            writeLineCoveragePercentageForFile(fileData);
         }

         if (Metrics.PATH_COVERAGE) {
            writePathCoveragePercentageForFile(fileData);
         }

         if (Metrics.DATA_COVERAGE) {
            writeDataCoveragePercentageForFile(fileData);
         }
      }
   }

   private int buildFileNameWithTrailingSpaces(String fileName)
   {
      int n = fileName.length();

      fileName.getChars(0, n, fileNameWithSpaces, 0);
      Arrays.fill(fileNameWithSpaces, n, fileNameWithSpaces.length, ' ');
      
      return n;
   }

   private void writeTableCellsWithFileNameAndUnknownCoverageMetrics()
   {
      output.write(fileNameWithSpaces);
      output.println("</td>");

      printIndent();
      output.print("  <td colspan='");
      output.print(Metrics.amountActive());
      output.println("' class='coverage unknown'>?</td>");
   }

   private void writeTableCellWithFileName(int fileNameLen)
   {
      if (withSourceFiles) {
         output.write("<a target='_blank' href='");
         int p = filePath.lastIndexOf('.');
         output.write(filePath.substring(0, p));
         output.write(".html'>");
         output.write(fileNameWithSpaces, 0, fileNameLen);
         output.write("</a>");
         output.write(fileNameWithSpaces, fileNameLen, fileNameWithSpaces.length - fileNameLen);
      }
      else {
         output.write(fileNameWithSpaces);
      }

      output.println("</td>");
   }

   private void writeLineCoveragePercentageForFile(FileCoverageData fileData)
   {
      int percentage = fileData.getCodeCoveragePercentage();
      int covered = fileData.getCoveredSegments();
      int total = fileData.getTotalSegments();

      writeCodeCoverageMetricForFile(0, covered, total, percentage);
   }

   private void writeCodeCoverageMetricForFile(int metric, int covered, int total, int percentage)
   {
      coveredItems[metric] += covered;
      totalItems[metric] += total;

      printCoveragePercentage(metric, covered, total, percentage);
   }

   private void writePathCoveragePercentageForFile(FileCoverageData fileData)
   {
      int percentage = fileData.getPathCoveragePercentage();
      int covered = fileData.getCoveredPaths();
      int total = fileData.getTotalPaths();

      writeCodeCoverageMetricForFile(1, covered, total, percentage);
   }

   private void writeDataCoveragePercentageForFile(FileCoverageData fileData)
   {
      int percentage = fileData.dataCoverageInfo.getCoveragePercentage();
      int covered = fileData.dataCoverageInfo.getCoveredItems();
      int total = fileData.dataCoverageInfo.getTotalItems();

      writeCodeCoverageMetricForFile(2, covered, total, percentage);
   }
}