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
   private final boolean withCallPoints;
   private final Map<Integer, LineCoverageData> lineToLineData;
   final InputFile inputFile;
   private final OutputFile output;

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
      String outputDir, List<File> sourceDirs, String sourceFilePath, FileCoverageData coverageData,
      boolean withCallPoints) throws IOException
   {
      this.withCallPoints = withCallPoints;
      lineToLineData = coverageData.getLineToLineData();
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
      nextMethod = getCoverageDataForFirstMethod(coverageData);
      inputFile = new InputFile(sourceDirs, sourceFilePath);

      if (!inputFile.wasFileFound()) {
         output = null;
         return;
      }

      output = new OutputFile(outputDir, sourceFilePath);
   }

   private Iterator<MethodCoverageData> getCoverageDataForFirstMethod(FileCoverageData coverageData)
   {
      Iterator<MethodCoverageData> itr = coverageData.getMethods().iterator();

      if (itr.hasNext()) {
         currentMethod = itr.next();
      }

      return itr;
   }

   void generate() throws IOException
   {
      try {
         writeHeader();
         writeFormattedSourceLines();
         writeFooter();
      }
      finally {
         inputFile.input.close();
         output.close();
      }
   }

   private void writeHeader()
   {
      output.printCommonFileHeader();

      output.println("  <script type='text/javascript'>");
      writeJavaScriptFunctionsForPathViewing();

      if (withCallPoints) {
         writeJavaScriptFunctionForCallPointViewing();
      }

      output.println("  </script>");
      output.println("</head>");
      output.println("<body>");
      output.println("  <table cellpadding='0' cellspacing='1'>");
      output.println("    <caption><code>" + inputFile.sourceFile.getPath() + "</code></caption>");
   }

   private void writeJavaScriptFunctionsForPathViewing()
   {
      output.println("    var pathIdShown;");
      output.println("    var lineIdsShown;");
      output.println("    function hidePath(pathId) {");
      output.println("      if (lineIdsShown) {");
      output.println("        for (var i = 0; i < lineIdsShown.length; i++) {");
      output.println("          var line = document.getElementById(lineIdsShown[i]);");
      output.println("          line.style.outlineStyle = 'none';");
      output.println("        }");
      output.println("        lineIdsShown = null; return pathId == pathIdShown;");
      output.println("      }");
      output.println("      return false;");
      output.println("    }");
      output.println("    function showPath(pathId, lineIdsStr) {");
      output.println("      if (hidePath(pathId)) return;");
      output.println("      pathIdShown = pathId;");
      output.println("      lineIdsShown = lineIdsStr.split(' ');");
      output.println("      for (var i = 0; i < lineIdsShown.length; i++) {");
      output.println("        var line = document.getElementById(lineIdsShown[i]);");
      output.println("        line.style.outline = 'thin dashed #0000FF';");
      output.println("      }");
      output.println("    }");
   }

   private void writeJavaScriptFunctionForCallPointViewing()
   {
      output.println("    function showHide(callPoints) {");
      output.println("      var list = callPoints.nextSibling.nextSibling.style;");
      output.println("      list.display = list.display == 'none' ? 'block' : 'none';");
      output.println("    }");
   }

   private void writeFormattedSourceLines() throws IOException
   {
      lineNo = 1;

      while ((line = inputFile.input.readLine()) != null) {
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

      if (
         (line.length() == q || line.charAt(q) != '(') &&
         (currentMethod.getFirstLineOfImplementationBody() > lineNo ||
          currentMethod.getLastLineOfImplementationBody() > lineNo)
      ) {
         return;
      }

      if (currentMethod.paths.size() > 1) {
         writePathCoverageHeaderForMethod();

         char pathId = 'A';

         for (Path path : currentMethod.paths) {
            writeCoverageInfoForIndividualPath(pathId, path);
            pathId++;
         }

         writePathCoverageFooterForMethod();
      }

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
      output.print("        <span style='cursor:default; background-color:#");
      output.print(CoveragePercentage.percentageColor(coveragePercentage));
      output.print("' onclick='hidePath()'>Path coverage: ");
      output.print(coveredPaths);
      output.print('/');
      output.print(totalPaths);
      output.println("</span>");
   }

   private void writeCoverageInfoForIndividualPath(char pathId, Path path)
   {
      int executionCount = path.getExecutionCount();
      StringBuilder lineIds = new StringBuilder();
      String sep = "";

      for (Node node : path.getNodes()) {
         if (node.line > 0) {
            lineIds.append(sep).append(node.line);
            sep = " ";
         }
      }

      output.print("        <span class='");
      output.print(executionCount == 0 ? "uncovered" : "covered");
      output.print("' onclick=\"showPath('");
      output.print(pathId);
      output.print("','");
      output.print(lineIds.toString());
      output.print("')\">");
      output.print(pathId);
      output.print(": ");
      output.print(executionCount);
      output.println("</span>");
   }

   private void writePathCoverageFooterForMethod()
   {
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
         output.print("      <td id='");
         output.print(lineNo);
         output.print("' class='");
         output.print(lineStatus);
         output.print("'><pre>");
         output.print(initialSegment.toString());
         output.println("</pre></td>");
      }
      else {
         line = lineCoverageFormatter.format(lineNo, lineData, initialSegment);
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
