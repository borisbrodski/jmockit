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

/**
 * Produces an xml file with all the coverage data gathered during a test run. If the file already
 * exists, it is overwritten.
 */
public abstract class XmlWriter
{
   private final CoverageData coverageData;
   protected BufferedWriter output;

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
         
         output.write("  <file path='");
         output.write(lineCountEntry.getKey());
         writeLine("'>");

         FileCoverageData fileCoverageData = lineCountEntry.getValue();
         writeCoverageDataForSourceFile(fileCoverageData);

         writeLine("  </file>");
         firstFile = false;
      }

      writeLine("</coverage>");
   }

   private void writeLine(String line) throws IOException
   {
      output.write(line);
      output.newLine();
   }

   private void writeCoverageDataForSourceFile(FileCoverageData fileData) throws IOException
   {
      SortedMap<Integer, LineCoverageData> lineToLineData = fileData.getLineToLineData();

      for (Entry<Integer, LineCoverageData> lineAndLineData : lineToLineData.entrySet()) {
         Integer line = lineAndLineData.getKey();
         LineCoverageData lineData = lineAndLineData.getValue();

         output.write("    <line number='");
         output.write(line.toString());
         output.write("' count='");
         output.write(String.valueOf(lineData.getExecutionCount()));

         boolean pendingEndTag = false;

         if (lineData.containsBranches()) {
            pendingEndTag = writeChildElementsForSegments(lineData);
         }
         else if (writeChildElementsForLine(lineData)) {
            pendingEndTag = true;
         }

         writeLine(pendingEndTag ? "    </line>" : "'/>");
      }
   }

   protected abstract boolean writeChildElementsForLine(LineCoverageData lineData)
      throws IOException;

   private boolean writeChildElementsForSegments(LineCoverageData lineData) throws IOException
   {
      writeAttributeWithSourceElements(lineData);

      boolean nonEmptySegmentFound = false;

      for (BranchCoverageData segmentData : lineData.getBranches()) {
         if (segmentData.isNonEmpty()) {
            if (!nonEmptySegmentFound) {
               writeLine("'>");
               nonEmptySegmentFound = true;
            }

            writeChildElementForSegment(segmentData);
            output.newLine();
         }
      }

      return nonEmptySegmentFound;
   }

   private void writeChildElementForSegment(BranchCoverageData segmentData)
      throws IOException
   {
      output.write("      <branch jumpInsn='");
      output.write(String.valueOf(segmentData.getJumpInsnIndex()));
      output.write("'");

      int noJumpTargetInsnIndex = segmentData.getNoJumpTargetInsnIndex();
      int noJumpCount = segmentData.getNoJumpExecutionCount();

      if (noJumpTargetInsnIndex >= 0) {
         output.write(" noJumpTargetInsn='");
         output.write(String.valueOf(noJumpTargetInsnIndex));
         output.write("' noJumpCount='");
         output.write(String.valueOf(noJumpCount));
         output.write("'");
      }

      int jumpTargetInsnIndex = segmentData.getJumpTargetInsnIndex();
      int jumpCount = segmentData.getJumpExecutionCount();

      if (jumpTargetInsnIndex >= 0) {
         output.write(" jumpTargetInsn='");
         output.write(String.valueOf(jumpTargetInsnIndex));
         output.write("' jumpCount='");
         output.write(String.valueOf(jumpCount));
         output.write("'");
      }

      writeEndTagForBranch(segmentData, jumpCount, noJumpCount);
   }

   private void writeAttributeWithSourceElements(LineCoverageData lineData) throws IOException
   {
      String sep = "' source='";

      for (String sourceElement : lineData.getSourceElements()) {
         output.write(sep);
         output.write(sourceElement);
         sep = " ";
      }
   }

   protected abstract void writeEndTagForBranch(
      BranchCoverageData branchData, int jumpCount, int noJumpCount) throws IOException;
}
