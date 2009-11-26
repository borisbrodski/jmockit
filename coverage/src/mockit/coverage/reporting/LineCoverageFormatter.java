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

import mockit.coverage.*;

final class LineCoverageFormatter
{
   private static final String EOL = System.getProperty("line.separator");

   private final boolean withCallPoints;
   private final StringBuilder formattedLine = new StringBuilder(200);

   LineCoverageFormatter(boolean withCallPoints)
   {
      this.withCallPoints = withCallPoints;
   }

   String format(int lineNo, LineCoverageData lineData, LineSegment initialSegment)
   {
      formattedLine.setLength(0);

      if (lineData.containsSegments()) {
         formatLineWithBranches(lineNo, lineData, initialSegment);
      }
      else {
         formatLineWithoutBranches(lineNo, lineData, initialSegment);
      }

      formattedLine.append("      </td>").append(EOL);

      return formattedLine.toString();
   }

   private void formatLineWithBranches(
      int lineNo, LineCoverageData lineData, LineSegment initialSegment)
   {
      // TODO: make line segment formatting work
//         formattedLine.append("      <td class='withBranches'>").append(EOL);
//         new LineSegmentsFormatter(withCallPoints, formattedLine).formatBranches(
//            lineData, initialSegment);
      formattedLine.append("      <td id='").append(lineNo).append("'><pre class='");
      formattedLine.append(lineData.isFullyCovered() ? "covered" : "partiallyCovered");

      if (withCallPoints) {
         formattedLine.append(" withCallPoints' onclick='showHide(this)");
      }

      formattedLine.append("'>");
      formattedLine.append(initialSegment.toString());
      formattedLine.append("</pre>").append(EOL);

      if (withCallPoints) {
         new ListOfCallPoints().insertListOfCallPoints(formattedLine, lineData.getCallPoints());
      }
   }

   private void formatLineWithoutBranches(
      int lineNo, LineCoverageData lineData, LineSegment initialSegment)
   {
      formattedLine.append("      <td id='").append(lineNo).append("'><pre class='covered");

      if (withCallPoints) {
         formattedLine.append(" withCallPoints' onclick='showHide(this)");
      }

      formattedLine.append("'>");
      formattedLine.append(initialSegment.toString());
      formattedLine.append("</pre>").append(EOL);

      if (withCallPoints) {
         new ListOfCallPoints().insertListOfCallPoints(formattedLine, lineData.getCallPoints());
      }
   }
}
