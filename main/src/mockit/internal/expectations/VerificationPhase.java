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

public abstract class VerificationPhase extends TestOnlyPhase
{
   private final List<Expectation> expectationsInReplayOrder;
   protected final List<Expectation> expectationsVerified;
   private boolean allInvocationsDuringReplayMustBeVerified;
   protected AssertionError pendingError;
   private Expectation aggregate;

   protected VerificationPhase(
      RecordAndReplayExecution recordAndReplay, List<Expectation> expectationsInReplayOrder)
   {
      super(recordAndReplay);
      this.expectationsInReplayOrder = expectationsInReplayOrder;
      expectationsVerified = new ArrayList<Expectation>();
   }

   public final void setAllInvocationsMustBeVerified()
   {
      allInvocationsDuringReplayMustBeVerified = true;
   }

   @Override
   final Object handleInvocation(
      Object mock, int mockAccess, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      if (pendingError != null) {
         recordAndReplay.errorThrown = pendingError;
         pendingError = null;
         return null;
      }

      currentExpectation = null;
      aggregate = null;
      findNonStrictExpectation(mock, mockClassDesc, mockNameAndDesc, args);
      argMatchers = null;

      if (recordAndReplay.errorThrown != null) {
         return null;
      }

      if (currentExpectation == null) {
         ExpectedInvocationWithMatchers invocation =
            new ExpectedInvocationWithMatchers(
               mock, mockAccess, mockClassDesc, mockNameAndDesc, false, args, null,
               recordAndReplay.recordToReplayInstanceMap);

         currentExpectation = new Expectation(null, invocation, true);

         pendingError =
            new ExpectedInvocation(mock, mockClassDesc, mockNameAndDesc, args)
               .errorForMissingInvocation();
      }

      return currentExpectation.expectedInvocation.getDefaultValueForReturnType();
   }

   protected abstract void findNonStrictExpectation(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args);

   protected final boolean matches(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args,
      Expectation expectation)
   {
      ExpectedInvocationWithMatchers invocation = expectation.expectedInvocation;

      if (invocation.isMatch(mock, mockClassDesc, mockNameAndDesc)) {
         Object[] argsToVerify =
            argMatchers == null ?
               args : invocation.prepareArgumentsForVerification(args, argMatchers);

         AssertionError error = invocation.assertThatInvocationArgumentsMatch(argsToVerify);

         if (argMatchers != null) {
            invocation.prepareArgumentsForVerification(argsToVerify, null);
         }

         if (error == null) {
            expectationsVerified.add(expectation);
            return true;
         }
      }

      return false;
   }

   protected final void aggregateMatchingExpectations(Expectation found)
   {
      if (currentExpectation == null) {
         currentExpectation = found;
         return;
      }

      if (aggregate == null) {
         aggregate = new Expectation(currentExpectation);
         currentExpectation = aggregate;
      }

      aggregate.constraints.addInvocationCount(found.constraints);
   }

   protected AssertionError endVerification()
   {
      if (pendingError != null) {
         return pendingError;
      }

      if (allInvocationsDuringReplayMustBeVerified) {
         List<Expectation> notVerified = new ArrayList<Expectation>(expectationsInReplayOrder);
         notVerified.removeAll(expectationsVerified);

         if (!notVerified.isEmpty()) {
            Expectation firstUnexpected = notVerified.get(0);
            return firstUnexpected.expectedInvocation.errorForUnexpectedInvocation();
         }
      }

      return null;
   }
}
