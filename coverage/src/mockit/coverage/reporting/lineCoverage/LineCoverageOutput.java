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
package mockit.coverage.reporting.lineCoverage;

import java.io.*;
import java.util.*;

import mockit.coverage.data.*;
import mockit.coverage.reporting.parsing.*;

public final class LineCoverageOutput
{
   private final PrintWriter output;
   private final Map<Integer, LineCoverageData> lineToLineData;
   private final LineCoverageFormatter lineCoverageFormatter;
   private LineCoverageData lineData;

   public LineCoverageOutput(
      PrintWriter output, Map<Integer, LineCoverageData> lineToLineData, boolean withCallPoints)
   {
      this.output = output;
      this.lineToLineData = lineToLineData;
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
   }

   public void writeLineOfSourceCodeWithCoverageInfo(LineParser lineParser)
   {
      int lineNo = lineParser.getLineNo();

      writeOpeningOfNewExecutableLine(lineNo);

      lineData = lineToLineData.get(lineNo);
      writeLineExecutionCountIfAny();
      writeExecutableLine(lineParser);

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
      if (lineData == null) {
         output.println("<td></td>");
      }
      else {
         output.write("<td class='count'>");
         output.print(lineData.getExecutionCount());
         output.println("</td>");
      }
   }

   private void writeExecutableLine(LineParser lineParser)
   {
      if (lineParser.isBlankLine()) {
         output.println("      <td></td>");
         return;
      }

      output.write("      <td id='l");
      output.print(lineParser.getLineNo());

      LineSegment initialSegment = lineParser.getInitialSegment();

      if (lineData != null && lineData.getExecutionCount() > 0) {
         String formattedLine = lineCoverageFormatter.format(lineData, initialSegment);
         output.write(formattedLine);
         return;
      }

      output.write("' class='");
      output.write(lineData == null ? "nonexec'>" : "uncovered'>");

      if (lineData == null && initialSegment.isComment()) {
         output.write("<pre class='comment'>");
      }
      else {
         output.write("<pre class='prettyprint'>");
      }

      output.write(initialSegment.toString());
      output.println("</pre></td>");
   }
}
