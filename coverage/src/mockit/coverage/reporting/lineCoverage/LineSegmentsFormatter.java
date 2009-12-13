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

import java.util.*;

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.LineElement;

final class LineSegmentsFormatter
{
   private final boolean withCallPoints;
   private final int line;
   private final StringBuilder formattedLine;
   private BranchCoverageData segmentData;
   private LineElement element;
   private int segmentIndex;

   LineSegmentsFormatter(boolean withCallPoints, int line, StringBuilder formattedLine)
   {
      this.withCallPoints = withCallPoints;
      this.line = line;
      this.formattedLine = formattedLine;
   }

   void formatBranches(List<BranchCoverageData> segmentedData, LineElement initialElement)
   {
      element = initialElement;
      int numSegments = segmentedData.size();

      for (segmentIndex = 0; segmentIndex < numSegments; segmentIndex++) {
         segmentData = segmentedData.get(segmentIndex);

         while (!element.isCode()) {
            formattedLine.append(element.getText());
            element = element.getNext();

            if (element == null) {
               return;
            }
         }

         LineElement firstElement = element;

         while (element != null && !element.isBranchingPoint()) {
            element = element.getNext();
         }

         appendToFormattedLine(firstElement);

         if (element != null) {
            formattedLine.append(element.getText());
            element = element.getNext();
         }

         if (element == null) {
            return;
         }
      }
   }

   private void appendToFormattedLine(LineElement firstElement)
   {
      if (firstElement == element) {
         return;
      }

      appendStartTag();

      LineElement elementToPrint = firstElement;

      while (true) {
         LineElement nextElement = elementToPrint.getNext();

         if (nextElement == element) {
            break;
         }

         formattedLine.append(elementToPrint.getText());
         elementToPrint = nextElement;
      }

      appendEndTag();
      formattedLine.append(elementToPrint.getText());
   }

   private void appendStartTag()
   {
      formattedLine.append("<span id='l").append(line);
      formattedLine.append('s').append(segmentIndex).append("' ");

      appendTooltipWithExecutionCounts();

      if (segmentData.isCovered()) {
         formattedLine.append("class='covered");

         if (withCallPoints) {
            formattedLine.append(" withCallPoints' onclick='showHide(this)");
         }

         formattedLine.append("'>");
      }
      else {
         formattedLine.append("class='uncovered'>");
      }
   }

   private void appendTooltipWithExecutionCounts()
   {
      int noJumpCount = segmentData.getNoJumpExecutionCount();
      int jumpCount = segmentData.getJumpExecutionCount();

      if (noJumpCount >= 0 && jumpCount >= 0) {
         formattedLine.append("title='Executions: ").append(noJumpCount + jumpCount);
         formattedLine.append("\r\nJumps: ").append(jumpCount).append("' ");
      }
      else if (noJumpCount > 0) {
         formattedLine.append("title='Executions: ").append(noJumpCount);
         formattedLine.append("\r\nNo jumps' ");
      }
      else if (jumpCount > 0) {
         formattedLine.append("title='Jumps: ").append(jumpCount).append("' ");
      }
   }

   private void appendEndTag()
   {
      formattedLine.append("</span>");

      if (withCallPoints && segmentData.isNonEmpty()) {
         new ListOfCallPoints().insertListOfCallPoints(formattedLine, segmentData.getCallPoints());
      }
   }
}
