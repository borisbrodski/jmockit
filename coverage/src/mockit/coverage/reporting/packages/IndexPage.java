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

import mockit.coverage.*;
import mockit.coverage.data.*;
import mockit.coverage.reporting.OutputFile;

public final class IndexPage extends ListWithFilesAndPercentages
{
   private final List<File> sourceDirs;
   private final Map<String, List<String>> packageToFiles;
   private final Map<String, Integer> packageToPackageCodePercentages;
   private final Map<String, Integer> packageToPackagePathPercentages;
   private final Map<String, Integer> packageToPackageDataPercentages;
   private final PackageCoverageReport packageReport;

   public IndexPage(
      File outputFile, List<File> sourceDirs,
      Map<String, List<String>> packageToFiles, Map<String, FileCoverageData> fileToFileData)
      throws IOException
   {
      super(new OutputFile(outputFile), "    ");
      this.sourceDirs = sourceDirs;
      this.packageToFiles = packageToFiles;
      packageToPackageCodePercentages = new HashMap<String, Integer>();
      packageToPackagePathPercentages = new HashMap<String, Integer>();
      packageToPackageDataPercentages = new HashMap<String, Integer>();
      packageReport = new PackageCoverageReport(output, fileToFileData, sourceDirs != null);
   }

   public void generate()
   {
      try {
         writeHeader();

         List<String> packages = new ArrayList<String>(packageToFiles.keySet());
         writeMetricsForEachFile(null, packages);

         writeLineWithCoverageTotals();
         writeFooter();
      }
      finally {
         output.close();
      }
   }

   private void writeHeader()
   {
      ((OutputFile) output).writeCommonHeader();

      output.println("  <h1>JMockit Coverage Report</h1>");
      output.println("  <table>");

      writeTableCaption();
      writeTableFirstRowWithColumnTitles();
   }

   private void writeTableCaption()
   {
      if (sourceDirs == null) {
         output.println("    <caption>All Packages and Files</caption>");
      }
      else {
         output.write("    <caption>All Packages and Files<div style='font-size: smaller'>");
         String commaSepDirs = sourceDirs.toString();
         output.write(commaSepDirs.substring(1, commaSepDirs.length() - 1));
         output.println("</div></caption>");
      }
   }

   private void writeTableFirstRowWithColumnTitles()
   {
      output.write("    <tr><th rowspan='2'>Packages: ");
      output.print(packageToFiles.keySet().size());
      output.write("</th><th rowspan='2'>Files (.java): ");

      int totalFileCount = computeTotalNumberOfSourceFiles();
      output.print(totalFileCount);
      output.println("</th><th colspan='3'>Coverage</th></tr>");

      output.println("    <tr><th>Line</th><th>Path</th><th>Data</th></tr>");
   }

   private int computeTotalNumberOfSourceFiles()
   {
      int totalFileCount = 0;

      for (List<String> files : packageToFiles.values()) {
         totalFileCount += files.size();
      }

      return totalFileCount;
   }

   private void writeLineWithCoverageTotals()
   {
      output.println("    <tr>");
      output.println("      <td colspan='2' class='total'>Total</td>");

      int totalLinePercentage = CoveragePercentage.calculate(coveredSegments, totalSegments);
      printCoveragePercentage(true, totalLinePercentage);

      int totalPathPercentage = CoveragePercentage.calculate(coveredPaths, totalPaths);
      printCoveragePercentage(false, totalPathPercentage);

      int totalDataPercentage = CoveragePercentage.calculate(coveredDataItems, totalDataItems);
      printCoveragePercentage(false, totalDataPercentage);

      output.println("    </tr>");
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.write("  <p>Generated on ");
      output.print(new Date());
      output.println("</p>");
      ((OutputFile) output).writeCommonFooter();
   }

   @Override
   protected String getHRefToFile(String filePath)
   {
      return null;
   }

   @Override
   protected String getFileNameForDisplay(String packageName)
   {
      return packageName.replace('/', '.');
   }

   @Override
   protected void writeMetricsForFile(String packageName, String fileName)
   {
      writeTableCellWithFileName(fileName);
      writeInternalTableForChildren(fileName);
      writeCodeCoveragePercentageForFile(fileName);
      writePathCoveragePercentageForFile(fileName);
      writeDataCoveragePercentageForFile(fileName);
   }

   private void writeInternalTableForChildren(String packageName)
   {
      printIndent();
      output.println("  <td>");
      printIndent();
      output.println("    <table>");

      List<String> packageFiles = packageToFiles.get(packageName);
      packageReport.writeMetricsForEachFile(packageName, packageFiles);

      recordCodeCoverageInformationForPackage(packageName);
      recordPathCoverageInformationForPackage(packageName);
      recordDataCoverageInformationForPackage(packageName);

      printIndent();
      output.println("    </table>");
      printIndent();
      output.println("  </td>");
   }

   private void recordCodeCoverageInformationForPackage(String packageName)
   {
      int packagePercentage =
         CoveragePercentage.calculate(packageReport.coveredSegments, packageReport.totalSegments);
      packageToPackageCodePercentages.put(packageName, packagePercentage);
      totalSegments += packageReport.totalSegments;
      coveredSegments += packageReport.coveredSegments;
   }

   private void recordPathCoverageInformationForPackage(String packageName)
   {
      int packagePercentage =
         CoveragePercentage.calculate(packageReport.coveredPaths, packageReport.totalPaths);
      packageToPackagePathPercentages.put(packageName, packagePercentage);
      totalPaths += packageReport.totalPaths;
      coveredPaths += packageReport.coveredPaths;
   }

   private void recordDataCoverageInformationForPackage(String packageName)
   {
      int packagePercentage =
         CoveragePercentage.calculate(packageReport.coveredDataItems, packageReport.totalDataItems);
      packageToPackageDataPercentages.put(packageName, packagePercentage);
      totalDataItems += packageReport.totalDataItems;
      coveredDataItems += packageReport.coveredDataItems;
   }

   private void writeCodeCoveragePercentageForFile(String packageName)
   {
      int filePercentage = packageToPackageCodePercentages.get(packageName);
      printCoveragePercentage(true, filePercentage);
   }

   private void writePathCoveragePercentageForFile(String packageName)
   {
      int filePercentage = packageToPackagePathPercentages.get(packageName);
      printCoveragePercentage(false, filePercentage);
   }

   private void writeDataCoveragePercentageForFile(String packageName)
   {
      int filePercentage = packageToPackageDataPercentages.get(packageName);
      printCoveragePercentage(false, filePercentage);
   }
}
