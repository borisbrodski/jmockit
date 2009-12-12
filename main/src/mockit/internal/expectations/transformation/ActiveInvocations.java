/*
 * JMockit Expectations & Verifications
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
package mockit.internal.expectations.transformation;

import mockit.external.hamcrest.core.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class ActiveInvocations
{
   private static final IsAnything<?> MATCHES_ANYTHING = new IsAnything();

   public static void addArgMatcher()
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();

         if (currentPhase != null) {
            currentPhase.addArgMatcher(MATCHES_ANYTHING);
         }
      }
   }

   public static void moveArgMatcher(int originalMatcherIndex, int toIndex)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();

         if (currentPhase != null) {
            currentPhase.moveArgMatcher(originalMatcherIndex, toIndex);
         }
      }
   }

   public static void addResult(Object result)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         Expectation expectation = instance.getRecordPhase().getCurrentExpectation();

         if (result instanceof Throwable) {
            expectation.getResults().addThrowable((Throwable) result);
         }
         else {
            expectation.addReturnValueOrValues(result);
         }
      }
   }

   public static void times(int n)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();
         currentPhase.handleInvocationCountConstraint(n, n);
      }
   }

   public static void minTimes(int n)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();
         currentPhase.handleInvocationCountConstraint(n, -1);
      }
   }

   public static void maxTimes(int n)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();
         currentPhase.setMaxInvocationCount(n);
      }
   }

   public static void endInvocations()
   {
      TestRun.enterNoMockingZone();

      try {
         RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(true);

         if (instance != null) {
            instance.endInvocations();
         }
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
