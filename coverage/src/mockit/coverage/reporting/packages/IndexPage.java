/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   private final Map<String, int[]> packageToPackagePercentages;
   private final PackageCoverageReport packageReport;
   private String packageName;

   public IndexPage(
      File outputFile, List<File> sourceDirs,
      Map<String, List<String>> packageToFiles, Map<String, FileCoverageData> fileToFileData)
      throws IOException
   {
      super(new OutputFile(outputFile), "    ");
      this.sourceDirs = sourceDirs;
      this.packageToFiles = packageToFiles;
      packageToPackagePercentages = new HashMap<String, int[]>();
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
      int totalFileCount = computeTotalNumberOfSourceFilesAndMaximumFileNameLength();

      output.println("    <tr>");
      output.write("      <th style='cursor: pointer' onclick='showHideAllFiles(this)'>Packages: ");
      output.print(packageToFiles.keySet().size());
      output.println("</th>");
      output.write(
         "      <th onclick='location.reload()' style='cursor: pointer' " +
         "title='Click on the title for each metric to sort by size (total number of line " +
         "segments, paths, or fields).'>Files: ");
      output.print(totalFileCount);
      output.println("</th>");

      if (Metrics.LINE_COVERAGE) {
         output.println(
            "      <th onclick='sortTables(1)' style='cursor: pointer' title='" +
            "Measures how much of the executable production code was exercised by tests.\r\n" +
            "An executable line of code contains one or more executable segments.\r\n" +
            "The percentages are calculated as 100*NE/NS, where NS is the number of segments " +
            "and NE the number of executed segments.'>Line</th>");
      }

      if (Metrics.PATH_COVERAGE) {
         output.println(
            "      <th onclick='sortTables(2)' style='cursor: pointer' title='" +
            "Measures how many of the possible execution paths through method/constructor bodies " +
            "were actually executed by tests.\r\n" +
            "The percentages are calculated as 100*NPE/NP, where NP is the number of possible " +
            "paths and NPE the number of fully executed paths.'>Path</th>");
      }

      if (Metrics.DATA_COVERAGE) {
         output.println(
            "      <th onclick='sortTables(3)' style='cursor: pointer' title='" +
            "Measures how many of the instance and static non-final fields were fully exercised " +
            "by the test run.\r\n" +
            "To be fully exercised, a field must have the last value assigned to it read by at " +
            "least one test.\r\n" +
            "The percentages are calculated as 100*NFE/NF, where NF is the number of non-final " +
            "fields and NFE the number of fully exercised fields.'>Data</th>");
      }

      output.println("    </tr>");
   }

   private int computeTotalNumberOfSourceFilesAndMaximumFileNameLength()
   {
      int totalFileCount = 0;
      int maxFileNameLength = 0;

      for (List<String> files : packageToFiles.values()) {
         totalFileCount += files.size();

         for (String fileName : files) {
            int n = fileName.length();
            
            if (n > maxFileNameLength) {
               maxFileNameLength = n;
            }
         }
      }

      packageReport.setMaxFileNameLength(maxFileNameLength);
      return totalFileCount;
   }

   private void writeLineWithCoverageTotals()
   {
      output.println("    <tr class='total'>");
      output.println("      <td>Total</td><td>&nbsp;</td>");

      writeLineWithCoverageTotals(0);
      writeLineWithCoverageTotals(1);
      writeLineWithCoverageTotals(2);

      output.println("    </tr>");
   }

   private void writeLineWithCoverageTotals(int metric)
   {
      if (Metrics.withMetric(metric)) {
         int covered = coveredItems[metric];
         int total = totalItems[metric];
         int percentage = CoveragePercentage.calculate(covered, total);

         printCoveragePercentage(metric, covered, total, percentage);
      }
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
   protected void writeMetricsForFile(String unused, String packageName)
   {
      this.packageName = packageName;
      writeTableCellWithPackageName();
      writeInternalTableForSourceFiles();
      writeCoveragePercentageForFile(0);
      writeCoveragePercentageForFile(1);
      writeCoveragePercentageForFile(2);
   }

   private void writeTableCellWithPackageName()
   {
      printIndent();
      output.write(
         packageToFiles.get(packageName).size() > 1 ?
            "  <td class='package click' onclick='showHideFiles(this)'>" :
            "  <td class='package'>");
      output.write(packageName.replace('/', '.'));
      output.println("</td>");
   }

   private void writeInternalTableForSourceFiles()
   {
      printIndent();
      output.println("  <td>");
      printIndent();
      output.println("    <table width='100%'>");

      packageReport.writeMetricsForEachFile(packageName, packageToFiles.get(packageName));

      recordCoverageInformationForPackage(0);
      recordCoverageInformationForPackage(1);
      recordCoverageInformationForPackage(2);

      printIndent();
      output.println("    </table>");
      printIndent();
      output.println("  </td>");
   }

   private void recordCoverageInformationForPackage(int metric)
   {
      if (Metrics.withMetric(metric)) {
         int coveredInPackage = packageReport.coveredItems[metric];
         int totalInPackage = packageReport.totalItems[metric];
         int packagePercentage = CoveragePercentage.calculate(coveredInPackage, totalInPackage);

         setPackageCoveragePercentage(metric, packagePercentage);

         totalItems[metric] += totalInPackage;
         coveredItems[metric] += coveredInPackage;
      }
   }

   private void setPackageCoveragePercentage(int metric, int percentage)
   {
      int[] percentages = packageToPackagePercentages.get(packageName);

      if (percentages == null) {
         percentages = new int[3];
         packageToPackagePercentages.put(packageName, percentages);
      }

      percentages[metric] = percentage;
   }

   private void writeCoveragePercentageForFile(int metric)
   {
      if (Metrics.withMetric(metric)) {
         int coveredInPackage = packageReport.coveredItems[metric];
         int totalInPackage = packageReport.totalItems[metric];
         int filePercentage = packageToPackagePercentages.get(packageName)[metric];

         printCoveragePercentage(metric, coveredInPackage, totalInPackage, filePercentage);
      }
   }
}
