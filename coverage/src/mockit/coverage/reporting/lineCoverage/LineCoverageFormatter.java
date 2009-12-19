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
