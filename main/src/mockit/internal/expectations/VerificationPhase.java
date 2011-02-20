/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;

import mockit.internal.expectations.invocation.*;
import mockit.internal.util.*;

public abstract class VerificationPhase extends TestOnlyPhase
{
   final List<Expectation> expectationsInReplayOrder;
   final List<Object[]> invocationArgumentsInReplayOrder;
   private boolean allInvocationsDuringReplayMustBeVerified;
   private Object[] mockedTypesAndInstancesToFullyVerify;
   protected AssertionError pendingError;

   protected VerificationPhase(
      RecordAndReplayExecution recordAndReplay,
      List<Expectation> expectationsInReplayOrder, List<Object[]> invocationArgumentsInReplayOrder)
   {
      super(recordAndReplay);
      this.expectationsInReplayOrder = expectationsInReplayOrder;
      this.invocationArgumentsInReplayOrder = invocationArgumentsInReplayOrder;
   }

   public List<Expectation> getExpectationsVerified() { return recordAndReplay.executionState.expectationsVerified; }

   public final void setAllInvocationsMustBeVerified() { allInvocationsDuringReplayMustBeVerified = true; }

   public final void setMockedTypesToFullyVerify(Object[] mockedTypesAndInstancesToFullyVerify)
   {
      this.mockedTypesAndInstancesToFullyVerify = mockedTypesAndInstancesToFullyVerify;
   }

   @Override
   final Object handleInvocation(
      Object mock, int mockAccess, String mockClassDesc, String mockNameAndDesc, boolean withRealImpl, Object[] args)
   {
      if (pendingError != null) {
         recordAndReplay.setErrorThrown(pendingError);
         pendingError = null;
         return null;
      }

      matchInstance = nextInstanceToMatch != null && mock == nextInstanceToMatch;

      currentExpectation = null;
      findNonStrictExpectation(mock, mockClassDesc, mockNameAndDesc, args);
      argMatchers = null;

      if (matchInstance) {
         nextInstanceToMatch = null;
      }

      if (recordAndReplay.getErrorThrown() != null) {
         return null;
      }

      if (currentExpectation == null) {
         ExpectedInvocation currentInvocation =
            new ExpectedInvocation(mock, mockAccess, mockClassDesc, mockNameAndDesc, false, args);
         currentExpectation = new Expectation(null, currentInvocation, true);

         ExpectedInvocation missingInvocation = new ExpectedInvocation(mock, mockClassDesc, mockNameAndDesc, args);
         pendingError = missingInvocation.errorForMissingInvocation();
      }

      return currentExpectation.invocation.getDefaultValueForReturnType(this);
   }

   abstract void findNonStrictExpectation(Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args);

   final boolean matches(
      Object mock, String mockClassDesc, String mockNameAndDesc, Object[] args, Expectation expectation)
   {
      ExpectedInvocation invocation = expectation.invocation;
      Map<Object, Object> instanceMap = getInstanceMap();

      if (
         invocation.isMatch(mock, mockClassDesc, mockNameAndDesc, instanceMap) &&
         (!matchInstance || invocation.isEquivalentInstance(mock, instanceMap))
      ) {
         Object[] argsToVerify =
            argMatchers == null ? args : invocation.arguments.prepareForVerification(args, argMatchers);

         AssertionError error = invocation.arguments.assertMatch(argsToVerify, instanceMap);

         if (argMatchers != null) {
            invocation.arguments.setValuesWithNoMatchers(argsToVerify);
         }

         if (error == null) {
            getExpectationsVerified().add(expectation);
            return true;
         }
      }

      return false;
   }

   @Override
   public final void setMaxInvocationCount(int maxInvocations)
   {
      if (maxInvocations == 0 || pendingError == null) {
         super.setMaxInvocationCount(maxInvocations);
      }
   }

   @Override
   public final void setCustomErrorMessage(CharSequence customMessage)
   {
      Expectation expectation = getCurrentExpectation();

      if (pendingError == null) {
         expectation.setCustomErrorMessage(customMessage);
      }
      else if (customMessage != null) {
         StackTraceElement[] previousStackTrace = pendingError.getStackTrace();
         pendingError = new AssertionError(customMessage + "\n" + pendingError.getMessage());
         pendingError.setStackTrace(previousStackTrace);
      }
   }

   final boolean evaluateInvocationHandlerIfExpectationMatchesCurrent(
      Expectation expectation, Object[] replayArgs, InvocationHandler handler, int invocationIndex)
   {
      ExpectedInvocation invocation = expectation.invocation;
      Object mock = invocation.instance;
      String mockClassDesc = invocation.getClassDesc();
      String mockNameAndDesc = invocation.getMethodNameAndDescription();
      Object[] args = invocation.getArgumentValues();
      InvocationConstraints constraints = expectation.constraints;

      if (matches(mock, mockClassDesc, mockNameAndDesc, args, currentExpectation)) {
         int originalCount = constraints.invocationCount;
         constraints.invocationCount = invocationIndex + 1;

         try {
            handler.evaluateInvocation(expectation, replayArgs);
         }
         finally {
            constraints.invocationCount = originalCount;
         }

         return true;
      }

      return false;
   }

   protected AssertionError endVerification()
   {
      if (pendingError != null) {
         return pendingError;
      }

      if (allInvocationsDuringReplayMustBeVerified) {
         return validateThatAllInvocationsWereVerified();
      }

      return null;
   }

   private AssertionError validateThatAllInvocationsWereVerified()
   {
      List<Expectation> notVerified = new ArrayList<Expectation>(expectationsInReplayOrder);
      notVerified.removeAll(getExpectationsVerified());
      discardExpectationsThatWillBeVerifiedImplicitly(notVerified);

      if (!notVerified.isEmpty()) {
         if (mockedTypesAndInstancesToFullyVerify == null) {
            Expectation firstUnexpected = notVerified.get(0);
            return firstUnexpected.invocation.errorForUnexpectedInvocation();
         }

         return validateThatUnverifiedInvocationsAreAllowed(notVerified);
      }

      return null;
   }

   private void discardExpectationsThatWillBeVerifiedImplicitly(List<Expectation> unverified)
   {
      for (Iterator<Expectation> itr = unverified.iterator(); itr.hasNext(); ) {
         Expectation expectation = itr.next();

         if (expectation.constraints.minInvocations > 0) {
            itr.remove();
         }
      }
   }

   private AssertionError validateThatUnverifiedInvocationsAreAllowed(List<Expectation> unverified)
   {
      for (Expectation expectation : unverified) {
         ExpectedInvocation invocation = expectation.invocation;

         if (isInvocationToBeVerified(invocation)) {
            return invocation.errorForUnexpectedInvocation();
         }
      }

      return null;
   }

   private boolean isInvocationToBeVerified(ExpectedInvocation unverifiedInvocation)
   {
      String invokedClassName = unverifiedInvocation.getClassName();
      Object invokedInstance = unverifiedInvocation.instance;

      for (Object mockedTypeOrInstance : mockedTypesAndInstancesToFullyVerify) {
         if (mockedTypeOrInstance instanceof Class) {
            Class<?> mockedType = (Class<?>) mockedTypeOrInstance;

            if (invokedClassName.equals(mockedType.getName())) {
               return true;
            }
         }
         else if (invokedInstance == null) {
            Class<?> invokedClass = Utilities.loadClass(invokedClassName);

            if (invokedClass.isInstance(mockedTypeOrInstance)) {
               return true;
            }
         }
         else if (unverifiedInvocation.matchInstance) {
            if (mockedTypeOrInstance == invokedInstance) {
               return true;
            }
         }
         else if (invokedInstance.getClass().isInstance(mockedTypeOrInstance)) {
            return true;
         }
      }

      return false;
   }
}
