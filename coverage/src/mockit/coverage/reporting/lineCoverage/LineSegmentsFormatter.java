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
   private final int lineNum;
   private final StringBuilder formattedLine;

   // Helper fields:
   private LineElement element;
   private int segmentIndex;
   private LineSegmentData segmentData;

   LineSegmentsFormatter(boolean withCallPoints, int lineNum, StringBuilder formattedLine)
   {
      this.withCallPoints = withCallPoints;
      this.lineNum = lineNum;
      this.formattedLine = formattedLine;
   }

   void formatSegments(LineCoverageData lineData, LineElement initialElement)
   {
      List<BranchCoverageData> branchData = lineData.getBranches();
      int numSegments = 1 + branchData.size();

      element = initialElement.appendUntilNextCodeElement(formattedLine);

      segmentIndex = 0;
      segmentData = lineData;
      appendUntilFirstElementAfterNextBranchingPoint();

      for (segmentIndex = 1; element != null && segmentIndex < numSegments; segmentIndex++) {
         segmentData = branchData.get(segmentIndex - 1);

         element = element.appendUntilNextCodeElement(formattedLine);

         if (element != null) {
            appendUntilFirstElementAfterNextBranchingPoint();
         }
      }
   }

   private void appendUntilFirstElementAfterNextBranchingPoint()
   {
      LineElement firstElement = element;
      element = element.findNextBranchingPoint();

      appendToFormattedLine(firstElement);

      if (element != null && element.isBranchingElement()) {
         formattedLine.append(element.getText());
         element = element.getNext();
      }
   }

   private void appendToFormattedLine(LineElement firstElement)
   {
      if (firstElement == element) {
         return;
      }

      appendStartTag();

      LineElement elementToPrint = firstElement;

      do {
         formattedLine.append(elementToPrint.getText());
         elementToPrint = elementToPrint.getNext();
      }
      while (elementToPrint != element);

      appendEndTag();
   }

   private void appendStartTag()
   {
      formattedLine.append("<span id='l").append(lineNum);
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
      formattedLine.append("title='Executions: ");

      int noJumpCount = segmentData.getExecutionCount();

      if (segmentData instanceof BranchCoverageData) {
         int jumpCount = ((BranchCoverageData) segmentData).getJumpExecutionCount();

         if (noJumpCount >= 0 && jumpCount >= 0) {
            formattedLine.append(noJumpCount + jumpCount);
            formattedLine.append("\r\nJumps: ").append(jumpCount);
         }
         else if (noJumpCount > 0) {
            formattedLine.append(noJumpCount);
            formattedLine.append("\r\nNo jumps");
         }
         else if (jumpCount > 0) {
            formattedLine.append("title='Jumps: ").append(jumpCount);
         }
         else {
            formattedLine.append('0');
         }
      }
      else {
         formattedLine.append(noJumpCount);
      }

      formattedLine.append("' ");
   }

   private void appendEndTag()
   {
      formattedLine.append("</span>");

      if (withCallPoints && !segmentData.getCallPoints().isEmpty()) {
         new ListOfCallPoints().insertListOfCallPoints(formattedLine, segmentData.getCallPoints());
      }
   }
}
