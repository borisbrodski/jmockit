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
   private static final String EOL = System.getProperty("line.separator");

   private final boolean withCallPoints;
   private final StringBuilder formattedLine = new StringBuilder(200);
   private LineCoverageData lineData;

   LineCoverageFormatter(boolean withCallPoints)
   {
      this.withCallPoints = withCallPoints;
   }

   String format(int line, LineCoverageData lineData, LineElement initialElement)
   {
      this.lineData = lineData;

      formattedLine.setLength(0);
      formattedLine.append("><pre class='prettyprint");

      if (lineData.containsSegments()) {
         formatLineWithMultipleSegments(line, initialElement);
      }
      else {
         formatLineWithSingleSegment(line, initialElement);
      }

      appendClosingTags();

      return formattedLine.toString();
   }

   private void formatLineWithMultipleSegments(int line, LineElement initialElement)
   {
      formattedLine.append(" withBranches'>");

      new LineSegmentsFormatter(withCallPoints, line, formattedLine).formatBranches(
         lineData.getSegments(), initialElement);
   }

   private void formatLineWithSingleSegment(int line, LineElement initialElement)
   {
      formattedLine.append(" covered");

      if (withCallPoints) {
         formattedLine.append(" withCallPoints' onclick='showHide(this)");
      }

      formattedLine.append("' id='l").append(line).append("s0'>").append(initialElement.toString());
   }

   private void appendClosingTags()
   {
      formattedLine.append("</pre>");

      if (withCallPoints) {
         formattedLine.append(EOL);
         new ListOfCallPoints().insertListOfCallPoints(formattedLine, lineData.getCallPoints());
         formattedLine.append("      ");
      }

      formattedLine.append("</td>").append(EOL);
   }
}
