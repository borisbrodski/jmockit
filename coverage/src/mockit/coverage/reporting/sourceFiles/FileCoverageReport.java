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
package mockit.coverage.reporting.sourceFiles;

import java.io.*;
import java.util.*;

import mockit.coverage.data.*;
import mockit.coverage.data.dataItems.*;
import mockit.coverage.paths.*;
import mockit.coverage.reporting.OutputFile;
import mockit.coverage.reporting.dataCoverage.*;
import mockit.coverage.reporting.lineCoverage.*;
import mockit.coverage.reporting.parsing.*;
import mockit.coverage.reporting.pathCoverage.*;

/**
 * Generates an XHTML page containing line-by-line coverage information for a single source file.
 */
public final class FileCoverageReport
{
   private final InputFile inputFile;
   private final OutputFile output;
   private final FileParser fileParser = new FileParser();
   private final LineCoverageOutput lineCoverage;
   private final PathCoverageOutput pathCoverage;
   private final DataCoverageOutput dataCoverage;

   public FileCoverageReport(
      String outputDir, InputFile inputFile, FileCoverageData fileData, boolean withCallPoints)
      throws IOException
   {
      this.inputFile = inputFile;
      output = new OutputFile(outputDir, inputFile.filePath);

      lineCoverage = new LineCoverageOutput(output, fileData.getLineToLineData(), withCallPoints);

      Collection<MethodCoverageData> methods = fileData.getMethods();
      pathCoverage = methods.isEmpty() ? null : new PathCoverageOutput(output, methods);

      DataCoverageInfo dataCoverageInfo = fileData.dataCoverageInfo;
      dataCoverage = dataCoverageInfo.hasFields() ? new DataCoverageOutput(dataCoverageInfo) : null;
   }

   public void generate() throws IOException
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
      output.writeCommonHeader();
      output.println("  <table cellpadding='0' cellspacing='1'>");
      output.println("    <caption><code>" + inputFile.sourceFile.getPath() + "</code></caption>");
   }

   private void writeFormattedSourceLines() throws IOException
   {
      String line;

      while ((line = inputFile.input.readLine()) != null) {
         boolean lineWithCodeElements = fileParser.parseCurrentLine(line);

         if (lineWithCodeElements) {
            if (dataCoverage != null) {
               dataCoverage.writeCoverageInfoIfLineStartsANewFieldDeclaration(fileParser);
            }

            if (pathCoverage != null) {
               pathCoverage.writePathCoverageInfoIfLineStartsANewMethodOrConstructor(
                  fileParser.lineParser.getNumber());
            }
         }

         lineCoverage.writeLineOfSourceCodeWithCoverageInfo(fileParser.lineParser);
      }
   }

   private void writeFooter()
   {
      output.println("  </table>");
      output.writeCommonFooter();
   }
}
