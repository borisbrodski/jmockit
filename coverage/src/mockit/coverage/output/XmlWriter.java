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
package mockit.coverage.output;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

/**
 * Produces an xml file with all the coverage data gathered during a test run. If the file already
 * exists, it is overwritten.
 */
public abstract class XmlWriter
{
   private final CoverageData coverageData;
   protected BufferedWriter output;
   private String filePath;
   private int line;
   private boolean pendingEndTag;

   protected XmlWriter(CoverageData coverageData)
   {
      this.coverageData = coverageData;
   }

   public final void writeToXmlFile(String outputDir) throws IOException
   {
      File outputFile = new File(outputDir.length() == 0 ? null : outputDir, "coverage.xml");

      if (outputFile.exists() && !outputFile.canWrite()) {
         System.out.println(
            "JMockit: " + outputFile.getCanonicalPath() +
            " is read-only; file generation canceled");
         return;
      }

      output = new BufferedWriter(new FileWriter(outputFile));

      try {
         writeXmlDocument();
      }
      finally {
         output.close();
         System.out.println();
         System.out.println("JMockit: Coverage data written to " + outputFile.getCanonicalPath());
      }
   }

   private void writeXmlDocument() throws IOException
   {
      writeLine("<?xml version='1.0' encoding='UTF-8'?>");
      writeLine("<coverage>");

      Map<String, FileCoverageData> fileToFileData = coverageData.getFileToFileDataMap();
      boolean firstFile = true;

      for (Entry<String, FileCoverageData> lineCountEntry : fileToFileData.entrySet()) {
         if (!firstFile) {
            output.newLine();
         }

         writeFileElement(lineCountEntry);
         firstFile = false;
      }

      writeLine("</coverage>");
   }

   private void writeLine(String line) throws IOException
   {
      output.write(line);
      output.newLine();
   }

   private void writeFileElement(Entry<String, FileCoverageData> lineCountEntry) throws IOException
   {
      output.write("  <file path='");
      filePath = lineCountEntry.getKey();
      output.write(filePath);
      writeLine("'>");

      FileCoverageData fileCoverageData = lineCountEntry.getValue();
      writeCoverageDataForSourceFile(fileCoverageData);
      writeBranchingPaths(fileCoverageData);

      writeLine("  </file>");
   }

   private void writeCoverageDataForSourceFile(FileCoverageData fileData) throws IOException
   {
      SortedMap<Integer, LineCoverageData> lineToLineData = fileData.getLineToLineData();

      for (Entry<Integer, LineCoverageData> lineAndLineData : lineToLineData.entrySet()) {
         line = lineAndLineData.getKey();
         LineCoverageData lineData = lineAndLineData.getValue();

         output.write("    <line number='");
         output.write(String.valueOf(line));

         if (lineData.isUnreachable()) {
            output.write("' unreachable='true");
         }

         output.write("' count='");
         output.write(String.valueOf(lineData.getExecutionCount()));

         pendingEndTag = writeChildElementsForLine(lineData);

         if (lineData.containsSegments()) {
            pendingEndTag = writeChildElementsForSegments(lineData) || pendingEndTag;
         }

         writeLine(pendingEndTag ? "    </line>" : "'/>");
      }
   }

   protected abstract boolean writeChildElementsForLine(LineCoverageData lineData)
      throws IOException;

   private boolean writeChildElementsForSegments(LineCoverageData lineData) throws IOException
   {
      boolean nonEmptySegmentFound = false;

      for (BranchCoverageData segmentData : lineData.getSegments()) {
         if (segmentData.isNonEmpty()) {
            if (!nonEmptySegmentFound) {
               if (!pendingEndTag) {
                  writeLine("'>");
               }

               nonEmptySegmentFound = true;
            }

            writeChildElementForSegment(segmentData);
            output.newLine();
         }
         else {
            assert true :
               "XmlWriter#writeChildElementsForSegments: empty segment in " + filePath + ':' + line;
         }
      }

      return nonEmptySegmentFound;
   }

   private void writeChildElementForSegment(BranchCoverageData data) throws IOException
   {
      output.write("      <segment");

      if (data.isUnreachable()) {
         output.write(" unreachable='true'");
      }

      int noJumpCount = data.getNoJumpExecutionCount();

      if (data.hasNoJumpTarget()) {
         output.write(" noJumpCount='");
         output.write(String.valueOf(noJumpCount));
         output.write("'");
      }

      int jumpCount = data.getJumpExecutionCount();

      if (data.hasJumpTarget()) {
         output.write(" jumpCount='");
         output.write(String.valueOf(jumpCount));
         output.write("'");
      }

      writeEndTagForSegment(data, jumpCount, noJumpCount);
   }

   protected abstract void writeEndTagForSegment(
      BranchCoverageData data, int jumpCount, int noJumpCount) throws IOException;

   private void writeBranchingPaths(FileCoverageData fileData) throws IOException
   {
      output.newLine();

      PathCoverage pathCoverage = new PathCoverage(fileData);

      for (MethodCoverageData methodData : fileData.getMethods()) {
         output.write("    <paths method='");
         output.write(methodData.methodNameAndDesc);
         writeLine("'>");

         List<Path> paths = pathCoverage.buildPaths(methodData);

         for (Path path : paths) {
            output.write("      <path>");
            output.write(path.getListOfSourceLocations());
            writeLine("</path>");
         }

         writeLine("    </paths>");
      }
   }
}
