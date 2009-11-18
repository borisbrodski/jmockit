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

import mockit.internal.state.*;

final class ReplayPhase extends Phase
{
   private int initialStrictExpectationIndexForCurrentBlock;
   int currentStrictExpectationIndex;
   final List<Expectation> nonStrictInvocations;
   private Expectation nonStrictExpectation;

   ReplayPhase(RecordAndReplayExecution recordAndReplay)
   {
      super(recordAndReplay);
      nonStrictInvocations = new ArrayList<Expectation>();
      initialStrictExpectationIndexForCurrentBlock =
         Math.max(recordAndReplay.lastExpectationIndexInPreviousReplayPhase, 0);
      positionOnFirstStrictInvocation();
   }

   private void positionOnFirstStrictInvocation()
   {
      List<Expectation> expectations = getExpectations();

      if (expectations.isEmpty()) {
         currentStrictExpectationIndex = -1;
         currentExpectation = null ;
      }
      else {
         currentStrictExpectationIndex = initialStrictExpectationIndexForCurrentBlock;
         currentExpectation =
            currentStrictExpectationIndex < expectations.size() ?
               expectations.get(currentStrictExpectationIndex) : null;
      }
   }

   @Override
   Object handleInvocation(
      Object mock, int mockAccess, String mockClassDesc, String mockNameAndDesc, Object[] args)
      throws Throwable
   {
      nonStrictExpectation = null;

      if (!findNonStrictExpectation(mock, mockClassDesc, mockNameAndDesc, args)) {
         createExpectationIfNonStrictInvocation(
            mock, mockAccess, mockClassDesc, mockNameAndDesc, args);
      }

      if (nonStrictExpectation != null) {
         nonStrictInvocations.add(nonStrictExpectation);
         return updateConstraintsAndProduceResult(args);
      }

      return handleStrictInvocation(mock, mockClassDesc, mockNameAndDesc, args);
   }

   private boolean findNonStrictExpectation(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      List<Expectation> nonStrictExpectations = getNonStrictExpectations();

      for (Expectation nonStrict : nonStrictExpectations) {
         ExpectedInvocation invocation = nonStrict.expectedInvocation;

         if (
            invocation.isMatch(mock, mockClassDesc, mockNameAndDesc) &&
            invocation.assertThatInvocationArgumentsMatch(args) == null
         ) {
            nonStrictExpectation = nonStrict;
            return true;
         }
      }

      return false;
   }

   private void createExpectationIfNonStrictInvocation(
      Object mock, int mockAccess, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      if (!TestRun.getExecutingTest().containsStrictMockForRunningTest(mock, mockClassDesc)) {
         ExpectedInvocationWithMatchers invocation =
            new ExpectedInvocationWithMatchers(
               mock, mockAccess, mockClassDesc, mockNameAndDesc, false, args, null,
               recordAndReplay.recordToReplayInstanceMap);

         nonStrictExpectation = new Expectation(null, invocation, true);

         recordAndReplay.nonStrictExpectations.add(nonStrictExpectation);
      }
   }

   private Object updateConstraintsAndProduceResult(Object[] args) throws Throwable
   {
      nonStrictExpectation.constraints.incrementInvocationCount();

      if (nonStrictExpectation.constraints.isInvocationCountMoreThanMaximumExpected()) {
         recordAndReplay.errorThrown =
            nonStrictExpectation.expectedInvocation.errorForUnexpectedInvocations(1);
         return null;
      }

      return nonStrictExpectation.produceResult(args);
   }

   private Object handleStrictInvocation(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] replayArgs)
      throws Throwable
   {
      while (true) {
         if (currentExpectation == null) {
            recordAndReplay.errorThrown =
               new ExpectedInvocation(mock, mockClassDesc, mockNameAndDesc, replayArgs)
                  .errorForUnexpectedInvocation();
            return null;
         }

         ExpectedInvocation invocation = currentExpectation.expectedInvocation;

         if (invocation.isMatch(mock, mockClassDesc, mockNameAndDesc)) {
            if (mock != invocation.instance) {
               recordAndReplay.recordToReplayInstanceMap.put(invocation.instance, mock);
            }

            AssertionError error = invocation.assertThatInvocationArgumentsMatch(replayArgs);

            if (error != null) {
               if (currentExpectation.constraints.isInvocationCountInExpectedRange()) {
                  moveToNextExpectation();
                  continue;
               }

               recordAndReplay.errorThrown = error;
               return null;
            }

            Expectation expectation = currentExpectation;

            if (expectation.constraints.incrementInvocationCount()) {
               moveToNextExpectation();
            }

            return expectation.produceResult(replayArgs);
         }
         else if (currentExpectation.constraints.isInvocationCountInExpectedRange()) {
            moveToNextExpectation();
         }
         else {
            recordAndReplay.errorThrown =
               invocation.errorForUnexpectedInvocation(mock, mockClassDesc, mockNameAndDesc);
            return null;
         }
      }
   }

   private void moveToNextExpectation()
   {
      List<Expectation> expectations = getExpectations();
      RecordPhase expectationBlock = currentExpectation.recordPhase;
      currentStrictExpectationIndex++;

      currentExpectation =
         currentStrictExpectationIndex < expectations.size() ?
            expectations.get(currentStrictExpectationIndex) : null;

      if (expectationBlock.numberOfIterations == 1) {
         if (currentExpectation != null && currentExpectation.recordPhase != expectationBlock) {
            initialStrictExpectationIndexForCurrentBlock = currentStrictExpectationIndex;
         }
      }
      else if (currentExpectation == null || currentExpectation.recordPhase != expectationBlock) {
         expectationBlock.numberOfIterations--;
         positionOnFirstStrictInvocation();
         resetInvocationCountsForStrictExpectations(expectationBlock);
      }
   }

   private void resetInvocationCountsForStrictExpectations(RecordPhase expectationBlock)
   {
      for (Expectation expectation : getExpectations()) {
         if (expectation.recordPhase == expectationBlock) {
            expectation.constraints.invocationCount = 0;
         }
      }
   }

   AssertionError endExecution()
   {
      Expectation strict = currentExpectation;
      currentExpectation = null;

      if (strict != null && strict.constraints.isInvocationCountLessThanMinimumExpected()) {
         return strict.expectedInvocation.errorForMissingInvocation();
      }

      List<Expectation> nonStrictExpectations = getNonStrictExpectations();

      for (Expectation nonStrict : nonStrictExpectations) {
         InvocationConstraints constraints = nonStrict.constraints;

         if (constraints.isInvocationCountLessThanMinimumExpected()) {
            return constraints.errorForMissingExpectations(nonStrict.expectedInvocation);
         }
      }

      return null;
   }
}
