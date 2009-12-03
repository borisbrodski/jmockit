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
package mockit.coverage.reporting.codeCoverage;

import java.util.*;

import mockit.coverage.*;
import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.LineSegment;

final class LineSegmentsFormatter
{
   private static final String EOL = System.getProperty("line.separator");

   private final boolean withCallPoints;
   private final StringBuilder formattedLine;

   LineSegmentsFormatter(boolean withCallPoints, StringBuilder formattedLine)
   {
      this.withCallPoints = withCallPoints;
      this.formattedLine = formattedLine;
   }

   void formatBranches(LineCoverageData lineData, LineSegment initialSegment)
   {
      List<String> sourceElements = null;// lineData.getSourceElements();
      LineSegment[][] segmentPairs =
         buildSegmentPairsForSourceElements(sourceElements, initialSegment);

      List<BranchCoverageData> branches = lineData.getSegments();
      FormattedSegments formattedSegments =
         buildFormattedSegments(lineData, branches, segmentPairs);

      formattedSegments.appendToFormattedLine();
   }

   private LineSegment[][] buildSegmentPairsForSourceElements(
      List<String> sourceElements, LineSegment initialSegment)
   {
      int pairCount = sourceElements.size();
      LineSegment[][] segmentPairs = new LineSegment[pairCount][2];
      LineSegment segment = initialSegment;

      for (int i = 0; segment != null && i < pairCount; i++) {
         String sourceElement = sourceElements.get(i);

         while (!sameSourceElement(segment, sourceElement)) {
            if (i > 0) {
               segmentPairs[i - 1][1] = segment;
            }

            segment = segment.getNext();

            if (segment == null) {
               if (i < pairCount - 1) {
                  throw new IllegalStateException("Missing line segment for pair " + i);
               }

               return segmentPairs;
            }
         }

         segmentPairs[i][0] = segment;
         segment = segment.getNext();
      }

      segmentPairs[0][0] = initialSegment;
      segmentPairs[pairCount - 1][1] = null;

      return segmentPairs;
   }

   private boolean sameSourceElement(LineSegment segment, String sourceElement)
   {
      if (segment.isCode()) {
         if (
            "*".equals(sourceElement) || segment.getText().equals(sourceElement) ||
            ("if".equals(sourceElement) || LineSegment.isRelationalOperator(sourceElement)) &&
            segment.containsConditionalOperator()
         ) {
            return true;
         }
      }

      return false;
   }

   private FormattedSegments buildFormattedSegments(
      LineCoverageData lineData, List<BranchCoverageData> branches, LineSegment[][] segmentPairs)
   {
      FormattedSegments formattedSegments = new FormattedSegments();

      formattedSegments.addSegment(
         segmentPairs[0], lineData.getExecutionCount(), lineData.getCallPoints());

      for (BranchCoverageData branchData : branches) {
         LineSegment[] segmentPair = segmentPairs[0];
         formattedSegments.addSegment(
            segmentPair, lineData.getExecutionCount(), lineData.getCallPoints());

         if (branchData.hasNoJumpTarget()) {
            segmentPair = null;//segmentPairs[branchData.getNoJumpTargetInsnIndex()];
            formattedSegments.addSegment(
               segmentPair, branchData.getNoJumpExecutionCount(), branchData.getCallPoints());
         }

         if (branchData.hasJumpTarget()) {
            segmentPair = null;//segmentPairs[branchData.getJumpTargetInsnIndex()];
            formattedSegments.addSegment(
               segmentPair, branchData.getJumpExecutionCount(), branchData.getCallPoints());
         }
      }

      formattedSegments.adjustPairsToIncludeAllLineSegments();

      return formattedSegments;
   }

   private final class FormattedSegments
   {
      final List<FormattedSegment> segments = new ArrayList<FormattedSegment>();

      void addSegment(LineSegment[] segmentPair, int executionCount, List<CallPoint> callPoints)
      {
         LineSegment newSegmentStart = segmentPair[0];
         int newSegmentIndex = segments.size();
         int i = 0;

         for (FormattedSegment segment : segments) {
            mockit.coverage.reporting.parsing.LineSegment segmentStart = segment.segmentPair[0];

            if (segmentStart == newSegmentStart) {
               return;
            }
            else if (newSegmentStart.before(segmentStart)) {
               newSegmentIndex = i;
               break;
            }

            i++;
         }

         FormattedSegment segment = new FormattedSegment(segmentPair, executionCount, callPoints);
         segments.add(newSegmentIndex, segment);
      }

      void appendToFormattedLine()
      {
         for (FormattedSegment segment : segments) {
            segment.appendToFormattedLine();
         }
      }

      void adjustPairsToIncludeAllLineSegments()
      {
         int segmentCount = segments.size();

         for (int i = 0; i < segmentCount - 1; i++) {
            LineSegment[] pair = segments.get(i).segmentPair;
            LineSegment[] nextPair = segments.get(i + 1).segmentPair;

            while (pair[1].getNext() != nextPair[0]) {
               pair[1] = pair[1].getNext();
            }
         }
      }
   }

   private final class FormattedSegment
   {
      final LineSegment[] segmentPair;
      final int executionCount;
      final List<CallPoint> callPoints;

      FormattedSegment(LineSegment[] segmentPair, int executionCount, List<CallPoint> callPoints)
      {
         this.segmentPair = segmentPair;
         this.executionCount = executionCount;
         this.callPoints = callPoints;
      }

      void appendToFormattedLine()
      {
         appendStartTag();

         for (LineSegment segment : segmentPair[0]) {
            formattedLine.append(segment.getText());

            if (segment == segmentPair[1]) {
               break;
            }
         }

         appendEndTag();
      }

      private void appendStartTag()
      {
         formattedLine.append("        <pre ");

         String startTagAttributes;

         if (withCallPoints && executionCount > 0) {
            startTagAttributes = "onclick='showHide(this)' class='covered withCallPoints'>";
         }
         else if (executionCount > 0) {
            startTagAttributes = "class='covered'>";
         }
         else {
            startTagAttributes = "class='uncovered'>";
         }

         formattedLine.append(startTagAttributes);
      }

      private void appendEndTag()
      {
         formattedLine.append("</pre>").append(EOL);

         if (withCallPoints && executionCount > 0) {
            new ListOfCallPoints().insertListOfCallPoints(formattedLine, callPoints);
         }
      }
   }
}
