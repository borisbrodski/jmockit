/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
