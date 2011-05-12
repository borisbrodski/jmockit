/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
         instance.getRecordPhase().addResult(result);
      }
   }

   public static void setHandler(Object handler)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         TestOnlyPhase currentPhase = instance.getCurrentTestOnlyPhase();
         currentPhase.applyHandlerForEachInvocation(handler);
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

   public static void setErrorMessage(CharSequence customMessage)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);

      if (instance != null) {
         instance.getCurrentTestOnlyPhase().setCustomErrorMessage(customMessage);
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
