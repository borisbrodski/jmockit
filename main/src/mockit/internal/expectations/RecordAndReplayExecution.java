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
import java.util.concurrent.locks.*;

import mockit.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class RecordAndReplayExecution
{
   public static final ReentrantLock LOCK = new ReentrantLock();

   private final LocalFieldTypeRedefinitions redefinitions;
   private final Map<Type, Object> typesAndTargetObjects;
   private final DynamicPartialMocking dynamicPartialMocking;

   final PhasedExecutionState executionState;
   final int lastExpectationIndexInPreviousReplayPhase;

   final FailureState failureState = new FailureState();

   private RecordPhase recordPhase;
   private ReplayPhase replayPhase;
   private VerificationPhase verificationPhase;

   public RecordAndReplayExecution()
   {
      executionState = new PhasedExecutionState();
      lastExpectationIndexInPreviousReplayPhase = 0;
      redefinitions = null;
      typesAndTargetObjects = new HashMap<Type, Object>(1);
      dynamicPartialMocking = null;
      validateRecordingContext();
      validateThereIsAtLeastOneMockedTypeInScope();
      discoverDuplicateMockedTypesForAutomaticMockInstanceMatching();
      replayPhase = new ReplayPhase(this);
   }

   private int getLastExpectationIndexInPreviousReplayPhase()
   {
      return replayPhase == null ? -1 : replayPhase.currentStrictExpectationIndex;
   }

   private void validateRecordingContext()
   {
      if (TestRun.getSharedFieldTypeRedefinitions() == null) {
         String msg = Startup.wasInitializedOnDemand() ?
            "JMockit wasn't properly initialized; check that jmockit.jar precedes junit.jar in the classpath " +
            "(if using JUnit; if not, check the documentation)" : 
            "Invalid context for the recording of expectations";
         throw new IllegalStateException(msg);
      }
   }

   public RecordAndReplayExecution(Expectations targetObject, Object... classesOrInstancesToBePartiallyMocked)
   {
      TestRun.enterNoMockingZone();
      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);

      try {
         RecordAndReplayExecution previous = TestRun.getExecutingTest().getRecordAndReplay();

         if (previous == null) {
            executionState = new PhasedExecutionState();
            lastExpectationIndexInPreviousReplayPhase = 0;
         }
         else {
            executionState = previous.executionState;
            lastExpectationIndexInPreviousReplayPhase = previous.getLastExpectationIndexInPreviousReplayPhase();
         }

         recordPhase = new RecordPhase(this, targetObject instanceof NonStrictExpectations);

         LocalFieldTypeRedefinitions redefs = new LocalFieldTypeRedefinitions(targetObject);
         typesAndTargetObjects = previous == null ? new HashMap<Type, Object>(2) : previous.typesAndTargetObjects;
         redefineFieldTypes(redefs);
         redefinitions = redefs.getTypesRedefined() == 0 ? null : redefs;

         dynamicPartialMocking = applyDynamicPartialMocking(classesOrInstancesToBePartiallyMocked);

         validateRecordingContext();
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
         ParameterTypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterTypeRedefinitions();

         if (paramTypeRedefinitions == null || paramTypeRedefinitions.getTypesRedefined() == 0) {
            throw new IllegalStateException(
               "No mocked types in scope; please declare mock fields or parameters for the types you need mocked");
         }
      }
   }

   private void discoverDuplicateMockedTypesForAutomaticMockInstanceMatching()
   {
      List<Class<?>> fields = TestRun.getSharedFieldTypeRedefinitions().getTargetClasses();
      List<Class<?>> targetClasses = new ArrayList<Class<?>>(fields);

      if (redefinitions != null) {
         targetClasses.addAll(redefinitions.getTargetClasses());
      }

      ParameterTypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterTypeRedefinitions();

      if (paramTypeRedefinitions != null) {
         targetClasses.addAll(paramTypeRedefinitions.getTargetClasses());
      }

      if (dynamicPartialMocking != null) {
         addDynamicallyMockedTargetClasses(targetClasses);
      }

      executionState.discoverMockedTypesToMatchOnInstances(targetClasses);
   }

   private void addDynamicallyMockedTargetClasses(List<Class<?>> targetClasses)
   {
      List<Class<?>> staticallyMockedClasses = new ArrayList<Class<?>>(targetClasses);
      List<Class<?>> dynamicallyMockedClasses = dynamicPartialMocking.getTargetClasses();

      for (Class<?> dynamicallyMockedClass : dynamicallyMockedClasses) {
         if (!staticallyMockedClasses.contains(dynamicallyMockedClass)) {
            targetClasses.add(dynamicallyMockedClass);
         }
      }
   }

   public Map<Type, Object> getLocalMocks() { return typesAndTargetObjects; }

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

   AssertionError getErrorThrown() { return failureState.getErrorThrown(); }
   void setErrorThrown(AssertionError error) { failureState.setErrorThrown(error); }

   /**
    * Only to be called from generated bytecode or from the Mocking Bridge.
    */
   public static Object recordOrReplay(
      Object mock, int mockAccess, String classDesc, String mockDesc, int executionMode, Object... args)
      throws Throwable
   {
      ExecutingTest executingTest = TestRun.getExecutingTest();

      if (executingTest.isShouldIgnoreMockingCallbacks()) {
         return executionMode == 0 ? DefaultValues.computeForType(mockDesc) : Void.class;
      }

      executingTest.registerAdditionalMocksFromFinalLocalMockFieldsIfAny();

      if (executionMode == 2 && (mock == null || !executingTest.isInjectableMock(mock))) {
         return Void.class;
      }

      LOCK.lock();

      try {
         RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(true);

         if (mockDesc.startsWith("<init>") && handleCallToConstructor(instance, mock, classDesc)) {
            return executionMode == 0 || executingTest.isInjectableMock(mock) ? null : Void.class;
         }

         if (instance == null) {
            // This can occur when a constructor of the mocked class is called in a mock field assignment expression,
            // or during the restoration of mocked classes that is done between test classes.
            return DefaultValues.computeForReturnType(mockDesc);
         }

         Phase currentPhase = instance.getCurrentPhase();
         instance.failureState.clearErrorThrown();

         boolean withRealImpl = executionMode == 1;
         Object result = currentPhase.handleInvocation(mock, mockAccess, classDesc, mockDesc, withRealImpl, args);

         instance.failureState.reportErrorThrownIfAny();

         return result;
      }
      finally {
         LOCK.unlock();
      }
   }

   private static boolean handleCallToConstructor(RecordAndReplayExecution instance, Object mock, String classDesc)
   {
      if (TestRun.getCurrentTestInstance() != null) {
         FieldTypeRedefinitions fieldTypeRedefs = instance == null ? null : instance.redefinitions;

         if (fieldTypeRedefs != null && fieldTypeRedefs.captureNewInstanceForApplicableMockField(mock)) {
            return true;
         }

         ParameterTypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterTypeRedefinitions();

         if (paramTypeRedefinitions != null) {
            CaptureOfNewInstancesForParameters paramTypeCaptures = paramTypeRedefinitions.getCaptureOfNewInstances();

            if (paramTypeCaptures != null && paramTypeCaptures.captureNewInstanceForApplicableMockParameter(mock)) {
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
         if (failureState.getErrorThrown() != null) {
            // This can only happen when called from a catch/finally block.
            throw failureState.getErrorThrown();
         }

         return replay;
      }

      return verification;
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

      List<Expectation> expectations = replayPhase.nonStrictInvocations;
      List<Object[]> invocationArguments = replayPhase.nonStrictInvocationArguments;
      verificationPhase =
         inOrder ?
            new OrderedVerificationPhase(this, expectations, invocationArguments) :
            new UnorderedVerificationPhase(this, expectations, invocationArguments);

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

      if (error == null) {
         error = failureState.getErrorThrownInAnotherThreadIfAny(error);
      }

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
