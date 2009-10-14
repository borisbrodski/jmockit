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

import java.util.*;

/**
 * Coverage data gathered for a branch inside a line of source code.
 */
public final class BranchCoverageData
{
   private boolean unreachable;
   private final int jumpInsnIndex;
   private int jumpTargetInsnIndex = -1;
   private int noJumpTargetInsnIndex = -1;
   private List<CallPoint> jumpCallPoints;
   private List<CallPoint> noJumpCallPoints;
   private int jumpExecutionCount;
   private int noJumpExecutionCount;

   BranchCoverageData(int jumpInsnIndex)
   {
      this.jumpInsnIndex = jumpInsnIndex;
   }

   void setJumpTargetInsnIndex(int jumpTargetInsnIndex)
   {
      this.jumpTargetInsnIndex = jumpTargetInsnIndex;
   }

   void setNoJumpTargetInsnIndex(int noJumpTargetInsnIndex)
   {
      this.noJumpTargetInsnIndex = noJumpTargetInsnIndex;
   }

   void registerJumpExecution(CallPoint callPoint)
   {
      jumpExecutionCount++;

      if (callPoint != null) {
         if (jumpCallPoints == null) {
            jumpCallPoints = new ArrayList<CallPoint>();
         }

         jumpCallPoints.add(callPoint);
      }
   }

   void registerNoJumpExecution(CallPoint callPoint)
   {
      noJumpExecutionCount++;

      if (callPoint != null) {
         if (noJumpCallPoints == null) {
            noJumpCallPoints = new ArrayList<CallPoint>();
         }

         noJumpCallPoints.add(callPoint);
      }
   }

   public int getJumpInsnIndex()
   {
      return jumpInsnIndex;
   }

   public int getJumpTargetInsnIndex()
   {
      return jumpTargetInsnIndex;
   }

   public int getNoJumpTargetInsnIndex()
   {
      return noJumpTargetInsnIndex;
   }

   public int getJumpExecutionCount()
   {
      return jumpExecutionCount;
   }

   public int getNoJumpExecutionCount()
   {
      return noJumpExecutionCount;
   }

   public List<CallPoint> getJumpCallPoints()
   {
      return jumpCallPoints == null ?
         Collections.<CallPoint>emptyList() : Collections.unmodifiableList(jumpCallPoints);
   }

   public List<CallPoint> getNoJumpCallPoints()
   {
      return noJumpCallPoints == null ?
         Collections.<CallPoint>emptyList() : Collections.unmodifiableList(noJumpCallPoints);
   }

   public boolean isCovered()
   {
      return
         unreachable ||
         (jumpTargetInsnIndex < 0 || jumpExecutionCount > 0) &&
         (noJumpTargetInsnIndex < 0 || noJumpExecutionCount > 0);
   }

   public void markAsUnreachable()
   {
      unreachable = true;
   }
}
