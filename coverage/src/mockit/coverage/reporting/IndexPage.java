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

final class IndexPage extends ListWithFilesAndPercentages
{
   private Map<String, List<String>> packagesToFiles;
   private Map<String, Integer> packagesToPackagePercentages;
   private PackageCoverageReport packageReport;

   IndexPage(File outputFile) throws IOException
   {
      super(new PrintWriter(new FileWriter(outputFile)), 0);
   }

   void generate(
      Map<String, FileCoverageData> filesToFileData, Map<String, List<String>> packagesToFiles)
   {
      this.packagesToFiles = packagesToFiles;

      packageReport = new PackageCoverageReport(output, filesToFileData);
      packagesToPackagePercentages = new HashMap<String, Integer>();

      try {
         writeHeader();

         List<String> packages = new ArrayList<String>(packagesToFiles.keySet());
         writeMetricForEachFile(packages);

         writeLineWithTotalCoverage();
         writeFooter();
      }
      finally {
         output.close();
      }
   }

   private void writeHeader()
   {
      CoverageReport.writeCommonFileHeader(output, "index.html");

      output.println("</head>");
      output.println("<body>");
      output.println("  <h1>JMockit Coverage Report</h1>");
      output.println("  <table cellpadding='0' cellspacing='1'>");
      output.println("    <caption>All Packages and Files</caption>");
      output.print("    <tr><th>Packages (");
      output.print(packagesToFiles.keySet().size());
      output.print(")</th><th>Files (");

      int totalFileCount = computeTotalNumberOfSourceFiles();
      output.print(totalFileCount);

      output.println(")</th><th>Coverage</th></tr>");
   }

   private int computeTotalNumberOfSourceFiles()
   {
      int totalFileCount = 0;

      for (List<String> files : packagesToFiles.values()) {
         totalFileCount += files.size();
      }

      return totalFileCount;
   }

   private void writeLineWithTotalCoverage()
   {
      int totalPercentage = (int) (100.0 * coveredSegments / totalSegments + 0.5);

      output.print("  <tr><td colspan='2' class='total'>Total</td>");
      printCoveragePercentage(totalPercentage);
      output.println("</tr>");
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.print("<p>Generated on ");
      output.print(new Date());
      output.println("</p>");
      output.println("</body>");
      output.println("</html>");
   }

   @Override
   protected int getTotalSegments(String filePath)
   {
      return 0;
   }

   @Override
   protected int getCoveredSegments(String filePath)
   {
      return 0;
   }

   @Override
   protected int getCoveragePercentageForFile(String packageName)
   {
      return packagesToPackagePercentages.get(packageName);
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
   protected void writeInternalTableForChildren(String packageName)
   {
      printIndent(3); output.println("<td>");
      printIndent(4); output.println("<table width='100%' cellpadding='1' cellspacing='1'>");

      List<String> packageFiles = packagesToFiles.get(packageName);
      int packagePercentage = packageReport.writeMetricForEachFile(packageFiles);
      packagesToPackagePercentages.put(packageName, packagePercentage);

      totalSegments += packageReport.totalSegments;
      coveredSegments += packageReport.coveredSegments;

      printIndent(4); output.println("</table>");
      printIndent(3); output.println("</td>");
   }
}
