/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
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
   private final int totalFileCount;
   private String packageName;

   public IndexPage(
      File outputFile, List<File> sourceDirs, Collection<String> sourceFilesNotFound,
      Map<String, List<String>> packageToFiles, Map<String, FileCoverageData> fileToFileData)
      throws IOException
   {
      super(new OutputFile(outputFile), "    ");
      this.sourceDirs = sourceDirs;
      this.packageToFiles = packageToFiles;
      packageToPackagePercentages = new HashMap<String, int[]>();
      packageReport = new PackageCoverageReport(output, sourceFilesNotFound, fileToFileData, packageToFiles.values());
      totalFileCount = totalNumberOfSourceFilesWithCoverageData(fileToFileData.values());
   }

   private int totalNumberOfSourceFilesWithCoverageData(Collection<FileCoverageData> fileData)
   {
      return fileData.size() - Collections.frequency(fileData, null);
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
      ((OutputFile) output).writeCommonHeader(null);

      output.println("  <h1>JMockit Coverage Report</h1>");
      output.println("  <table id='packages'>");

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
         output.write(getCommaSeparatedListOfSourceDirs());
         output.println("</div></caption>");
      }
   }

   private String getCommaSeparatedListOfSourceDirs()
   {
      String prefixToRemove = ".." + File.separatorChar;
      String commaSepDirs = sourceDirs.toString().replace(prefixToRemove, "");
      return commaSepDirs.substring(1, commaSepDirs.length() - 1);
   }

   private void writeTableFirstRowWithColumnTitles()
   {
      output.println("    <tr>");
      output.write("      <th style='cursor: col-resize' onclick='showHideAllFiles(this)'>Packages: ");
      output.print(packageToFiles.keySet().size());
      output.println("</th>");
      output.write(
         "      <th onclick='location.reload()' style='cursor: n-resize' " +
         "title='Click on the title for each metric to sort by size (total number of line " +
         "segments, paths, or fields).'>Files: ");
      output.print(totalFileCount);
      output.println("</th>");

      int tableColumnForMetric = 1;

      if (Metrics.LineCoverage.isActive()) {
         output.println(
            "      <th onclick='sortTables(" + tableColumnForMetric + ")' style='cursor: n-resize' title='" +
            "Measures how much of the executable production code was exercised by tests.\r\n" +
            "An executable line of code contains one or more executable segments.\r\n" +
            "The percentages are calculated as 100*NE/NS, where NS is the number of segments " +
            "and NE the number of executed segments.'>Line</th>");
         tableColumnForMetric++;
      }

      if (Metrics.PathCoverage.isActive()) {
         output.println(
            "      <th onclick='sortTables(" + tableColumnForMetric + ")' style='cursor: n-resize' title='" +
            "Measures how many of the possible execution paths through method/constructor bodies " +
            "were actually executed by tests.\r\n" +
            "The percentages are calculated as 100*NPE/NP, where NP is the number of possible " +
            "paths and NPE the number of fully executed paths.'>Path</th>");
         tableColumnForMetric++;
      }

      if (Metrics.DataCoverage.isActive()) {
         output.println(
            "      <th onclick='sortTables(" + tableColumnForMetric + ")' style='cursor: n-resize' title='" +
            "Measures how many of the instance and static non-final fields were fully exercised " +
            "by the test run.\r\n" +
            "To be fully exercised, a field must have the last value assigned to it read by at " +
            "least one test.\r\n" +
            "The percentages are calculated as 100*NFE/NF, where NF is the number of non-final " +
            "fields and NFE the number of fully exercised fields.'>Data</th>");
      }

      output.println("    </tr>");
   }

   private void writeLineWithCoverageTotals()
   {
      output.println("    <tr class='total'>");
      output.println("      <td>Total</td><td>&nbsp;</td>");

      writeLineWithCoverageTotals(Metrics.LineCoverage);
      writeLineWithCoverageTotals(Metrics.PathCoverage);
      writeLineWithCoverageTotals(Metrics.DataCoverage);

      output.println("    </tr>");
   }

   private void writeLineWithCoverageTotals(Metrics metric)
   {
      if (metric.isActive()) {
         int covered = coveredItems[metric.ordinal()];
         int total = totalItems[metric.ordinal()];
         int percentage = CoveragePercentage.calculate(covered, total);

         printCoveragePercentage(metric, covered, total, percentage);
      }
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.println("  <p>");
      output.println("    <a href='http://code.google.com/p/jmockit'><img src='logo.png'></a>");
      output.write("    Generated on ");
      output.println(new Date());
      output.println("  </p>");
      ((OutputFile) output).writeCommonFooter();
   }

   @Override
   protected void writeMetricsForFile(String unused, String packageName)
   {
      this.packageName = packageName;
      writeRowStart();
      writeTableCellWithPackageName();
      writeInternalTableForSourceFiles();
      writeCoveragePercentageForPackage(Metrics.LineCoverage);
      writeCoveragePercentageForPackage(Metrics.PathCoverage);
      writeCoveragePercentageForPackage(Metrics.DataCoverage);
      writeRowClose();
   }

   private void writeTableCellWithPackageName()
   {
      printIndent();
      output.write(
         packageToFiles.get(packageName).size() > 1 ?
            "  <td class='package click' onclick='showHideFiles(this)'>" : "  <td class='package'>");
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

      recordCoverageInformationForPackage(Metrics.LineCoverage);
      recordCoverageInformationForPackage(Metrics.PathCoverage);
      recordCoverageInformationForPackage(Metrics.DataCoverage);

      printIndent();
      output.println("    </table>");
      printIndent();
      output.println("  </td>");
   }

   private void recordCoverageInformationForPackage(Metrics metric)
   {
      if (metric.isActive()) {
         int coveredInPackage = packageReport.coveredItems[metric.ordinal()];
         int totalInPackage = packageReport.totalItems[metric.ordinal()];
         int packagePercentage = CoveragePercentage.calculate(coveredInPackage, totalInPackage);

         setPackageCoveragePercentage(metric, packagePercentage);

         totalItems[metric.ordinal()] += totalInPackage;
         coveredItems[metric.ordinal()] += coveredInPackage;
      }
   }

   private void setPackageCoveragePercentage(Metrics metric, int percentage)
   {
      int[] percentages = packageToPackagePercentages.get(packageName);

      if (percentages == null) {
         percentages = new int[3];
         packageToPackagePercentages.put(packageName, percentages);
      }

      percentages[metric.ordinal()] = percentage;
   }

   private void writeCoveragePercentageForPackage(Metrics metric)
   {
      if (metric.isActive()) {
         int coveredInPackage = packageReport.coveredItems[metric.ordinal()];
         int totalInPackage = packageReport.totalItems[metric.ordinal()];
         int filePercentage = packageToPackagePercentages.get(packageName)[metric.ordinal()];

         printCoveragePercentage(metric, coveredInPackage, totalInPackage, filePercentage);
      }
   }
}
