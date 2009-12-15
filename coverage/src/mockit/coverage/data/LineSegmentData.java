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

import java.io.*;
import java.util.*;

import mockit.coverage.*;

public class LineSegmentData implements Serializable
{
   private static final long serialVersionUID = -6233980722802474992L;

   // Static data:
   boolean unreachable;

   // Runtime data:
   int executionCount;
   private List<CallPoint> callPoints;

   public final boolean isUnreachable()
   {
      return unreachable;
   }

   public final void markAsUnreachable()
   {
      unreachable = true;
   }

   void registerExecution(CallPoint callPoint)
   {
      addCallPointIfAny(callPoint);
      executionCount++;
   }

   final void addCallPointIfAny(CallPoint callPoint)
   {
      if (callPoint != null) {
         if (callPoints == null) {
            callPoints = new ArrayList<CallPoint>();
         }

         callPoints.add(callPoint);
      }
   }

   public final int getExecutionCount()
   {
      return executionCount;
   }

   public final boolean containsCallPoints()
   {
      return callPoints != null;
   }

   public final List<CallPoint> getCallPoints()
   {
      return callPoints == null ? Collections.<CallPoint>emptyList() : callPoints;
   }

   public final boolean isCovered()
   {
      return unreachable || executionCount > 0;
   }

   final void addExecutionCountAndCallPointsFromPreviousTestRun(LineSegmentData previousData)
   {
      executionCount += previousData.executionCount;

      if (previousData.containsCallPoints()) {
         if (containsCallPoints()) {
            callPoints.addAll(0, previousData.callPoints);
         }
         else {
            callPoints = previousData.callPoints;
         }
      }
   }
}