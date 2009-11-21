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
package mockit.coverage;

import java.io.*;
import java.util.*;

import org.objectweb.asm2.*;

/**
 * Coverage data gathered for a single executable line of code in a source file.
 */
public final class LineCoverageData implements Serializable
{
   private static final long serialVersionUID = -6233980722802474992L;

   // Static data:
   private boolean unreachable;
   private List<BranchCoverageData> segments;

   // Runtime data:
   private int executionCount;
   private List<CallPoint> callPoints;

   int addSegment(Label targetLabel)
   {
      if (segments == null) {
         segments = new ArrayList<BranchCoverageData>(4);
      }

      BranchCoverageData data = new BranchCoverageData(targetLabel);
      segments.add(data);

      return segments.size() - 1;
   }

   BranchCoverageData getSegmentData(int segmentIndex)
   {
      return segments.get(segmentIndex);
   }

   void registerExecution(CallPoint callPoint)
   {
      if (callPoint != null) {
         if (callPoints == null) {
            callPoints = new ArrayList<CallPoint>();
         }

         callPoints.add(callPoint);
      }

      executionCount++;
   }

   void registerExecution(int segmentIndex, boolean jumped, CallPoint callPoint)
   {
      BranchCoverageData data = segments.get(segmentIndex);

      if (jumped) {
         data.registerJumpExecution(callPoint);
      }
      else {
         data.registerNoJumpExecution(callPoint);
      }
   }

   public boolean containsSegments()
   {
      return segments != null;
   }

   public List<BranchCoverageData> getSegments()
   {
      return segments;
   }

   public boolean containsCallPoints()
   {
      return callPoints != null;
   }

   public List<CallPoint> getCallPoints()
   {
      return callPoints == null ?
         Collections.<CallPoint>emptyList() : Collections.unmodifiableList(callPoints);
   }

   public int getExecutionCount()
   {
      return executionCount;
   }

   public boolean isUnreachable()
   {
      return unreachable;
   }

   public void markAsUnreachable()
   {
      unreachable = true;
   }

   public int getNumberOfSegments()
   {
      return segments == null ? 1 : segments.size();
   }

   public int getNumberOfCoveredSegments()
   {
      if (unreachable) {
         return getNumberOfSegments();
      }

      if (executionCount == 0) {
         return 0;
      }

      if (segments == null) {
         return 1;
      }

      return segmentsCovered();
   }

   private int segmentsCovered()
   {
      int segmentsCovered = 0;

      for (BranchCoverageData segment : segments) {
         if (segment.isCovered()) {
            segmentsCovered++;
         }
      }

      return segmentsCovered;
   }

   public int getCoveragePercentage()
   {
      if (unreachable) {
         return 100;
      }

      if (executionCount == 0) {
         return 0;
      }

      if (segments == null) {
         return 100;
      }

      int segmentsCovered = segmentsCovered();

      return 100 * segmentsCovered / segments.size();
   }

   void addCountsFromPreviousMeasurement(LineCoverageData previousLineData)
   {
      executionCount += previousLineData.executionCount;

      if (previousLineData.containsCallPoints()) {
         if (containsCallPoints()) {
            callPoints.addAll(0, previousLineData.callPoints);
         }
         else {
            callPoints = previousLineData.callPoints;
         }
      }

      if (containsSegments()) {
         for (int i = 0; i < segments.size(); i++) {
            BranchCoverageData segmentData = segments.get(i);
            BranchCoverageData previousSegmentData = previousLineData.segments.get(i);

            segmentData.addCountsFromPreviousMeasurement(previousSegmentData);
         }
      }
   }
}
