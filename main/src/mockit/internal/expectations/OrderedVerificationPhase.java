/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.internal.expectations.invocation.*;

public final class OrderedVerificationPhase extends BaseVerificationPhase
{
   private final int expectationCount;
   private ExpectedInvocation unverifiedInvocationLeftBehind;
   private ExpectedInvocation unverifiedInvocationPrecedingVerifiedOnesLeftBehind;
   private boolean unverifiedExpectationsFixed;
   private int indexIncrement;

   OrderedVerificationPhase(
      RecordAndReplayExecution recordAndReplay,
      List<Expectation> expectationsInReplayOrder, List<Object[]> invocationArgumentsInReplayOrder)
   {
      super(recordAndReplay, new ArrayList<Expectation>(expectationsInReplayOrder), invocationArgumentsInReplayOrder);
      discardExpectationsAndArgumentsAlreadyVerified();
      expectationCount = expectationsInReplayOrder.size();
      indexIncrement = 1;
   }

   private void discardExpectationsAndArgumentsAlreadyVerified()
   {
      for (VerifiedExpectation verified : recordAndReplay.executionState.verifiedExpectations) {
         int i = expectationsInReplayOrder.indexOf(verified.expectation);

         if (i >= 0) {
            expectationsInReplayOrder.set(i, null);
         }
      }
   }

   @Override
   protected void findNonStrictExpectation(Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      int i = replayIndex;

      while (i >= 0 && i < expectationCount) {
         Expectation replayExpectation = expectationsInReplayOrder.get(i);
         Object[] replayArgs = invocationArgumentsInReplayOrder.get(i);

         i += indexIncrement;

         if (replayExpectation == null) {
            continue;
         }

         if (recordAndReplay.executionState.isToBeMatchedOnInstance(mock, mockNameAndDesc)) {
            matchInstance = true;
         }

         if (matches(mock, mockClassDesc, mockNameAndDesc, args, replayExpectation, replayArgs)) {
            currentExpectation = replayExpectation;
            i += 1 - indexIncrement;
            indexIncrement = 1;
            replayIndex = i;
            currentVerification.constraints.invocationCount++;
            break;
         }

         if (!unverifiedExpectationsFixed) {
            unverifiedInvocationLeftBehind = replayExpectation.invocation;
         }
         else if (indexIncrement > 0) {
            recordAndReplay.setErrorThrown(replayExpectation.invocation.errorForUnexpectedInvocation());
            replayIndex = i;
            break;
         }
      }
   }

   public void fixPositionOfUnverifiedExpectations()
   {
      if (unverifiedInvocationLeftBehind != null) {
         throw
            currentExpectation == null ?
               unverifiedInvocationLeftBehind.errorForUnexpectedInvocation() :
               unverifiedInvocationLeftBehind.errorForUnexpectedInvocationBeforeAnother(currentExpectation.invocation);
      }

      int indexOfLastUnverified = indexOfLastUnverifiedExpectation();

      if (indexOfLastUnverified >= 0) {
         replayIndex = indexOfLastUnverified;
         indexIncrement = -1;
         unverifiedExpectationsFixed = true;
      }
   }

   private int indexOfLastUnverifiedExpectation()
   {
      for (int i = expectationCount - 1; i >= 0; i--) {
         if (expectationsInReplayOrder.get(i) != null) {
            return i;
         }
      }

      return -1;
   }

   @SuppressWarnings("OverlyComplexMethod")
   @Override
   public void handleInvocationCountConstraint(int minInvocations, int maxInvocations)
   {
      if (pendingError != null && minInvocations > 0) {
         return;
      }

      ExpectedInvocation invocation = currentVerification.invocation;
      argMatchers = invocation.arguments.getMatchers();
      int invocationCount = 1;

      while (replayIndex < expectationCount) {
         Expectation replayExpectation = expectationsInReplayOrder.get(replayIndex);

         if (replayExpectation != null && matchesCurrentVerification(replayExpectation)) {
            invocationCount++;
            currentVerification.constraints.invocationCount++;

            if (invocationCount > maxInvocations) {
               if (maxInvocations >= 0 && numberOfIterations <= 1) {
                  pendingError = replayExpectation.invocation.errorForUnexpectedInvocation();
                  return;
               }

               break;
            }
         }
         else if (invocationCount >= minInvocations) {
            break;
         }

         replayIndex++;
      }

      argMatchers = null;

      int n = minInvocations - invocationCount;

      if (n > 0) {
         pendingError = invocation.errorForMissingInvocations(n);
         return;
      }

      pendingError = verifyMaxInvocations(maxInvocations);
   }

   private boolean matchesCurrentVerification(Expectation replayExpectation)
   {
      ExpectedInvocation invocation = currentVerification.invocation;
      Object mock = invocation.instance;
      String mockClassDesc = invocation.getClassDesc();
      String mockNameAndDesc = invocation.getMethodNameAndDescription();
      Object[] args = invocation.arguments.getValues();
      matchInstance = invocation.matchInstance;

      if (recordAndReplay.executionState.isToBeMatchedOnInstance(mock, mockNameAndDesc)) {
         matchInstance = true;
      }

      Object[] replayArgs = invocationArgumentsInReplayOrder.get(replayIndex);

      return matches(mock, mockClassDesc, mockNameAndDesc, args, replayExpectation, replayArgs);
   }

   private Error verifyMaxInvocations(int maxInvocations)
   {
      if (maxInvocations >= 0) {
         int multiplier = numberOfIterations <= 1 ? 1 : numberOfIterations;
         int n = currentVerification.constraints.invocationCount - maxInvocations * multiplier;

         if (n > 0) {
            Object[] replayArgs = invocationArgumentsInReplayOrder.get(replayIndex - 1);
            return currentVerification.invocation.errorForUnexpectedInvocations(replayArgs, n);
         }
      }

      return null;
   }

   @Override
   public void applyHandlerForEachInvocation(Object invocationHandler)
   {
      if (pendingError != null) {
         return;
      }

      getCurrentExpectation();
      InvocationHandlerResult handler = new InvocationHandlerResult(invocationHandler);
      int i = expectationsInReplayOrder.indexOf(currentExpectation);

      while (i < expectationCount) {
         Expectation expectation = expectationsInReplayOrder.get(i);

         if (expectation != null) {
            Object[] args = invocationArgumentsInReplayOrder.get(i);

            if (!evaluateInvocationHandlerIfExpectationMatchesCurrent(expectation, args, handler, i)) {
               break;
            }
         }

         i++;
      }
   }

   @Override
   protected Error endVerification()
   {
      if (pendingError != null) {
         return pendingError;
      }

      if (
         unverifiedExpectationsFixed && indexIncrement > 0 && currentExpectation != null &&
         replayIndex <= indexOfLastUnverifiedExpectation()
      ) {
         ExpectedInvocation unexpectedInvocation = expectationsInReplayOrder.get(replayIndex).invocation;
         return unexpectedInvocation.errorForUnexpectedInvocationAfterAnother(currentExpectation.invocation);
      }

      if (unverifiedInvocationPrecedingVerifiedOnesLeftBehind != null) {
         return unverifiedInvocationPrecedingVerifiedOnesLeftBehind.errorForUnexpectedInvocation();
      }

      Error error = verifyRemainingIterations();

      if (error != null) {
         return error;
      }

      return super.endVerification();
   }

   private Error verifyRemainingIterations()
   {
      int expectationsVerifiedInFirstIteration = recordAndReplay.executionState.verifiedExpectations.size();

      for (int i = 1; i < numberOfIterations; i++) {
         Error error = verifyNextIterationOfWholeBlockOfInvocations(expectationsVerifiedInFirstIteration);

         if (error != null) {
            return error;
         }
      }

      return null;
   }

   private Error verifyNextIterationOfWholeBlockOfInvocations(int expectationsVerifiedInFirstIteration)
   {
      List<VerifiedExpectation> expectationsVerified = recordAndReplay.executionState.verifiedExpectations;

      for (int i = 0; i < expectationsVerifiedInFirstIteration; i++) {
         VerifiedExpectation verifiedExpectation = expectationsVerified.get(i);
         ExpectedInvocation invocation = verifiedExpectation.expectation.invocation;

         argMatchers = verifiedExpectation.argMatchers;
         handleInvocation(
            invocation.instance, 0, invocation.getClassDesc(), invocation.getMethodNameAndDescription(), null, null,
            false, verifiedExpectation.arguments);

         Error testFailure = recordAndReplay.getErrorThrown();

         if (testFailure != null) {
            return testFailure;
         }
      }

      return null;
   }

   @Override
   boolean shouldDiscardInformationAboutVerifiedInvocationOnceUsed() { return true; }

   public void checkOrderOfVerifiedInvocations(BaseVerificationPhase verificationPhase)
   {
      if (verificationPhase instanceof OrderedVerificationPhase) {
         throw new IllegalArgumentException("Invalid use of ordered verification block");
      }

      UnorderedVerificationPhase previousVerification = (UnorderedVerificationPhase) verificationPhase;

      if (previousVerification.verifiedExpectations.isEmpty()) {
         return;
      }

      if (indexIncrement > 0) {
         checkForwardOrderOfVerifiedInvocations(previousVerification);
      }
      else {
         checkBackwardOrderOfVerifiedInvocations(previousVerification);
      }
   }

   private void checkForwardOrderOfVerifiedInvocations(UnorderedVerificationPhase previousVerification)
   {
      int maxReplayIndex = replayIndex - 1;

      for (VerifiedExpectation verified : previousVerification.verifiedExpectations) {
         if (verified.replayIndex < replayIndex) {
            ExpectedInvocation unexpectedInvocation = verified.expectation.invocation;
            throw currentExpectation == null ?
               unexpectedInvocation.errorForUnexpectedInvocationFoundBeforeAnother() :
               unexpectedInvocation.errorForUnexpectedInvocationFoundBeforeAnother(currentExpectation.invocation);
         }

         if (verified.replayIndex > maxReplayIndex) {
            maxReplayIndex = verified.replayIndex;
         }
      }

      for (int i = replayIndex; i < maxReplayIndex; i++) {
         Expectation expectation = expectationsInReplayOrder.get(i);

         if (expectation != null) {
            unverifiedInvocationPrecedingVerifiedOnesLeftBehind = expectation.invocation;
            break;
         }
      }

      replayIndex = maxReplayIndex + 1;
      currentExpectation = replayIndex < expectationCount ? expectationsInReplayOrder.get(replayIndex) : null;
   }

   private void checkBackwardOrderOfVerifiedInvocations(UnorderedVerificationPhase previousVerification)
   {
      int indexOfLastUnverified = indexOfLastUnverifiedExpectation();

      if (indexOfLastUnverified >= 0) {
         VerifiedExpectation firstVerified = previousVerification.firstExpectationVerified();

         if (firstVerified.replayIndex != indexOfLastUnverified + 1) {
            Expectation lastUnverified = expectationsInReplayOrder.get(indexOfLastUnverified);
            Expectation after = firstVerified.expectation;
            throw lastUnverified.invocation.errorForUnexpectedInvocationAfterAnother(after.invocation);
         }
      }
   }
}
