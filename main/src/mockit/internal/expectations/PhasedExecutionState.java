/*
 * JMockit Expectations & Verifications
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
import mockit.internal.util.*;

final class PhasedExecutionState
{
   final List<Expectation> expectations;
   final List<Expectation> nonStrictExpectations;
   final Map<Object, Object> instanceMap;
   private List<Class<?>> mockedTypesToMatchOnInstances;

   PhasedExecutionState()
   {
      expectations = new ArrayList<Expectation>();
      nonStrictExpectations = new ArrayList<Expectation>();
      instanceMap = new IdentityHashMap<Object, Object>();
   }

   void discoverMockedTypesToMatchOnInstances(List<Class<?>> targetClasses)
   {
      int numClasses = targetClasses.size();

      if (numClasses > 1) {
         for (int i = 0; i < numClasses; i++) {
            Class<?> targetClass = targetClasses.get(i);

            if (targetClasses.lastIndexOf(targetClass) > i) {
               addMockedTypeToMatchOnInstance(targetClass);
            }
         }
      }
   }

   private void addMockedTypeToMatchOnInstance(Class<?> mockedType)
   {
      if (mockedTypesToMatchOnInstances == null) {
         mockedTypesToMatchOnInstances = new LinkedList<Class<?>>();
      }

      mockedTypesToMatchOnInstances.add(mockedType);
   }

   void addExpectation(Expectation expectation, boolean nonStrict)
   {
      forceMatchingOnMockInstanceIfRequired(expectation.invocation);

      if (nonStrict) {
         nonStrictExpectations.add(expectation);
      }
      else {
         expectations.add(expectation);
      }
   }

   private void forceMatchingOnMockInstanceIfRequired(ExpectedInvocation invocation)
   {
      if (mockedTypesToMatchOnInstances != null) {
         Object mock = invocation.instance;

         if (mock != null) {
            Class<?> mockedClass = Utilities.getMockedClass(mock);

            if (mockedTypesToMatchOnInstances.contains(mockedClass)) {
               invocation.matchInstance = true;
            }
         }
      }
   }

   public void makeNonStrict(Expectation expectation)
   {
      if (expectations.remove(expectation)) {
         expectation.constraints.setDefaultLimits(true);
         nonStrictExpectations.add(expectation);
      }
   }
}
