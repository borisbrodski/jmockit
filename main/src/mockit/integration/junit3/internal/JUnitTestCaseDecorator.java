/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit3.internal;

import java.lang.reflect.*;

import junit.framework.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Provides an startup mock that modifies the JUnit 3.8 test runner so that it calls back to JMockit for each test
 * execution.
 * When that happens, JMockit will assert any expectations set during the test, including expectations specified through
 * {@link Mock} as well as in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = TestCase.class, instantiation = Instantiation.PerMockedInstance)
public final class JUnitTestCaseDecorator extends TestRunnerDecorator
{
   private static final Method setUpMethod;
   private static final Method tearDownMethod;
   private static final Method runTestMethod;
   private static final Field fName;

   static
   {
      try {
         setUpMethod = TestCase.class.getDeclaredMethod("setUp");
         tearDownMethod = TestCase.class.getDeclaredMethod("tearDown");
         runTestMethod = TestCase.class.getDeclaredMethod("runTest");
         fName = TestCase.class.getDeclaredField("fName");
      }
      catch (NoSuchMethodException e) {
         // OK, won't happen.
         throw new RuntimeException(e);
      }
      catch (NoSuchFieldException e) {
         // OK, won't happen.
         throw new RuntimeException(e);
      }

      setUpMethod.setAccessible(true);
      tearDownMethod.setAccessible(true);
      runTestMethod.setAccessible(true);
      fName.setAccessible(true);
   }

   public TestCase it;

   @Mock
   public void runBare() throws Throwable
   {
      updateTestClassState(it, it.getClass());

      TestRun.setRunningIndividualTest(it);
      prepareForNextTest();

      try {
         originalRunBare();
      }
      catch (Throwable t) {
         Utilities.filterStackTrace(t);
         throw t;
      }
   }

   private void originalRunBare() throws Throwable
   {
      setUpMethod.invoke(it);

      Throwable exception = null;

      try {
         runTest();
         exception = endTestExecution(true);
      }
      catch (Throwable running) {
         endTestExecution(false);
         exception = running;
      }
      finally {
         exception = performTearDown(exception);
      }

      if (exception != null) {
         throw exception;
      }
   }

   private Throwable performTearDown(Throwable thrownByTestMethod)
   {
      TestRun.setRunningTestMethod(null);

      try {
         tearDownMethod.invoke(it);
         return thrownByTestMethod;
      }
      catch (Throwable tearingDown) {
         return thrownByTestMethod == null ? tearingDown : thrownByTestMethod;
      }
      finally {
         TestRun.getExecutingTest().setRecordAndReplay(null);
      }
   }

   @Mock(reentrant = true)
   public void runTest() throws Throwable
   {
      String testMethodName = (String) fName.get(it);
      Method testMethod = findTestMethod(testMethodName);

      if (testMethod == null) {
         try {
            runTestMethod.invoke(it);
         }
         catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
         }
         catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
         }

         return;
      }

      TestRun.setRunningTestMethod(testMethod);
      SavePoint savePoint = new SavePoint();
      Object[] args = createInstancesForMockParametersIfAny(it, testMethod, Utilities.NO_ARGS);

      try {
         if (args.length == 0) {
            runTestMethod.invoke(it);
         }
         else {
            testMethod.invoke(it, args);
         }
      }
      catch (InvocationTargetException e) {
         e.fillInStackTrace();
         throw e.getTargetException();
      }
      catch (IllegalAccessException e) {
         e.fillInStackTrace();
         throw e;
      }
      finally {
         rollbackToSavePoint(savePoint);
      }
   }

   private void rollbackToSavePoint(SavePoint savePoint)
   {
      TestRun.enterNoMockingZone();

      try {
         savePoint.rollback();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private Method findTestMethod(String testMethodName)
   {
      for (Method publicMethod : it.getClass().getMethods()) {
         if (publicMethod.getName().equals(testMethodName)) {
            return publicMethod;
         }
      }

      return null;
   }

   private AssertionError endTestExecution(boolean nothingThrownByTestMethod)
   {
      AssertionError expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();

      try {
         if (nothingThrownByTestMethod && expectationsFailure == null) {
            TestRun.verifyExpectationsOnAnnotatedMocks();
         }
      }
      catch (AssertionError e) {
         expectationsFailure = e;
      }
      finally {
         TestRun.resetExpectationsOnAnnotatedMocks();
      }

      TestRun.finishCurrentTestExecution();
      return expectationsFailure;
   }
}
