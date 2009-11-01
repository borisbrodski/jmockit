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

import mockit.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class RecordAndReplayExecution
{
   private final LocalFieldTypeRedefinitions redefinitions;
   private final DynamicPartialMocking dynamicPartialMocking;
   final List<Expectation> expectations;
   final List<Expectation> nonStrictExpectations;
   final Map<Object, Object> recordToReplayInstanceMap;
   private RecordPhase recordPhase;
   private ReplayPhase replayPhase;
   private VerificationPhase verificationPhase;
   int lastExpectationIndexInPreviousReplayPhase;

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

   public RecordAndReplayExecution(
      Object targetObject, Object... classesOrInstancesToBePartiallyMocked)
   {
      TestRun.enterNoMockingZone();

      try {
         RecordAndReplayExecution previous = TestRun.getExecutingTest().setRecordAndReplay(null);
         Class<?> enclosingClassForTargetObject = targetObject.getClass().getEnclosingClass();

         if (previous == null || enclosingClassForTargetObject == null) {
            expectations = new ArrayList<Expectation>();
            nonStrictExpectations = new ArrayList<Expectation>();
            recordToReplayInstanceMap = new IdentityHashMap<Object, Object>();
         }
         else {
            expectations = previous.expectations;
            nonStrictExpectations = previous.nonStrictExpectations;
            recordToReplayInstanceMap = previous.recordToReplayInstanceMap;
            lastExpectationIndexInPreviousReplayPhase =
               previous.replayPhase == null ?
                  -1 : previous.replayPhase.currentStrictExpectationIndex;
         }

         recordPhase = new RecordPhase(this, targetObject instanceof NonStrictExpectations);

         redefinitions =
            enclosingClassForTargetObject == null ||
            enclosingClassForTargetObject == TestRun.class ?
               null : redefineFieldTypes(targetObject);

         dynamicPartialMocking = applyDynamicPartialMocking(classesOrInstancesToBePartiallyMocked);

         TestRun.getExecutingTest().setRecordAndReplay(this);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private LocalFieldTypeRedefinitions redefineFieldTypes(Object targetObject)
   {
      LocalFieldTypeRedefinitions typeRedefinitions = new LocalFieldTypeRedefinitions(targetObject);

      //noinspection CatchGenericClass
      try {
         typeRedefinitions.redefineTypesForNestedClass();
         return typeRedefinitions;
      }
      catch (Error e) {
         typeRedefinitions.cleanUp();
         Utilities.filterStackTrace(e);
         throw e;
      }
      catch (RuntimeException e) {
         typeRedefinitions.cleanUp();
         Utilities.filterStackTrace(e);
         throw e;
      }
   }

   private DynamicPartialMocking applyDynamicPartialMocking(Object... classesOrInstances)
   {
      if (
         classesOrInstances == null || classesOrInstances.length == 0 ||
         classesOrInstances.length == 1 && classesOrInstances[0] == Boolean.TRUE
      ) {
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
      Object mock, int mockAccess, String classDesc, String mockDesc, Object... args)
      throws Throwable
   {
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

      Object result = currentPhase.handleInvocation(mock, mockAccess, classDesc, mockDesc, args);

      if (instance.errorThrown != null) {
         throw instance.errorThrown;
      }

      return result;
   }

   private static boolean handleCallToConstructor(
      RecordAndReplayExecution instance, Object mock, String classDesc)
   {
      if (isCallToSuperClassConstructor(mock, classDesc)) {
         return true;
      }

      if (TestRun.getCurrentTestInstance() != null) {
         FieldTypeRedefinitions fieldTypeRedefs = instance == null ? null : instance.redefinitions;

         if (
            fieldTypeRedefs != null &&
            fieldTypeRedefs.captureNewInstanceForApplicableMockField(mock)
         ) {
            return true;
         }

         CaptureOfNewInstancesForParameters paramTypeCaptures =
            TestRun.getExecutingTest().getCaptureOfNewInstancesForParameters();

         if (
            paramTypeCaptures != null &&
            paramTypeCaptures.captureNewInstanceForApplicableMockParameter(mock)
         ) {
            return true;
         }

         FieldTypeRedefinitions sharedFieldTypeRedefs = TestRun.getSharedFieldTypeRedefinitions();

         if (sharedFieldTypeRedefs.captureNewInstanceForApplicableMockField(mock)) {
            return true;
         }
      }

      return false;
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

   void addExpectation(Expectation expectation, boolean nonStrictInvocation)
   {
      ExpectedInvocation invocation = expectation.expectedInvocation;

      if (nonStrictInvocation) {
         nonStrictExpectations.add(expectation);
      }
      else {
         expectations.add(expectation);
      }

      if (dynamicPartialMocking != null) {
         dynamicPartialMocking.addRecordedInvocation(invocation);
      }
   }

   public void endRecording()
   {
      if (replayPhase == null) {
         recordPhase = null;
         replayPhase = new ReplayPhase(this);

         if (dynamicPartialMocking != null) {
            dynamicPartialMocking.restoreNonRecordedMethodsAndConstructors();
         }
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

   public AssertionError endReplay()
   {
      if (replayPhase == null) {
         throw new IllegalStateException("Not in the replay phase yet");
      }

      return endReplayPhaseExecution();
   }

   private AssertionError endReplayPhaseExecution()
   {
      if (redefinitions != null) {
         redefinitions.cleanUp();
      }

      TestRun.getExecutingTest().setRecordAndReplay(null);
      return replayPhase.endExecution();
   }

   public static AssertionError endCurrentReplayIfAny()
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(false);
      return instance == null ? null : instance.endExecution();
   }

   private AssertionError endExecution()
   {
      endRecording();

      AssertionError error = endReplayPhaseExecution();

      if (error == null && verificationPhase != null) {
         error = verificationPhase.endVerification();
         verificationPhase = null;
      }

      return error;
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static void endInvocations()
   {
      TestRun.enterNoMockingZone();

      try {
         RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(true);

         if (instance != null) {
            if (instance.verificationPhase == null) {
               instance.endRecording();
            }
            else {
               AssertionError error = instance.verificationPhase.endVerification();
               instance.verificationPhase = null;

               if (error != null) {
                  throw error;
               }
            }
         }
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
