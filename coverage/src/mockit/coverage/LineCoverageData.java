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

/**
 * Coverage data gathered for a single line of code in a source file.
 */
public final class LineCoverageData implements Serializable
{
   private static final long serialVersionUID = -6233980722802474992L;
   
   private boolean unreachable;
   private List<BranchCoverageData> branches;
   private transient List<String> sourceElements;
   private List<CallPoint> callPoints;
   private int executionCount;

   int addBranch(int jumpInsnIndex)
   {
      if (branches == null) {
         branches = new ArrayList<BranchCoverageData>(4);
      }

      BranchCoverageData branchData = new BranchCoverageData(jumpInsnIndex);
      branches.add(branchData);

      return branches.size() - 1;
   }

   int addSourceElement(String sourceElement)
   {
      if (sourceElements == null) {
         sourceElements = new ArrayList<String>(8);
      }

      if (sourceElement.length() > 0) {
         sourceElements.add(sourceElement);
      }

      return sourceElements.size() - 1;
   }

   BranchCoverageData getBranchData(int branchIndex)
   {
      return branches.get(branchIndex);
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

   void registerExecution(int branchIndex, boolean jumped, CallPoint callPoint)
   {
      BranchCoverageData branchData = branches.get(branchIndex);

      if (jumped) {
         branchData.registerJumpExecution(callPoint);
      }
      else {
         branchData.registerNoJumpExecution(callPoint);
      }
   }

   public boolean containsBranches()
   {
      return branches != null;
   }

   public List<BranchCoverageData> getBranches()
   {
      return branches == null ?
         Collections.<BranchCoverageData>emptyList() : Collections.unmodifiableList(branches);
   }

   public List<String> getSourceElements()
   {
      return sourceElements == null ?
         Collections.<String>emptyList() : Collections.unmodifiableList(sourceElements);
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

   public void markAsUnreachable()
   {
      unreachable = true;
   }

   public int getCoveragePercentage()
   {
      if (unreachable) {
         return 100;
      }

      if (executionCount == 0) {
         return 0;
      }

      if (branches == null) {
         return 100;
      }

      int branchesCovered = 0;

      for (BranchCoverageData branch : branches) {
         if (branch.isCovered()) {
            branchesCovered++;
         }
      }

      return 100 * branchesCovered / branches.size();
   }
}
