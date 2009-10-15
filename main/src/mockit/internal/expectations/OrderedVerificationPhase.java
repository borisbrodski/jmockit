/*
 * JMockit Expectations
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
package mockit.internal.expectations;

import java.util.*;

public final class OrderedVerificationPhase extends VerificationPhase
{
   private final List<Expectation> expectationsInReplayOrder;
   private final int expectationCount;
   private boolean unverifiedExpectationsLeftBehind;
   private boolean unverifiedExpectationsFixed;
   private int replayIndex;
   private int indexIncrement;

   OrderedVerificationPhase(
      RecordAndReplayExecution recordAndReplay, List<Expectation> expectationsInReplayOrder)
   {
      super(recordAndReplay, expectationsInReplayOrder);
      this.expectationsInReplayOrder = expectationsInReplayOrder;
      expectationCount = expectationsInReplayOrder.size();
      indexIncrement = 1;
   }

   @Override
   protected void findNonStrictExpectation(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
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
               expectation.expectedInvocation.invocationArgMatchers = argMatchers;
            }

            replayIndex = i;
            break;
         }

         if (!unverifiedExpectationsFixed) {
            unverifiedExpectationsLeftBehind = true;
         }
         else if (indexIncrement > 0) {
            recordAndReplay.errorThrown =
               expectation.expectedInvocation.errorForUnexpectedInvocation();
            replayIndex = i;
            break;
         }
      }
   }

   public void fixPositionOfUnverifiedExpectations()
   {
      if (unverifiedExpectationsLeftBehind) {
         throw new AssertionError(
            "Unexpected invocations before" + currentExpectation.expectedInvocation);
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
      ExpectedInvocationWithMatchers invocation = currentExpectation.expectedInvocation;
      Object mock = invocation.instance;
      String mockClassDesc = invocation.classDesc;
      String mockNameAndDesc = invocation.methodNameAndDesc;
      Object[] args = invocation.invocationArgs;
      argMatchers = invocation.invocationArgMatchers;
      int invocationCount = 1;

      while (replayIndex < expectationCount) {
         Expectation nextExpectation = expectationsInReplayOrder.get(replayIndex);

         if (matches(mock, mockClassDesc, mockNameAndDesc, args, nextExpectation)) {
            invocationCount++;

            if (invocationCount > maxInvocations) {
               if (numberOfIterations == 1) {
                  throw nextExpectation.expectedInvocation.errorForUnexpectedInvocation();
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
         throw invocation.errorForMissingInvocations(n);
      }

      if (maxInvocations >= 0) {
         n = currentExpectation.constraints.invocationCount - maxInvocations * numberOfIterations;

         if (n > 0) {
            throw invocation.errorForUnexpectedInvocations(n);
         }
      }

      pendingError = null;
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
         return new AssertionError(
            "Unexpected invocations after" + currentExpectation.expectedInvocation);
      }

      verifyMultipleIterations();

      return super.endVerification();
   }

   private void verifyMultipleIterations()
   {
      int n = expectationsVerified.size();

      for (int i = 1; i < numberOfIterations; i++) {
         verifyNextIterationOfWholeBlockOfInvocations(n);
      }
   }

   private void verifyNextIterationOfWholeBlockOfInvocations(int n)
   {
      for (int i = 0; i < n; i++) {
         Expectation verified = expectationsVerified.get(i);
         ExpectedInvocationWithMatchers invocation = verified.expectedInvocation;

         argMatchers = invocation.invocationArgMatchers;
         handleInvocation(
            invocation.instance, 0, invocation.classDesc, invocation.methodNameAndDesc,
            invocation.invocationArgs);

         if (recordAndReplay.errorThrown != null) {
            throw recordAndReplay.errorThrown;
         }
      }
   }
}
