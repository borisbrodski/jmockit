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

import java.lang.reflect.*;
import java.util.*;

import mockit.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class RecordAndReplayExecution
{
   private final LocalFieldTypeRedefinitions redefinitions;
   private final Map<Type, Object> typesAndTargetObjects;
   private final DynamicPartialMocking dynamicPartialMocking;

   final PhasedExecutionState executionState;
   final int lastExpectationIndexInPreviousReplayPhase;

   private RecordPhase recordPhase;
   private ReplayPhase replayPhase;
   private VerificationPhase verificationPhase;

   /**
    * Holds an error associated to an ExpectedInvocation that is to be reported to the user.
    * <p/>
    * This field is also set if and when an unexpected invocation is detected, so that any future
    * invocations in this same phase execution can rethrow the original error instead of throwing a
    * new one, which would hide the original.
    * Such a situation can happen when test code or the code under test contains a "catch" or
    * "finally" block where a mock invocation is made after a previous such invocation in the "try"
    * block already failed.
    */
   AssertionError errorThrown;

   public RecordAndReplayExecution(RecordAndReplayExecution previous)
   {
      if (previous == null) {
         executionState = new PhasedExecutionState();
         lastExpectationIndexInPreviousReplayPhase = 0;
      }
      else {
         executionState = previous.executionState;
         lastExpectationIndexInPreviousReplayPhase =
            previous.getLastExpectationIndexInPreviousReplayPhase();
      }

      redefinitions = null;
      typesAndTargetObjects = new HashMap<Type, Object>(1);
      dynamicPartialMocking = null;
      validateThereIsAtLeastOneMockedTypeInScope();
      discoverDuplicateMockedTypesForAutomaticMockInstanceMatching();
      replayPhase = new ReplayPhase(this);
   }

   private int getLastExpectationIndexInPreviousReplayPhase()
   {
      return replayPhase == null ? -1 : replayPhase.currentStrictExpectationIndex;
   }

   public RecordAndReplayExecution(
      Expectations targetObject, Object... classesOrInstancesToBePartiallyMocked)
   {
      TestRun.enterNoMockingZone();
      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);

      try {
         RecordAndReplayExecution previous = TestRun.getExecutingTest().setRecordAndReplay(null);
         Class<?> enclosingClassForTargetObject = targetObject.getClass().getEnclosingClass();

         if (enclosingClassForTargetObject == null) {
            throw new RuntimeException("Invalid top level Expectations subclass");
         }

         if (previous == null) {
            executionState = new PhasedExecutionState();
            lastExpectationIndexInPreviousReplayPhase = 0;
         }
         else {
            executionState = previous.executionState;
            lastExpectationIndexInPreviousReplayPhase =
               previous.getLastExpectationIndexInPreviousReplayPhase();
         }

         recordPhase = new RecordPhase(this, targetObject instanceof NonStrictExpectations);

         LocalFieldTypeRedefinitions redefs = new LocalFieldTypeRedefinitions(targetObject);
         typesAndTargetObjects =
            previous == null ? new HashMap<Type, Object>(2) : previous.typesAndTargetObjects;
         redefineFieldTypes(redefs);
         redefinitions = redefs.getTypesRedefined() == 0 ? null : redefs;

         dynamicPartialMocking = applyDynamicPartialMocking(classesOrInstancesToBePartiallyMocked);

         validateThereIsAtLeastOneMockedTypeInScope();
         discoverDuplicateMockedTypesForAutomaticMockInstanceMatching();
         TestRun.getExecutingTest().setRecordAndReplay(this);
      }
      finally {
         TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(false);
         TestRun.exitNoMockingZone();
      }
   }

   private void redefineFieldTypes(LocalFieldTypeRedefinitions redefs)
   {
      //noinspection CatchGenericClass
      try {
         redefs.redefineTypesForNestedClass(typesAndTargetObjects);
      }
      catch (Error e) {
         redefs.cleanUp();
         Utilities.filterStackTrace(e);
         throw e;
      }
      catch (RuntimeException e) {
         redefs.cleanUp();
         Utilities.filterStackTrace(e);
         throw e;
      }
   }

   private void validateThereIsAtLeastOneMockedTypeInScope()
   {
      if (
         redefinitions == null && dynamicPartialMocking == null &&
         TestRun.getSharedFieldTypeRedefinitions().getTypesRedefined() == 0
      ) {
         ParameterTypeRedefinitions paramTypeRedefinitions =
            TestRun.getExecutingTest().getParameterTypeRedefinitions();

         if (paramTypeRedefinitions == null || paramTypeRedefinitions.getTypesRedefined() == 0) {
            throw new IllegalStateException(
               "No mocked types in scope; " +
               "please declare mock fields or parameters for the types you need mocked");
         }
      }
   }

   private void discoverDuplicateMockedTypesForAutomaticMockInstanceMatching()
   {
      List<Class<?>> fields = TestRun.getSharedFieldTypeRedefinitions().getTargetClasses();
      List<Class<?>> targetClasses = new ArrayList<Class<?>>(fields);

      ParameterTypeRedefinitions paramTypeRedefinitions =
         TestRun.getExecutingTest().getParameterTypeRedefinitions();

      if (paramTypeRedefinitions != null) {
         targetClasses.addAll(paramTypeRedefinitions.getTargetClasses());
      }

      if (dynamicPartialMocking != null) {
         List<Class<?>> staticallyMockedClasses = new ArrayList<Class<?>>(targetClasses);
         List<Class<?>> dynamicallyMockedClasses = dynamicPartialMocking.getTargetClasses();

         for (Class<?> dynamicallyMockedClass : dynamicallyMockedClasses) {
            if (!staticallyMockedClasses.contains(dynamicallyMockedClass)) {
               targetClasses.addAll(dynamicallyMockedClasses);
            }
         }
      }

      executionState.discoverMockedTypesToMatchOnInstances(targetClasses);
   }

   public Map<Type, Object> getLocalMocks()
   {
      return typesAndTargetObjects;
   }

   private DynamicPartialMocking applyDynamicPartialMocking(Object... classesOrInstances)
   {
      if (classesOrInstances == null || classesOrInstances.length == 0) {
         return null;
      }

      DynamicPartialMocking mocking = new DynamicPartialMocking();
      mocking.redefineTypes(classesOrInstances);
      return mocking;
   }

   public RecordPhase getRecordPhase()
   {
      if (recordPhase == null) {
         throw new IllegalStateException("Not in the recording phase");
      }

      return recordPhase;
   }

   /**
    * Only to be called from generated bytecode or from the Mocking Bridge.
    */
   public static synchronized Object recordOrReplay(
      Object mock, int mockAccess, String classDesc, String mockDesc, boolean withRealImpl,
      Object... args)
      throws Throwable
   {
      if (TestRun.getExecutingTest().isShouldIgnoreMockingCallbacks()) {
         return null;
      }

      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(true);

      if (mockDesc.startsWith("<init>") && handleCallToConstructor(instance, mock, classDesc)) {
         return null;
      }

      if (instance == null) {
         // This occurs when the mock class constructor is called to instantiate the mock object.
         return DefaultValues.computeForReturnType(mockDesc);
      }

      Phase currentPhase = instance.getCurrentPhase();
      instance.errorThrown = null;

      Object result =
         currentPhase.handleInvocation(mock, mockAccess, classDesc, mockDesc, withRealImpl, args);

      if (instance.errorThrown != null) {
         throw instance.errorThrown;
      }

      return result;
   }

   private static boolean handleCallToConstructor(
      RecordAndReplayExecution instance, Object mock, String classDesc)
   {
      if (TestRun.getCurrentTestInstance() != null) {
         FieldTypeRedefinitions fieldTypeRedefs = instance == null ? null : instance.redefinitions;

         if (
            fieldTypeRedefs != null &&
            fieldTypeRedefs.captureNewInstanceForApplicableMockField(mock)
         ) {
            return true;
         }

         ParameterTypeRedefinitions paramTypeRedefinitions =
            TestRun.getExecutingTest().getParameterTypeRedefinitions();

         if (paramTypeRedefinitions != null) {
            CaptureOfNewInstancesForParameters paramTypeCaptures =
               paramTypeRedefinitions.getCaptureOfNewInstances();

            if (
               paramTypeCaptures != null &&
               paramTypeCaptures.captureNewInstanceForApplicableMockParameter(mock)
            ) {
               return true;
            }
         }

         FieldTypeRedefinitions sharedFieldTypeRedefs = TestRun.getSharedFieldTypeRedefinitions();

         if (sharedFieldTypeRedefs.captureNewInstanceForApplicableMockField(mock)) {
            return true;
         }
      }

      return isCallToSuperClassConstructor(mock, classDesc);
   }

   private static boolean isCallToSuperClassConstructor(Object mock, String calledClassDesc)
   {
      Class<?> mockedClass = mock.getClass();

      if (mockedClass.isAnonymousClass()) {
         // An anonymous class instantiation always invokes the constructor on the super-class,
         // so that is the class we need to consider, not the anonymous one.
         mockedClass = mockedClass.getSuperclass();

         if (mockedClass == Object.class) {
            return false;
         }
      }

      String calledClassName = calledClassDesc.replace('/', '.');

      return !calledClassName.equals(mockedClass.getName());
   }

   private Phase getCurrentPhase()
   {
      ReplayPhase replay = replayPhase;

      if (replay == null) {
         return recordPhase;
      }

      VerificationPhase verification = verificationPhase;

      if (verification == null) {
         if (errorThrown != null) {
            // This can only happen when called from a catch/finally block.
            throw errorThrown;
         }

         return replay;
      }

      return verification;
   }

   void addRecordedExpectation(Expectation expectation, boolean nonStrictInvocation)
   {
      executionState.addExpectation(expectation, nonStrictInvocation);
   }

   public void endRecording()
   {
      if (replayPhase == null) {
         recordPhase = null;
         replayPhase = new ReplayPhase(this);
      }
   }

   public VerificationPhase startVerifications(boolean inOrder)
   {
      if (replayPhase == null) {
         throw new IllegalStateException("Not in the replay phase yet");
      }

      List<Expectation> expectationsInReplayOrder = replayPhase.nonStrictInvocations;
      verificationPhase =
         inOrder ?
            new OrderedVerificationPhase(this, expectationsInReplayOrder) :
            new UnorderedVerificationPhase(this, expectationsInReplayOrder);

      return verificationPhase;
   }

   public static AssertionError endCurrentReplayIfAny()
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);
      return instance == null ? null : instance.endExecution();
   }

   private AssertionError endExecution()
   {
      endRecording();

      if (redefinitions != null) {
         redefinitions.cleanUp();
      }

      AssertionError error = replayPhase.endExecution();

      if (error == null && verificationPhase != null) {
         error = verificationPhase.endVerification();
         verificationPhase = null;
      }

      return error;
   }

   public TestOnlyPhase getCurrentTestOnlyPhase()
   {
      return recordPhase != null ? recordPhase : verificationPhase;
   }

   public void endInvocations()
   {
      if (verificationPhase == null) {
         endRecording();
      }
      else {
         AssertionError error = verificationPhase.endVerification();
         verificationPhase = null;

         if (error != null) {
            throw error;
         }
      }
   }
}
