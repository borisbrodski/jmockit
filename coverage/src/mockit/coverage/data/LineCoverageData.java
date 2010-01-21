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
package mockit.coverage.data;

import java.util.*;

import mockit.coverage.*;
import mockit.external.asm.*;

/**
 * Coverage data gathered for a single executable line of code in a source file.
 */
public final class LineCoverageData extends LineSegmentData
{
   private static final long serialVersionUID = -6233980722802474992L;

   // Static data:
   private List<BranchCoverageData> branches;

   public int addBranch(Label jumpSource, Label jumpTarget)
   {
      if (branches == null) {
         branches = new ArrayList<BranchCoverageData>(4);
      }

      BranchCoverageData data = new BranchCoverageData(jumpSource, jumpTarget);
      branches.add(data);

      return branches.size() - 1;
   }

   public BranchCoverageData getBranchData(int index)
   {
      return branches.get(index);
   }

   void registerExecution(int branchIndex, boolean jumped, CallPoint callPoint)
   {
      BranchCoverageData data = branches.get(branchIndex);

      if (jumped) {
         data.registerJumpExecution(callPoint);
      }
      else {
         data.registerNoJumpExecution(callPoint);
      }
   }

   public boolean containsBranches()
   {
      return branches != null;
   }

   public List<BranchCoverageData> getBranches()
   {
      return branches;
   }

   public int getNumberOfSegments()
   {
      return branches == null ? 1 : 1 + branches.size();
   }

   public int getNumberOfCoveredSegments()
   {
      if (unreachable) {
         return getNumberOfSegments();
      }

      if (executionCount == 0) {
         return 0;
      }

      if (branches == null) {
         return 1;
      }

      return getSegmentsCovered();
   }

   private int getSegmentsCovered()
   {
      int segmentsCovered = 1;

      for (BranchCoverageData branch : branches) {
         if (branch.isCovered()) {
            segmentsCovered++;
         }
      }

      return segmentsCovered;
   }

   void addCountsFromPreviousTestRun(LineCoverageData previousData)
   {
      addExecutionCountAndCallPointsFromPreviousTestRun(previousData);

      if (containsBranches()) {
         for (int i = 0; i < branches.size(); i++) {
            BranchCoverageData segmentData = branches.get(i);
            BranchCoverageData previousSegmentData = previousData.branches.get(i);

            segmentData.addCountsFromPreviousTestRun(previousSegmentData);
         }
      }
   }
}
