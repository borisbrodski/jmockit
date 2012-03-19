/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng.internal;

import java.lang.reflect.*;

import static mockit.internal.util.StackTrace.*;
import org.testng.*;
import org.testng.annotations.*;
import org.testng.internal.Parameters;

import mockit.*;
import mockit.integration.internal.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;

/**
 * Provides callbacks to be called by the TestNG 5.14+ test runner for each test execution.
 * JMockit will then assert any expectations set during the test, including those specified through {@link Mock} and
 * those recorded in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. It will be automatically loaded at startup.
 */
public final class TestNGRunnerDecorator extends TestRunnerDecorator
   implements IConfigurable, IInvokedMethodListener, ISuiteListener
{
   @MockClass(realClass = Parameters.class, stubs = "checkParameterTypes")
   public static final class MockParameters
   {
      @Mock(reentrant = true)
      public static Object getInjectedParameter(Class<?> c, Method method, ITestContext context, ITestResult testResult)
      {
         Object value = Parameters.getInjectedParameter(c, method, context, testResult);

         if (value != null) {
            return value;
         }

         return method == null || isMethodWithParametersProvidedByTestNG(method) ? null : "";
      }
   }

   private static boolean isMethodWithParametersProvidedByTestNG(Method method)
   {
      if (method.isAnnotationPresent(org.testng.annotations.Parameters.class)) {
         return true;
      }

      Test testMetadata = method.getAnnotation(Test.class);

      return testMetadata != null && testMetadata.dataProvider().length() > 0;
   }

   private final ThreadLocal<SavePoint> savePoint;
   private boolean shouldPrepareForNextTest;

   public static void registerWithTestNG(TestNG testNG)
   {
      Object runnerDecorator = new TestNGRunnerDecorator();
      testNG.addListener(runnerDecorator);
   }

   public TestNGRunnerDecorator()
   {
      savePoint = new ThreadLocal<SavePoint>();
      Mockit.setUpMocks(MockParameters.class);
      shouldPrepareForNextTest = true;
   }

   public void run(IConfigureCallBack callBack, ITestResult testResult)
   {
      Object testInstance = testResult.getInstance();
      Class<?> testClass = testResult.getTestClass().getRealClass();

      updateTestClassState(testInstance, testClass);

      if (shouldPrepareForNextTest && testResult.getMethod().isBeforeMethodConfiguration()) {
         prepareForNextTest();
         shouldPrepareForNextTest = false;
      }

      TestRun.setRunningIndividualTest(testInstance);
      TestRun.setRunningTestMethod(null);

      try {
         callBack.runConfigurationMethod(testResult);
      }
      catch (RuntimeException t) {
         RecordAndReplayExecution.endCurrentReplayIfAny();
         filterStackTrace(t);
         throw t;
      }
      finally {
         if (testResult.getMethod().isAfterMethodConfiguration()) {
            TestRun.getExecutingTest().setRecordAndReplay(null);
         }
      }
   }

   public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult)
   {
      if (!invokedMethod.isTestMethod()) {
         return;
      }

      Object testInstance = testResult.getInstance();

      TestRun.enterNoMockingZone();

      Method method;

      try {
         Class<?> testClass = testResult.getTestClass().getRealClass();

         updateTestClassState(testInstance, testClass);
         savePoint.set(new SavePoint());

         if (shouldPrepareForNextTest) {
            prepareForNextTest();
         }

         shouldPrepareForNextTest = true;

         //noinspection deprecation
         method = testResult.getMethod().getMethod();

         if (!isMethodWithParametersProvidedByTestNG(method)) {
            Object[] parameters = testResult.getParameters();
            Object[] mockParameters = createInstancesForMockParameters(testInstance, method);

            if (mockParameters != null) {
               System.arraycopy(mockParameters, 0, parameters, 0, parameters.length);
            }
         }

         createInstancesForTestedFields(testInstance);
      }
      finally {
         TestRun.exitNoMockingZone();
      }

      TestRun.setRunningIndividualTest(testInstance);
      TestRun.setRunningTestMethod(method);
   }

   public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult)
   {
      if (!invokedMethod.isTestMethod()) {
         return;
      }

      SavePoint testMethodSavePoint = savePoint.get();
      savePoint.set(null);

      Throwable thrownByTest = testResult.getThrowable();

      try {
         if (thrownByTest == null) {
            concludeTestExecutionWithNothingThrown(testMethodSavePoint, testResult);
         }
         else if (thrownByTest instanceof TestException) {
            concludeTestExecutionWithExpectedExceptionNotThrown(invokedMethod, testMethodSavePoint, testResult);
         }
         else if (testResult.isSuccess()) {
            concludeTestExecutionWithExpectedExceptionThrown(testMethodSavePoint, testResult, thrownByTest);
         }
         else {
            concludeTestExecutionWithUnexpectedExceptionThrown(testMethodSavePoint, thrownByTest);
         }
      }
      finally {
         TestRun.finishCurrentTestExecution(false);
      }
   }

   private void concludeTestExecutionWithNothingThrown(SavePoint testMethodSavePoint, ITestResult testResult)
   {
      try {
         concludeTestMethodExecution(testMethodSavePoint, null, false);
      }
      catch (Throwable t) {
         filterStackTrace(t);
         testResult.setThrowable(t);
         testResult.setStatus(ITestResult.FAILURE);
      }
   }

   private void concludeTestExecutionWithExpectedExceptionNotThrown(
      IInvokedMethod invokedMethod, SavePoint testMethodSavePoint, ITestResult testResult)
   {
      try {
         concludeTestMethodExecution(testMethodSavePoint, null, false);
      }
      catch (Throwable t) {
         filterStackTrace(t);

         if (isExpectedException(invokedMethod, t)) {
            testResult.setThrowable(null);
            testResult.setStatus(ITestResult.SUCCESS);
         }
         else {
            filterStackTrace(testResult.getThrowable());
         }
      }
   }

   private void concludeTestExecutionWithExpectedExceptionThrown(
      SavePoint testMethodSavePoint, ITestResult testResult, Throwable thrownByTest)
   {
      filterStackTrace(thrownByTest);

      try {
         concludeTestMethodExecution(testMethodSavePoint, thrownByTest, true);
      }
      catch (Throwable t) {
         if (t != thrownByTest) {
            filterStackTrace(t);
            testResult.setThrowable(t);
            testResult.setStatus(ITestResult.FAILURE);
         }
      }
   }

   private void concludeTestExecutionWithUnexpectedExceptionThrown(
      SavePoint testMethodSavePoint, Throwable thrownByTest)
   {
      filterStackTrace(thrownByTest);

      try {
         concludeTestMethodExecution(testMethodSavePoint, thrownByTest, false);
      }
      catch (Throwable ignored) {}
   }

   private boolean isExpectedException(IInvokedMethod invokedMethod, Throwable thrownByTest)
   {
      Method testMethod = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
      Class<?>[] expectedExceptions = testMethod.getAnnotation(Test.class).expectedExceptions();
      Class<? extends Throwable> thrownExceptionType = thrownByTest.getClass();

      for (Class<?> expectedException : expectedExceptions) {
         if (expectedException.isAssignableFrom(thrownExceptionType)) {
            return true;
         }
      }

      return false;
   }

   public void onStart(ISuite suite) {}

   public void onFinish(ISuite suite)
   {
      TestRun.enterNoMockingZone();

      try {
         TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
