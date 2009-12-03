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

import mockit.coverage.data.*;
import mockit.coverage.paths.*;
import mockit.coverage.reporting.codeCoverage.*;
import mockit.coverage.reporting.parsing.*;
import mockit.coverage.reporting.pathCoverage.*;

/**
 * Generates an XHTML page containing line-by-line coverage information for a single source file.
 */
final class FileCoverageReport
{
   private final InputFile inputFile;
   private final OutputFile output;
   private final LineParser lineParser = new LineParser();
   private final CodeCoverageOutput codeCoverage;
   private final PathCoverageOutput pathCoverage;

   FileCoverageReport(
      String outputDir, InputFile inputFile, FileCoverageData fileData, boolean withCallPoints)
      throws IOException
   {
      this.inputFile = inputFile;
      output = new OutputFile(outputDir, inputFile.filePath);
      codeCoverage = new CodeCoverageOutput(output, fileData.getLineToLineData(), withCallPoints);

      Collection<MethodCoverageData> methods = fileData.getMethods();
      pathCoverage = methods.isEmpty() ? null : new PathCoverageOutput(output, methods);
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
      output.printCommonHeader(true);
      output.println("  <table cellpadding='0' cellspacing='1'>");
      output.println("    <caption><code>" + inputFile.sourceFile.getPath() + "</code></caption>");
   }

   private void writeFormattedSourceLines() throws IOException
   {
      String line;

      while ((line = inputFile.input.readLine()) != null) {
         lineParser.parse(line);

         if (pathCoverage != null && !lineParser.isInComments()) {
            pathCoverage.writePathCoverageInfoIfLineStartsANewMethodOrConstructor(lineParser);
         }

         codeCoverage.writeLineOfSourceCodeWithCoverageInfo(lineParser);
      }
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.writeCommonFooter();
   }
}
