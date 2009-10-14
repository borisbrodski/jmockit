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
 *
 * TODO: implement merge with previous xml file, in order to avoid losing coverage data for those
 * tests not run this time (either because the tests were simply not included in the test run, or
 * because they were ignored by the incremental test runner)
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
         System.out.println("\nJMockit: Coverage data written to " + outputFile.getCanonicalPath());
      }
   }

   private void writeXmlDocument() throws IOException
   {
      writeLine("<?xml version='1.0' encoding='UTF-8'?>");
      writeLine("<coverage>");

      Map<String,FileCoverageData> fileToFileData = coverageData.getFileToFileDataMap();

      for (Entry<String, FileCoverageData> lineCountEntry : fileToFileData.entrySet()) {
         output.write("  <file path='");
         output.write(lineCountEntry.getKey());
         writeLine("'>");

         FileCoverageData fileCoverageData = lineCountEntry.getValue();
         writeCoverageDataForSourceFile(fileCoverageData);

         writeLine("  </file>");
         output.newLine();
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
            writeChildElementsForBranches(lineData);
            pendingEndTag = true;
         }
         else if (writeChildElementsForLine(lineData)) {
            pendingEndTag = true;
         }

         writeLine(pendingEndTag ? "    </line>" : "'/>");
      }
   }

   protected abstract boolean writeChildElementsForLine(LineCoverageData lineData)
      throws IOException;

   private boolean writeChildElementsForBranches(LineCoverageData lineData) throws IOException
   {
      writeAttributeWithSourceElements(lineData);

      for (BranchCoverageData branchData : lineData.getBranches()) {
         int jumpTargetInsnIndex = branchData.getJumpTargetInsnIndex();
         int jumpCount = branchData.getJumpExecutionCount();
         int noJumpTargetInsnIndex = branchData.getNoJumpTargetInsnIndex();
         int noJumpCount = branchData.getNoJumpExecutionCount();

         if (jumpTargetInsnIndex >= 0 || noJumpTargetInsnIndex >= 0) {
            output.write("      <branch jumpInsn='");
            output.write(String.valueOf(branchData.getJumpInsnIndex()));
            output.write("'");

            if (noJumpTargetInsnIndex >= 0) {
               output.write(" noJumpTargetInsn='");
               output.write(String.valueOf(noJumpTargetInsnIndex));
               output.write("' noJumpCount='");
               output.write(String.valueOf(noJumpCount));
               output.write("'");
            }

            if (jumpTargetInsnIndex >= 0) {
               output.write(" jumpTargetInsn='");
               output.write(String.valueOf(jumpTargetInsnIndex));
               output.write("' jumpCount='");
               output.write(String.valueOf(jumpCount));
               output.write("'");
            }

            writeEndTagForBranch(branchData, jumpCount, noJumpCount);
            output.newLine();
         }
      }

      return true;
   }

   private void writeAttributeWithSourceElements(LineCoverageData lineData) throws IOException
   {
      String sep = "' source='";

      for (String sourceElement : lineData.getSourceElements()) {
         output.write(sep);
         output.write(sourceElement);
         sep = " ";
      }

      writeLine("'>");
   }

   protected abstract void writeEndTagForBranch(
      BranchCoverageData branchData, int jumpCount, int noJumpCount) throws IOException;
}
