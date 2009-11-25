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
import mockit.coverage.paths.*;

/**
 * Generates an XHTML page containing line-by-line coverage information for a single source file.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
final class FileCoverageReport
{
   private final Map<Integer, LineCoverageData> lineToLineData;
   private final File inputFile;
   private final BufferedReader input;
   private final String pathToOutputFile;
   private final PrintWriter output;
   private final boolean withCallPoints;

   // Helper fields.
   private final LineParser lineParser = new LineParser();
   private final LineSyntaxFormatter lineSyntaxFormatter = new LineSyntaxFormatter();
   private final LineCoverageFormatter lineCoverageFormatter;

   private final Iterator<MethodCoverageData> nextMethod;
   private MethodCoverageData currentMethod;
   private int previousMethodEndLine;

   private int lineNo;
   private String line;
   private LineCoverageData lineData;

   FileCoverageReport(
      String outputDir, List<File> sourceDirs, String filePath, FileCoverageData coverageData,
      boolean withCallPoints) throws IOException
   {
      lineToLineData = coverageData.getLineToLineData();
      inputFile = findSourceFile(sourceDirs, filePath);
      pathToOutputFile = filePath.replace(".java", ".html");
      this.withCallPoints = withCallPoints;
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);

      nextMethod = coverageData.getMethods().iterator();

      if (nextMethod.hasNext()) {
         currentMethod = nextMethod.next();
      }

      if (inputFile == null) {
         input = null;
         output = null;
         return;
      }

      input = new BufferedReader(new FileReader(inputFile));

      File outputFile = getOutputFileCreatingOutputDirIfNonExisting(outputDir);
      output = new PrintWriter(new FileWriter(outputFile));
   }

   private File findSourceFile(List<File> sourceDirs, String filePath)
   {
      int p = filePath.indexOf('/');
      String topLevelPackage = p < 0 ? "" : filePath.substring(0, p);

      for (File sourceDir : sourceDirs) {
         File file = getSourceFile(sourceDir, topLevelPackage, filePath);

         if (file != null) {
            return file;
         }
      }

      return null;
   }

   private File getSourceFile(File sourceDir, String topLevelPackage, String filePath)
   {
      File sourceFile = new File(sourceDir, filePath);

      if (sourceFile.exists()) {
         return sourceFile;
      }

      File[] subDirs = sourceDir.listFiles();

      for (File subDir : subDirs) {
         if (
            subDir.isDirectory() && !subDir.isHidden() && !subDir.getName().equals(topLevelPackage)
         ) {
            sourceFile = getSourceFile(subDir, topLevelPackage, filePath);

            if (sourceFile != null) {
               return sourceFile;
            }
         }
      }

      return null;
   }

   private File getOutputFileCreatingOutputDirIfNonExisting(String outputDir)
   {
      File outputFile = new File(outputDir, pathToOutputFile);
      File parentDir = outputFile.getParentFile();

      if (!parentDir.exists()) {
         boolean outputDirCreated = parentDir.mkdirs();
         assert outputDirCreated : "Failed to create output dir: " + outputDir;
      }

      return outputFile;
   }

   boolean wasSourceFileFound()
   {
      return inputFile != null;
   }

   void generate() throws IOException
   {
      try {
         writeHeader();
         writeFormattedSourceLines();
         writeFooter();
      }
      finally {
         input.close();
         output.close();
      }
   }

   private void writeHeader()
   {
      CoverageReport.writeCommonFileHeader(output, pathToOutputFile);

      if (withCallPoints) {
         output.println("  <script type='text/javascript'>");
         output.println("    function showHide(callPoints) {");
         output.println("      var list = callPoints.nextSibling.nextSibling.style;");
         output.println("      list.display = list.display == 'none' ? 'block' : 'none';");
         output.println("    }");
         output.println("  </script>");
      }

      output.println("</head>");
      output.println("<body>");
      output.println("  <table cellpadding='0' cellspacing='1'>");
      output.println("    <caption><code>" + inputFile.getPath() + "</code></caption>");
   }

   private void writeFormattedSourceLines() throws IOException
   {
      lineNo = 1;

      while ((line = input.readLine()) != null) {
         writePathCoverageInfoIfLineStartsANewMethodOrConstructor();
         writeOpeningOfNewExecutableLine();

         lineData = lineToLineData.get(lineNo);
         writeLineExecutionCountIfAny();
         writeExecutableLine();

         output.println("    </tr>");
         lineNo++;
      }
   }

   private void writePathCoverageInfoIfLineStartsANewMethodOrConstructor()
   {
      if (
         currentMethod == null || lineNo <= previousMethodEndLine ||
         lineNo > currentMethod.getFirstLineOfImplementationBody()
      ) {
         return;
      }

      int p = line.indexOf(currentMethod.methodName);

      if (p < 0) {
         return;
      }

      int q = p + currentMethod.methodName.length();

      if (line.length() <= q || line.charAt(q) != '(') {
         return;
      }

      writePathCoverageHeaderForMethod();

      char pathId = 'A';

      for (Path path : currentMethod.paths) {
         writeCoverageInfoForIndividualPath(pathId, path);
         pathId++;
      }

      writePathCoverageFooterForMethod();

      previousMethodEndLine = currentMethod.getLastLineOfImplementationBody();
      currentMethod = nextMethod.hasNext() ? nextMethod.next() : null;
   }

   private void writePathCoverageHeaderForMethod()
   {
      int coveredPaths = currentMethod.getCoveredPaths();
      int totalPaths = currentMethod.paths.size();
      int coveragePercentage = CoveragePercentage.calculate(coveredPaths, totalPaths);

      output.println("    <tr>");
      output.print("      <td></td><td class='count'>");
      output.print(currentMethod.getExecutionCount());
      output.println("</td>");
      output.println("      <td class='paths'>");
      output.print("        <span style='background-color:#");
      output.print(CoveragePercentage.percentageColor(coveragePercentage));
      output.print("'>Path coverage: ");
      output.print(coveredPaths);
      output.print('/');
      output.print(totalPaths);
      output.println("</span>");
   }

   private void writeCoverageInfoForIndividualPath(char pathId, Path path)
   {
      int executionCount = path.getExecutionCount();

      output.print("        <span class='");
      output.print(executionCount == 0 ? "uncovered" : "covered");
      output.print("'>");
      output.print(pathId);
      output.print(": ");
      output.print(executionCount);
      output.println("</span>");
   }

   private void writePathCoverageFooterForMethod()
   {
      output.println();
      output.println("      </td>");
      output.println("    </tr>");
   }

   private void writeOpeningOfNewExecutableLine()
   {
      output.println("    <tr>");
      output.print("      <td class='lineNo'>");
      output.print(lineNo);
      output.print("</td>");
   }

   private void writeLineExecutionCountIfAny()
   {
      if (lineData != null) {
         output.print("<td class='count'>");
         output.print(lineData.getExecutionCount());
         output.println("</td>");
      }
      else {
         output.println("<td>&nbsp;</td>");
      }
   }

   private void writeExecutableLine()
   {
      if (line.trim().length() == 0) {
         output.println("      <td/>");
         return;
      }

      LineSegment initialSegment = lineParser.parse(line);
      lineSyntaxFormatter.format(initialSegment);

      String lineStatus =
         lineData == null ? "nonexec" : lineData.getExecutionCount() == 0 ? "uncovered" : null;

      if (lineStatus != null) {
         output.print("      <td class='");
         output.print(lineStatus);
         output.print("'><pre>");
         output.print(initialSegment.toString());
         output.println("</pre></td>");
      }
      else {
         line = lineCoverageFormatter.format(lineData, initialSegment);
         output.print(line);
      }
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.println("</body>");
      output.println("</html>");
   }
}
