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
package mockit.coverage.reporting.packages;

import java.io.*;
import java.util.*;

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
      filePath = packageName + '/' + fileName;
      printIndent();
      output.write("  <td class='file'>");

      int fileNameLength = buildFileNameWithoutExtensionButCompletedWithSpaces(fileName);

      if (filesToFileData.containsKey(filePath)) {
         writeTableCellWithFileName(fileNameLength);
         writeCodeCoveragePercentageForFile();
         writePathCoveragePercentageForFile();
         writeDataCoveragePercentageForFile();
      }
      else {
         writeTableCellsWithFileNameAndUnknownCoverageMetrics();
      }
   }

   private int buildFileNameWithoutExtensionButCompletedWithSpaces(String fileName)
   {
      int p = fileName.lastIndexOf('.');

      for (int i = 0; i < fileNameWithSpaces.length; i++) {
         fileNameWithSpaces[i] = i < p ? fileName.charAt(i) : ' ';
      }

      return p;
   }

   private void writeTableCellsWithFileNameAndUnknownCoverageMetrics()
   {
      output.write(fileNameWithSpaces);
      output.println("</td>");

      printIndent();
      output.println("  <td colspan='3' class='coverage unknown'>?</td>");
   }

   private void writeTableCellWithFileName(int fileNameLen)
   {
      if (withSourceFiles) {
         output.write("<a href='");
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

   private void writeCodeCoveragePercentageForFile()
   {
      FileCoverageData fileData = filesToFileData.get(filePath);
      int percentage = fileData.getCodeCoveragePercentage();

      coveredSegments += fileData.getCoveredSegments();
      totalSegments += fileData.getTotalSegments();

      printCoveragePercentage(true, percentage);
   }

   private void writePathCoveragePercentageForFile()
   {
      FileCoverageData fileData = filesToFileData.get(filePath);
      int percentage = fileData.getPathCoveragePercentage();

      coveredPaths += fileData.getCoveredPaths();
      totalPaths += fileData.getTotalPaths();

      printCoveragePercentage(false, percentage);
   }

   private void writeDataCoveragePercentageForFile()
   {
      FileCoverageData fileData = filesToFileData.get(filePath);

      coveredDataItems += fileData.dataCoverageInfo.getCoveredItems();
      totalDataItems += fileData.dataCoverageInfo.getTotalItems();

      int percentage = fileData.dataCoverageInfo.getCoveragePercentage();

      printCoveragePercentage(false, percentage);
   }
}