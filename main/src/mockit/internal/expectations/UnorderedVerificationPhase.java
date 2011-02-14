/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
