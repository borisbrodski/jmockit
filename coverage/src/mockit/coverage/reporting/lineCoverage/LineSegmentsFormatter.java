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

import java.util.*;

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.LineElement;

final class LineSegmentsFormatter
{
   private final ListOfCallPoints listOfCallPoints;
   private final StringBuilder line;
   private int lineNum;

   // Helper fields:
   private LineElement element;
   private int segmentIndex;
   private LineSegmentData segmentData;

   LineSegmentsFormatter(boolean withCallPoints, StringBuilder line)
   {
      listOfCallPoints = withCallPoints ? new ListOfCallPoints() : null;
      this.line = line;
   }

   void formatSegments(int lineNum, LineCoverageData lineData, LineElement initialElement)
   {
      this.lineNum = lineNum;

      List<BranchCoverageData> branchData = lineData.getBranches();
      int numSegments = 1 + branchData.size();

      element = initialElement.appendUntilNextCodeElement(line);

      segmentIndex = 0;
      segmentData = lineData;
      appendUntilFirstElementAfterNextBranchingPoint();

      for (segmentIndex = 1; element != null && segmentIndex < numSegments; segmentIndex++) {
         segmentData = branchData.get(segmentIndex - 1);

         element = element.appendUntilNextCodeElement(line);
         appendUntilFirstElementAfterNextBranchingPoint();
      }

      line.append("</pre>");

      if (listOfCallPoints != null) {
         line.append(listOfCallPoints.getContents());
      }
   }

   private void appendUntilFirstElementAfterNextBranchingPoint()
   {
      if (element != null) {
         LineElement firstElement = element;
         element = element.findNextBranchingPoint();

         appendToFormattedLine(firstElement);

         if (element != null && element.isBranchingElement()) {
            line.append(element.getText());
            element = element.getNext();
         }
      }
   }

   private void appendToFormattedLine(LineElement firstElement)
   {
      if (firstElement == element) {
         return;
      }

      appendStartTag();
      firstElement.appendAllBefore(line, element);
      appendEndTag();
   }

   private void appendStartTag()
   {
      line.append("<span id='l").append(lineNum);
      line.append('s').append(segmentIndex).append("' ");

      appendTooltipWithExecutionCounts();

      if (segmentData.isCovered()) {
         line.append("class='covered");

         if (listOfCallPoints != null) {
            line.append(" cp' onclick='showHide(this,").append(segmentIndex).append(')');
         }

         line.append("'>");
      }
      else {
         line.append("class='uncovered'>");
      }
   }

   private void appendTooltipWithExecutionCounts()
   {
      line.append("title='Executions: ").append(segmentData.getExecutionCount()).append("' ");
   }

   private void appendEndTag()
   {
      int i = line.length() - 1;

      while (Character.isWhitespace(line.charAt(i))) {
         i--;
      }
      
      line.insert(i + 1, "</span>");

      if (listOfCallPoints != null && !segmentData.getCallPoints().isEmpty()) {
         listOfCallPoints.insertListOfCallPoints(segmentData.getCallPoints());
      }
   }
}
