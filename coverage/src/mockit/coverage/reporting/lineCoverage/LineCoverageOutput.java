/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   private String initialCommentLineIndentation;

   public LineCoverageOutput(PrintWriter output, Map<Integer, LineCoverageData> lineToLineData, boolean withCallPoints)
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
         String lineText = initialElement.toString();

         if (previousLineInComments) {
            output.println();
         }
         else {
            writeOpeningForBlockOfCommentedLines();
            extractInitialCommentLineIndentation(lineText);
            previousLineInComments = true;
         }

         output.write(lineText);
         return true;
      }
      else if (previousLineInComments) {
         output.append("</div><span>").append(initialCommentLineIndentation).println("/*...*/</span></td></tr>");
         previousLineInComments = false;
      }

      return false;
   }

   private void writeOpeningForBlockOfCommentedLines()
   {
      output.println("    <tr class='click' onclick='showHideLines(this)'>");
      output.write("      <td class='line'></td><td>&nbsp;</td><td class='comment'><div>");
   }

   private void extractInitialCommentLineIndentation(String lineText)
   {
      int indentationSize = 0;

      for (int i = 0; i < lineText.length(); i++, indentationSize++) {
         if (lineText.charAt(i) > ' ') break;
      }

      initialCommentLineIndentation = lineText.substring(0, indentationSize);
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
         String formattedLine = lineCoverageFormatter.format(lineParser.getNumber(), lineData, initialElement);
         output.write("      <td>");
         output.write(formattedLine);
      }

      output.println("</td>");
   }
}
