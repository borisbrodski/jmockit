/*
 * JMockit Expectations
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

import mockit.internal.expectations.invocation.*;
import mockit.internal.state.*;
import static mockit.internal.util.Utilities.*;

public final class RecordPhase extends TestOnlyPhase
{
   private final boolean nonStrict;

   RecordPhase(RecordAndReplayExecution recordAndReplay, boolean nonStrict)
   {
      super(recordAndReplay);
      this.nonStrict = nonStrict;
   }

   public void setNotStrict()
   {
      if (getExpectations().remove(currentExpectation)) {
         currentExpectation.constraints.setDefaultLimits(true);
         getNonStrictExpectations().add(currentExpectation);
      }
   }

   @Override
   Object handleInvocation(
      Object mock, int mockAccess, String classDesc, String mockNameAndDesc, Object[] args)
      throws Throwable
   {
      //noinspection AssignmentToMethodParameter
      mock = configureMatchingOnMockInstanceIfSpecified(mock);
      ExpectedInvocation invocation =
         new ExpectedInvocation(mock, mockAccess, classDesc, mockNameAndDesc, matchInstance, args);
      boolean nonStrictInvocation =
         nonStrict || TestRun.getExecutingTest().containsNonStrictMock(mock, classDesc);

      if (!nonStrictInvocation) {
         String mockClassDesc = matchInstance ? null : classDesc;
         TestRun.getExecutingTest().addStrictMock(mock, mockClassDesc);
      }

      currentExpectation = new Expectation(this, invocation, nonStrictInvocation);

      if (argMatchers != null) {
         invocation.arguments.setMatchers(argMatchers);
         argMatchers = null;
      }

      recordAndReplay.addRecordedExpectation(currentExpectation, nonStrictInvocation);

      return invocation.getDefaultValueForReturnType(this);
   }

   private Object configureMatchingOnMockInstanceIfSpecified(Object mock)
   {
      matchInstance = false;

      if (mock == null || nextInstanceToMatch == null) {
         return mock;
      }

      Object specified = nextInstanceToMatch;

      if (mock != specified) {
         Class<?> mockedClass = getMockedClass(mock);

         if (!mockedClass.isInstance(specified)) {
            return mock;
         }
      }

      nextInstanceToMatch = null;
      matchInstance = true;
      return specified;
   }

   @Override
   public void handleInvocationCountConstraint(int minInvocations, int maxInvocations)
   {
      int lowerLimit = minInvocations;
      int upperLimit = maxInvocations;

      if (numberOfIterations > 1 && nonStrict) {
         lowerLimit *= numberOfIterations;
         upperLimit *= numberOfIterations;
      }

      getCurrentExpectation().constraints.setLimits(lowerLimit, upperLimit);
   }

   @Override
   public void setCustomErrorMessage(CharSequence customMessage)
   {
      getCurrentExpectation().setCustomErrorMessage(customMessage);
   }

   @Override
   public void applyHandlerForEachInvocation(Object invocationHandler)
   {
      getCurrentExpectation().getResults().addResult(new InvocationHandler(invocationHandler));
   }
}
