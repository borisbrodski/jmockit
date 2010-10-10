/*
 * JMockit Verifications
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
package mockit.internal.expectations;

import java.util.*;

import mockit.internal.expectations.invocation.*;

final class UnorderedVerificationPhase extends VerificationPhase
{
   private Expectation aggregate;

   UnorderedVerificationPhase(
      RecordAndReplayExecution recordAndReplay,
      List<Expectation> expectationsInReplayOrder, List<Object[]> invocationArgumentsInReplayOrder)
   {
      super(recordAndReplay, expectationsInReplayOrder, invocationArgumentsInReplayOrder);
   }

   @Override
   protected void findNonStrictExpectation(Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      aggregate = null;
      List<Expectation> expectations = getNonStrictExpectations();

      for (Expectation expectation : expectations) {
         if (matches(mock, mockClassDesc, mockNameAndDesc, args, expectation)) {
            if (argMatchers == null) {
               currentExpectation = expectation;
               break;
            }

            aggregateMatchingExpectations(expectation);
         }
      }

      if (currentExpectation != null) {
         currentExpectation.constraints.setLimits(numberOfIterations, -1);
         pendingError = currentExpectation.verifyConstraints();
      }
   }

   private void aggregateMatchingExpectations(Expectation found)
   {
      if (currentExpectation == null) {
         found.invocation.arguments.setMatchers(argMatchers);
         currentExpectation = found;
         return;
      }

      if (aggregate == null) {
         aggregate = new Expectation(currentExpectation);
         currentExpectation = aggregate;
      }

      aggregate.constraints.addInvocationCount(found.constraints);
   }

   @Override
   public void handleInvocationCountConstraint(int minInvocations, int maxInvocations)
   {
      Expectation expectation = getCurrentExpectation();
      expectation.constraints.setLimits(numberOfIterations * minInvocations, numberOfIterations * maxInvocations);

      pendingError = null;
      AssertionError error = expectation.verifyConstraints();

      if (error != null) {
         pendingError = error;
      }
   }

   @Override
   public void applyHandlerForEachInvocation(Object invocationHandler)
   {
      if (pendingError != null) {
         return;
      }

      getCurrentExpectation();
      InvocationHandler handler = new InvocationHandler(invocationHandler);
      int i = 0;

      for (int j = 0, n = expectationsInReplayOrder.size(); j < n; j++) {
         Expectation expectation = expectationsInReplayOrder.get(j);
         Object[] args = invocationArgumentsInReplayOrder.get(j);

         if (evaluateInvocationHandlerIfExpectationMatchesCurrent(expectation, args, handler, i)) {
            i++;
         }
      }
   }
}
