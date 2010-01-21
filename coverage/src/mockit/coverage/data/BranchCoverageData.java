/*
 * JMockit Coverage
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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

import mockit.coverage.*;
import mockit.external.asm.*;

/**
 * Coverage data gathered for a branch inside a line of source code.
 */
public final class BranchCoverageData extends LineSegmentData
{
   private static final long serialVersionUID = 1003335601845442606L;

   // Static data:
   public final transient Label jumpSource;
   public final transient Label jumpTarget;

   // Runtime data (and static if any execution count is -1, meaning lack of the jump target):
   private int jumpExecutionCount;

   BranchCoverageData(Label jumpSource, Label jumpTarget)
   {
      this.jumpSource = jumpSource;
      this.jumpTarget = jumpTarget;
      jumpExecutionCount = -1;
      executionCount = -1;
   }

   public void setHasJumpTarget()
   {
      jumpExecutionCount = 0;
   }

   public void setHasNoJumpTarget()
   {
      executionCount = 0;
   }

   void registerJumpExecution(CallPoint callPoint)
   {
      assert jumpExecutionCount >= 0 : "Illegal registerJumpExecution";
      jumpExecutionCount++;
      addCallPointIfAny(callPoint);
   }

   void registerNoJumpExecution(CallPoint callPoint)
   {
      assert executionCount >= 0 : "Illegal registerNoJumpExecution";
      executionCount++;
      addCallPointIfAny(callPoint);
   }

   @Override
   public boolean isCovered()
   {
      return super.isCovered() || jumpExecutionCount > 0;
   }

   @Override
   public int getExecutionCount()
   {
      return executionCount > 0 ? executionCount : jumpExecutionCount > 0 ? jumpExecutionCount : 0;
   }

   void addCountsFromPreviousTestRun(BranchCoverageData previousData)
   {
      addExecutionCountAndCallPointsFromPreviousTestRun(previousData);
      jumpExecutionCount += previousData.jumpExecutionCount;
   }
}
