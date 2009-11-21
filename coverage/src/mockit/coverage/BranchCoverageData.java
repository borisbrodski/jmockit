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
 * Coverage data gathered for a branch inside a line of source code.
 */
public final class BranchCoverageData implements Serializable
{
   private static final long serialVersionUID = 1003335601845442606L;

   // Static data:
   public final transient Label startLabel;
   private boolean unreachable;

   // Runtime data (and static if any execution count is -1, meaning lack of the jump target):
   private int jumpExecutionCount;
   private int noJumpExecutionCount;
   private List<CallPoint> callPoints;

   BranchCoverageData(Label startLabel)
   {
      this.startLabel = startLabel;
      jumpExecutionCount = -1;
      noJumpExecutionCount = -1;
   }

   public boolean isUnreachable()
   {
      return unreachable;
   }

   public void markAsUnreachable()
   {
      unreachable = true;
   }

   void setHasJumpTarget()
   {
      jumpExecutionCount = 0;
   }

   void setHasNoJumpTarget()
   {
      noJumpExecutionCount = 0;
   }

   void registerJumpExecution(CallPoint callPoint)
   {
      assert jumpExecutionCount >= 0 : "Illegal registerJumpExecution";
      jumpExecutionCount++;
      addCallPointIfAny(callPoint);
   }

   private void addCallPointIfAny(CallPoint callPoint)
   {
      if (callPoint != null) {
         if (callPoints == null) {
            callPoints = new ArrayList<CallPoint>();
         }

         callPoints.add(callPoint);
      }
   }

   void registerNoJumpExecution(CallPoint callPoint)
   {
      assert noJumpExecutionCount >= 0 : "Illegal registerNoJumpExecution";
      noJumpExecutionCount++;
      addCallPointIfAny(callPoint);
   }

   public boolean hasJumpTarget()
   {
      return jumpExecutionCount >= 0;
   }

   public boolean hasNoJumpTarget()
   {
      return noJumpExecutionCount >= 0;
   }

   public boolean isNonEmpty()
   {
      return hasJumpTarget() || hasNoJumpTarget();
   }

   public int getJumpExecutionCount()
   {
      return jumpExecutionCount;
   }

   public int getNoJumpExecutionCount()
   {
      return noJumpExecutionCount;
   }

   public List<CallPoint> getCallPoints()
   {
      return callPoints;
   }

   public boolean isCovered()
   {
      return unreachable || jumpExecutionCount != 0 && noJumpExecutionCount != 0;
   }

   void addCountsFromPreviousMeasurement(BranchCoverageData previousData)
   {
      jumpExecutionCount += previousData.jumpExecutionCount;
      noJumpExecutionCount += previousData.noJumpExecutionCount;
      callPoints = addPreviousCallPoints(callPoints, previousData.callPoints);
   }

   private List<CallPoint> addPreviousCallPoints(List<CallPoint> current, List<CallPoint> previous)
   {
      if (previous != null) {
         if (current != null) {
            current.addAll(0, previous);
         }
         else {
            return previous;
         }
      }

      return current;
   }
}
