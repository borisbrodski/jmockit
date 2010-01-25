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
   private boolean previousLineInComments;

   public LineCoverageOutput(
      PrintWriter output, Map<Integer, LineCoverageData> lineToLineData, boolean withCallPoints)
   {
      this.output = output;
      this.lineToLineData = lineToLineData;
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
   }

   public void writeLineOfSourceCodeWithCoverageInfo(LineParser lineParser)
   {
      if (writeLineInComments(lineParser)) {
         return;
      }

      int lineNum = lineParser.getNumber();
      writeOpeningOfNewLine(lineNum);

      if (lineParser.isBlankLine()) {
         output.println("<td colspan='2'></td>");
      }
      else {
         lineData = lineToLineData.get(lineNum);
         writeLineExecutionCountIfAny();
         writeCodeFromLine(lineParser);
      }

      output.println("    </tr>");
   }

   private boolean writeLineInComments(LineParser lineParser)
   {
      LineElement initialElement = lineParser.getInitialElement();

      if (
         lineParser.isInComments() ||
         previousLineInComments &&
         initialElement != null && initialElement.isComment() && initialElement.getNext() == null
      ) {
         if (!previousLineInComments) {
            writeOpeningForBlockOfCommentedLines();
            previousLineInComments = true;
         }

         output.println(initialElement.toString());
         return true;
      }
      else if (previousLineInComments) {
         output.println("</td></tr>");
         previousLineInComments = false;
      }

      return false;
   }

   private void writeOpeningForBlockOfCommentedLines()
   {
      output.println("    <tr class='click' onclick='showHideLines(this)'>");
      output.write("      <td class='line'></td><td>&nbsp;</td><td class='comment'>");
   }

   private void writeOpeningOfNewLine(int line)
   {
      output.println("    <tr>");
      output.write("      <td class='line'>");
      output.print(line);
      output.write("</td>");
   }

   private void writeLineExecutionCountIfAny()
   {
      if (lineData == null) {
         output.println("<td>&nbsp;</td>");
      }
      else {
         output.write("<td class='count'>");
         output.print(lineData.getExecutionCount());
         output.println("</td>");
      }
   }

   private void writeCodeFromLine(LineParser lineParser)
   {
      LineElement initialElement = lineParser.getInitialElement();

      if (lineData == null) {
         output.write("      <td><pre class='");
         output.write(initialElement.isComment() ? "comment'>" : "prettyprint'>");
         output.write(initialElement.toString());
         output.write("</pre>");
      }
      else {
         String formattedLine =
            lineCoverageFormatter.format(lineParser.getNumber(), lineData, initialElement);
         output.write("      <td>");
         output.write(formattedLine);
      }

      output.println("</td>");
   }
}
