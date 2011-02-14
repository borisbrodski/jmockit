/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.sourceFiles;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
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
      pathCoverage = createPathCoverageOutput(fileData);
      dataCoverage = createDataCoverageOutput(fileData);
   }

   private PathCoverageOutput createPathCoverageOutput(FileCoverageData fileData)
   {
      if (Metrics.PATH_COVERAGE) {
         Collection<MethodCoverageData> methods = fileData.getMethods();
         return methods.isEmpty() ? null : new PathCoverageOutput(output, methods);
      }
      else {
         return null;
      }
   }

   private DataCoverageOutput createDataCoverageOutput(FileCoverageData fileData)
   {
      if (Metrics.DATA_COVERAGE) {
         DataCoverageInfo dataCoverageInfo = fileData.dataCoverageInfo;
         return dataCoverageInfo.hasFields() ? new DataCoverageOutput(dataCoverageInfo) : null;
      }
      else {
         return null;
      }
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
