/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng.internal;

import java.lang.reflect.*;

import org.testng.*;
import org.testng.annotations.*;
import org.testng.internal.Parameters;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Provides callbacks to be called by the TestNG 5.14+ test runner for each test execution.
 * JMockit will then assert any expectations set during the test, including those specified through {@link Mock} and
 * those recorded in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. It will be automatically loaded at startup.
 */
public final class TestNGRunnerDecorator extends TestRunnerDecorator implements IConfigurable, IHookable
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

   public TestNGRunnerDecorator()
   {
      savePoint = new ThreadLocal<SavePoint>();
      Mockit.setUpMocks(MockParameters.class);
      shouldPrepareForNextTest = true;
   }

   public void run(IConfigureCallBack callBack, ITestResult testResult)
   {
      Object instance = testResult.getInstance();
      Class<?> testClass = testResult.getTestClass().getRealClass();

      updateTestClassState(instance, testClass);

      if (shouldPrepareForNextTest && testResult.getMethod().isBeforeMethodConfiguration()) {
         prepareForNextTest();
         shouldPrepareForNextTest = false;
      }

      TestRun.setRunningIndividualTest(instance);
      TestRun.setRunningTestMethod(null);

      try {
         callBack.runConfigurationMethod(testResult);
      }
      catch (RuntimeException t) {
         RecordAndReplayExecution.endCurrentReplayIfAny();
         Utilities.filterStackTrace(t);
         throw t;
      }
      finally {
         if (testResult.getMethod().isAfterMethodConfiguration()) {
            TestRun.getExecutingTest().setRecordAndReplay(null);
         }
      }
   }

   public void run(IHookCallBack callBack, ITestResult testResult)
   {
      Object instance = testResult.getInstance();
      Class<?> testClass = testResult.getTestClass().getRealClass();

      updateTestClassState(instance, testClass);
      savePoint.set(new SavePoint());

      Method method = testResult.getMethod().getMethod();

      if (!isMethodWithParametersProvidedByTestNG(method)) {
         Object[] parameters = testResult.getParameters();
         Object[] mockParameters = createInstancesForMockParametersIfAny(this, method, parameters);
         System.arraycopy(mockParameters, 0, parameters, 0, parameters.length);
      }

      if (shouldPrepareForNextTest) {
         prepareForNextTest();
      }

      TestRun.setRunningIndividualTest(instance);
      TestRun.setRunningTestMethod(method);
      shouldPrepareForNextTest = true;

      executeTestMethod(callBack, testResult);
   }

   private void executeTestMethod(IHookCallBack callBack, ITestResult testResult)
   {
      try {
         callBack.runTestMethod(testResult);

         AssertionError error = RecordAndReplayExecution.endCurrentReplayIfAny();

         if (error != null) {
            Utilities.filterStackTrace(error);
            throw error;
         }

         TestRun.verifyExpectationsOnAnnotatedMocks();
      }
      finally {
         cleanUpAfterTestMethodExecution();
      }
   }

   private void cleanUpAfterTestMethodExecution()
   {
      TestRun.enterNoMockingZone();

      try {
         TestRun.resetExpectationsOnAnnotatedMocks();
         TestRun.finishCurrentTestExecution();
         savePoint.get().rollback();
         savePoint.set(null);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
