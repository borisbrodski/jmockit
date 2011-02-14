/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.LineElement;

final class LineCoverageFormatter
{
   private final StringBuilder formattedLine;
   private final LineSegmentsFormatter segmentsFormatter;
   private final ListOfCallPoints listOfCallPoints;
   private LineCoverageData lineData;

   LineCoverageFormatter(boolean withCallPoints)
   {
      formattedLine = new StringBuilder(200);
      segmentsFormatter = new LineSegmentsFormatter(withCallPoints, formattedLine);
      listOfCallPoints = withCallPoints ? new ListOfCallPoints() : null;
   }

   String format(int lineNum, LineCoverageData lineData, LineElement initialElement)
   {
      this.lineData = lineData;

      formattedLine.setLength(0);
      formattedLine.append("<pre class='prettyprint");

      if (lineData.containsBranches()) {
         formatLineWithMultipleSegments(lineNum, initialElement);
      }
      else {
         formatLineWithSingleSegment(lineNum, initialElement);
      }

      return formattedLine.toString();
   }

   private void formatLineWithMultipleSegments(int lineNum, LineElement initialElement)
   {
      formattedLine.append(" jmp'>");
      segmentsFormatter.formatSegments(lineNum, lineData, initialElement);
   }

   private void formatLineWithSingleSegment(int line, LineElement initialElement)
   {
      formattedLine.append(lineData.isCovered() ? " covered" : " uncovered");

      boolean lineWithCallPoints = listOfCallPoints != null && lineData.getExecutionCount() > 0;

      if (lineWithCallPoints) {
         formattedLine.append(" cp' onclick='showHide(this)");
      }

      formattedLine.append("' id='l").append(line).append("s0'>").append(initialElement.toString());
      formattedLine.append("</pre>");

      if (lineWithCallPoints) {
         listOfCallPoints.insertListOfCallPoints(lineData.getCallPoints());
         formattedLine.append(listOfCallPoints.getContents());
      }
   }
}
