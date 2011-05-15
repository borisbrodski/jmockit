/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.internal.expectations.invocation.*;
import mockit.internal.util.*;

final class PhasedExecutionState
{
   final List<Expectation> expectations;
   final List<Expectation> nonStrictExpectations;
   final List<VerifiedExpectation> verifiedExpectations;
   final Map<Object, Object> instanceMap;
   private List<Class<?>> mockedTypesToMatchOnInstances;

   PhasedExecutionState()
   {
      expectations = new ArrayList<Expectation>();
      nonStrictExpectations = new ArrayList<Expectation>();
      verifiedExpectations = new ArrayList<VerifiedExpectation>();
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
      ExpectedInvocation invocation = expectation.invocation;
      forceMatchingOnMockInstanceIfRequired(invocation);
      removeMatchingExpectationsCreatedBefore(invocation);

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

   private void removeMatchingExpectationsCreatedBefore(ExpectedInvocation invocation)
   {
      Expectation previousExpectation = findPreviousNonStrictExpectation(invocation);

      if (previousExpectation != null) {
         nonStrictExpectations.remove(previousExpectation);
         invocation.copyDefaultReturnValue(previousExpectation.invocation);
      }
   }

   private Expectation findPreviousNonStrictExpectation(ExpectedInvocation newInvocation)
   {
      Object mock = newInvocation.instance;
      String mockClassDesc = newInvocation.getClassDesc();
      String mockNameAndDesc = newInvocation.getMethodNameAndDescription();
      InvocationArguments arguments = newInvocation.arguments;
      Object[] argValues = arguments.getValues();
      boolean newInvocationWithMatchers = arguments.getMatchers() != null;

      for (int i = 0, n = nonStrictExpectations.size(); i < n; i++) {
         Expectation previousExpectation = nonStrictExpectations.get(i);
         ExpectedInvocation previousInvocation = previousExpectation.invocation;

         if (
            isInvocationToSameMethodOrConstructor(mock, mockClassDesc, mockNameAndDesc, previousInvocation) &&
            (newInvocationWithMatchers && arguments.hasEquivalentMatchers(previousInvocation.arguments) ||
             !newInvocationWithMatchers && previousInvocation.arguments.isMatch(argValues, instanceMap))
         ) {
            return previousExpectation;
         }
      }

      return null;
   }

   private boolean isInvocationToSameMethodOrConstructor(
      Object mock, String mockClassDesc, String mockNameAndDesc, ExpectedInvocation invocation)
   {
      return invocation.isMatch(mockClassDesc, mockNameAndDesc) && invocation.isMatch(mock, instanceMap);
   }

   Expectation findNonStrictExpectation(Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args)
   {
      // Note: new expectations might get added to the list, so a regular loop would cause a CME:
      for (int i = 0, n = nonStrictExpectations.size(); i < n; i++) {
         Expectation nonStrict = nonStrictExpectations.get(i);
         ExpectedInvocation invocation = nonStrict.invocation;

         if (
            isInvocationToSameMethodOrConstructor(mock, mockClassDesc, mockNameAndDesc, invocation) &&
            invocation.arguments.isMatch(args, instanceMap)
         ) {
            return nonStrict;
         }
      }

      return null;
   }

   void makeNonStrict(Expectation expectation)
   {
      if (expectations.remove(expectation)) {
         expectation.constraints.setDefaultLimits(true);
         nonStrictExpectations.add(expectation);
      }
   }
}
