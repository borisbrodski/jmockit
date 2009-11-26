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

final class CodeCoverageOutput
{
   private final PrintWriter output;
   private final Map<Integer, LineCoverageData> lineToLineData;
   private final LineParser lineParser = new LineParser();
   private final LineSyntaxFormatter lineSyntaxFormatter = new LineSyntaxFormatter();
   private final LineCoverageFormatter lineCoverageFormatter;
   private LineCoverageData lineData;

   CodeCoverageOutput(
      PrintWriter output, Map<Integer, LineCoverageData> lineToLineData, boolean withCallPoints)
   {
      this.output = output;
      this.lineToLineData = lineToLineData;
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
   }

   void writeLineOfSourceCodeWithCoverageInfo(int lineNo, String line)
   {
      writeOpeningOfNewExecutableLine(lineNo);

      lineData = lineToLineData.get(lineNo);
      writeLineExecutionCountIfAny();
      writeExecutableLine(lineNo, line);

      output.println("    </tr>");
   }

   private void writeOpeningOfNewExecutableLine(int lineNo)
   {
      output.println("    <tr>");
      output.write("      <td class='lineNo'>");
      output.print(lineNo);
      output.write("</td>");
   }

   private void writeLineExecutionCountIfAny()
   {
      if (lineData != null) {
         output.write("<td class='count'>");
         output.print(lineData.getExecutionCount());
         output.println("</td>");
      }
      else {
         output.println("<td>&nbsp;</td>");
      }
   }

   private void writeExecutableLine(int lineNo, String line)
   {
      if (line.trim().length() == 0) {
         output.println("      <td></td>");
         return;
      }

      LineSegment initialSegment = lineParser.parse(line);
      lineSyntaxFormatter.format(initialSegment);

      String lineStatus =
         lineData == null ? "nonexec" : lineData.getExecutionCount() == 0 ? "uncovered" : null;

      if (lineStatus != null) {
         output.write("      <td id='");
         output.print(lineNo);
         output.write("' class='");
         output.write(lineStatus);
         output.write("'><pre>");
         output.write(initialSegment.toString());
         output.println("</pre></td>");
      }
      else {
         String formattedLine = lineCoverageFormatter.format(lineNo, lineData, initialSegment);
         output.write(formattedLine);
      }
   }
}
