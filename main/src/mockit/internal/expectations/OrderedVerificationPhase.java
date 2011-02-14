/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.internal.expectations.invocation.*;

public final class OrderedVerificationPhase extends VerificationPhase
{
   private final int expectationCount;
   private boolean unverifiedExpectationsLeftBehind;
   private boolean unverifiedExpectationsFixed;
   private int replayIndex;
   private int indexIncrement;

   OrderedVerificationPhase(
      RecordAndReplayExecution recordAndReplay,
      List<Expectation> expectationsInReplayOrder, List<Object[]> invocationArgumentsInReplayOrder)
   {
      super(recordAndReplay, expectationsInReplayOrder, invocationArgumentsInReplayOrder);
      expectationCount = expectationsInReplayOrder.size();
      indexIncrement = 1;
   }

   @Override
   protected void findNonStrictExpectation(Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      int i = replayIndex;

      while (i >= 0 && i < expectationCount) {
         Expectation expectation = expectationsInReplayOrder.get(i);
         i += indexIncrement;

         if (matches(mock, mockClassDesc, mockNameAndDesc, args, expectation)) {
            currentExpectation = expectation;
            i += 1 - indexIncrement;
            indexIncrement = 1;

            if (argMatchers != null) {
               expectation.invocation.arguments.setMatchers(argMatchers);
            }

            replayIndex = i;
            break;
         }

         if (!unverifiedExpectationsFixed) {
            unverifiedExpectationsLeftBehind = true;
         }
         else if (indexIncrement > 0) {
            recordAndReplay.setErrorThrown(expectation.invocation.errorForUnexpectedInvocation());
            replayIndex = i;
            break;
         }
      }
   }

   public void fixPositionOfUnverifiedExpectations()
   {
      if (unverifiedExpectationsLeftBehind) {
         throw new AssertionError("Unexpected invocations before" + currentExpectation.invocation);
      }

      if (replayIndex >= expectationCount) {
         throw new AssertionError("No unverified invocations left");
      }

      replayIndex = expectationCount - 1;
      indexIncrement = -1;
      unverifiedExpectationsFixed = true;
   }

   @Override
   public void handleInvocationCountConstraint(int minInvocations, int maxInvocations)
   {
      ExpectedInvocation invocation = currentExpectation.invocation;
      Object mock = invocation.instance;
      String mockClassDesc = invocation.getClassDesc();
      String mockNameAndDesc = invocation.getMethodNameAndDescription();
      Object[] args = invocation.arguments.getValues();
      argMatchers = invocation.arguments.getMatchers();
      int invocationCount = 1;

      while (replayIndex < expectationCount) {
         Expectation nextExpectation = expectationsInReplayOrder.get(replayIndex);

         if (matches(mock, mockClassDesc, mockNameAndDesc, args, nextExpectation)) {
            invocationCount++;

            if (invocationCount > maxInvocations) {
               if (maxInvocations >= 0 && numberOfIterations == 1) {
                  pendingError = nextExpectation.invocation.errorForUnexpectedInvocation();
                  return;
               }

               break;
            }

            replayIndex++;
         }
         else {
            break;
         }
      }

      argMatchers = null;

      int n = minInvocations - invocationCount;

      if (n > 0) {
         pendingError = invocation.errorForMissingInvocations(n);
         return;
      }

      if (maxInvocations >= 0) {
         //noinspection ReuseOfLocalVariable
         n = currentExpectation.constraints.invocationCount - maxInvocations * numberOfIterations;

         if (n > 0) {
            pendingError = invocation.errorForUnexpectedInvocations(n);
            return;
         }
      }

      pendingError = null;
   }

   @Override
   public void applyHandlerForEachInvocation(Object invocationHandler)
   {
      if (pendingError != null) {
         return;
      }

      getCurrentExpectation();
      InvocationHandler handler = new InvocationHandler(invocationHandler);
      int i = expectationsInReplayOrder.indexOf(currentExpectation);

      while (i < expectationCount) {
         Expectation expectation = expectationsInReplayOrder.get(i);
         Object[] args = invocationArgumentsInReplayOrder.get(i);

         if (!evaluateInvocationHandlerIfExpectationMatchesCurrent(expectation, args, handler, i)) {
            break;
         }

         i++;
      }
   }

   @Override
   protected AssertionError endVerification()
   {
      if (pendingError != null) {
         return pendingError;
      }

      if (
         unverifiedExpectationsFixed && indexIncrement > 0 && replayIndex < expectationCount &&
         currentExpectation != null
      ) {
         return new AssertionError("Unexpected invocations after" + currentExpectation.invocation);
      }

      AssertionError error = verifyMultipleIterations();

      if (error != null) {
         return error;
      }

      return super.endVerification();
   }

   private AssertionError verifyMultipleIterations()
   {
      int n = expectationsVerified.size();

      for (int i = 1; i < numberOfIterations; i++) {
         AssertionError error = verifyNextIterationOfWholeBlockOfInvocations(n);

         if (error != null) {
            return error;
         }
      }

      return null;
   }

   private AssertionError verifyNextIterationOfWholeBlockOfInvocations(int n)
   {
      for (int i = 0; i < n; i++) {
         Expectation verified = expectationsVerified.get(i);
         ExpectedInvocation invocation = verified.invocation;

         argMatchers = invocation.arguments.getMatchers();
         handleInvocation(
            invocation.instance, 0, invocation.getClassDesc(), invocation.getMethodNameAndDescription(), false,
            invocation.arguments.getValues());

         AssertionError testFailure = recordAndReplay.getErrorThrown();

         if (testFailure != null) {
            return testFailure;
         }
      }

      return null;
   }
}
