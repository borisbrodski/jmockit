/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import java.lang.reflect.*;

import org.testng.*;
import org.testng.annotations.*;
import org.testng.internal.Parameters;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * A test listener implementation for TestNG that will properly initialize JMockit before any tests
 * are executed. This class is useful if running on JDK 1.6+ and the JVM argument
 * "-javaagent:jmockit.jar" isn't being used.
 * <p/>
 * One way to configure TestNG to use this class as a listener is to pass
 * "-listener mockit.integration.testng.Initializer" as a command line argument.
 * Another way would be through {@code testng.xml} configuration.
 * Please check the <a href="http://testng.org/doc/documentation-main.html#running-testng">TestNG
 * documentation</a> for more details.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/RunningTests.html">Tutorial</a>
 */
public final class Initializer extends TestRunnerDecorator implements IConfigurable, IHookable
{
   static
   {
      Startup.initializeIfNeeded();
      Mockit.setUpMocks(MockParameters.class);
   }

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

         return isMethodWithParametersProvidedByTestNG(method) ? null : "";
      }
   }

   private final ThreadLocal<SavePoint> savePoint = new ThreadLocal<SavePoint>();
   private boolean generateTestIdForNextBeforeMethod;

   public void run(IConfigureCallBack callBack, ITestResult testResult)
   {
      Object instance = testResult.getInstance();
      Class<?> testClass = testResult.getTestClass().getRealClass();

      updateTestClassState(instance, testClass);

      if (generateTestIdForNextBeforeMethod && testResult.getMethod().isBeforeMethodConfiguration()) {
         TestRun.prepareForNextTest();
         generateTestIdForNextBeforeMethod = false;
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
         TestRun.setRunningIndividualTest(null);
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

      if (generateTestIdForNextBeforeMethod) {
         TestRun.prepareForNextTest();
      }

      TestRun.setRunningIndividualTest(instance);
      TestRun.setRunningTestMethod(method);
      generateTestIdForNextBeforeMethod = true;

      executeTestMethod(callBack, testResult);
   }

   private static boolean isMethodWithParametersProvidedByTestNG(Method method)
   {
      if (method.isAnnotationPresent(org.testng.annotations.Parameters.class)) {
         return true;
      }

      Test testMetadata = method.getAnnotation(Test.class);

      return testMetadata.dataProvider().length() > 0;
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
