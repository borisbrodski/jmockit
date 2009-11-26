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

import mockit.coverage.*;

/**
 * Generates an XHTML page containing line-by-line coverage information for a single source file.
 */
final class FileCoverageReport
{
   private final InputFile inputFile;
   private final OutputFile output;
   private final CodeCoverageOutput codeCoverage;
   private final PathCoverageOutput pathCoverage;

   FileCoverageReport(
      String outputDir, InputFile inputFile, String sourceFilePath, FileCoverageData fileData,
      boolean withCallPoints) throws IOException
   {
      this.inputFile = inputFile;
      output = new OutputFile(outputDir, sourceFilePath);
      codeCoverage = new CodeCoverageOutput(output, fileData.getLineToLineData(), withCallPoints);
      pathCoverage = new PathCoverageOutput(fileData.getMethods(), output);
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
      writeJavaScriptFunctionForCallPointViewing();
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
      int lineNo = 1;
      String line;

      while ((line = inputFile.input.readLine()) != null) {
         pathCoverage.writePathCoverageInfoIfLineStartsANewMethodOrConstructor(lineNo, line);
         codeCoverage.writeLineOfSourceCodeWithCoverageInfo(lineNo, line);
         lineNo++;
      }
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.println("</body>");
      output.println("</html>");
   }
}
