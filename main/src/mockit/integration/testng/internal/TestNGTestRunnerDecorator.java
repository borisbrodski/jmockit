/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng.internal;

import java.lang.reflect.*;

import org.testng.annotations.*;
import org.testng.annotations.Parameters;
import org.testng.internal.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Provides an startup mock that modifies the TestNG 5.9/5.10/5.11 test runner so that it calls back to JMockit for each
 * test execution.
 * When that happens, JMockit will assert any expectations set during the test, including expectations specified through
 * {@link Mock} as well as in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = MethodHelper.class, instantiation = Instantiation.PerMockSetup)
public final class TestNGTestRunnerDecorator extends TestRunnerDecorator
{
   private final ThreadLocal<SavePoint> savePoint = new ThreadLocal<SavePoint>();
   private boolean generateTestIdForNextBeforeMethod;

   public TestNGTestRunnerDecorator()
   {
      Mockit.stubOutClass(org.testng.internal.Parameters.class, "checkParameterTypes");
   }

   @SuppressWarnings({"UnusedDeclaration"})
   @Mock(reentrant = true)
   public Object invokeMethod(Method method, Object instance, Object[] parameters)
      throws InvocationTargetException, IllegalAccessException
   {
      Class<?> testClass = instance == null ? method.getDeclaringClass() : instance.getClass();
      updateTestClassState(instance, testClass);

      // In case it isn't a test method, but a before/after method:
      if (!isTestMethod(testClass, method)) {
         if (generateTestIdForNextBeforeMethod && method.isAnnotationPresent(BeforeMethod.class)) {
            TestRun.prepareForNextTest();
            generateTestIdForNextBeforeMethod = false;
         }

         TestRun.setRunningIndividualTest(instance);
         TestRun.setRunningTestMethod(null);

         try {
            return MethodHelper.invokeMethod(method, instance, parameters);
         }
         catch (InvocationTargetException t) {
            //noinspection ThrowableResultOfMethodCallIgnored
            RecordAndReplayExecution.endCurrentReplayIfAny();
            Utilities.filterStackTrace(t);
            throw t;
         }
         finally {
            TestRun.setRunningIndividualTest(null);
         }
      }

      savePoint.set(new SavePoint());

      if (!isMethodWithParametersProvidedByTestNG(method)) {
         //noinspection AssignmentToMethodParameter
         parameters = createInstancesForMockParametersIfAny(this, method, parameters);
      }

      if (generateTestIdForNextBeforeMethod) {
         TestRun.prepareForNextTest();
      }

      TestRun.setRunningIndividualTest(instance);
      TestRun.setRunningTestMethod(method);
      generateTestIdForNextBeforeMethod = true;

      return executeTestMethod(instance, method, parameters);
   }

   private boolean isTestMethod(Class<?> testClass, Method method)
   {
      return
         method.isAnnotationPresent(Test.class) ||
         testClass.isAnnotationPresent(Test.class) && Modifier.isPublic(method.getModifiers()) &&
         method.getDeclaredAnnotations().length == 0;
   }

   private boolean isMethodWithParametersProvidedByTestNG(Method method)
   {
      if (method.isAnnotationPresent(Parameters.class)) {
         return true;
      }

      Test testMetadata = method.getAnnotation(Test.class);

      return testMetadata != null && testMetadata.dataProvider().length() > 0;
   }

   private Object executeTestMethod(Object instance, Method method, Object[] parameters)
      throws InvocationTargetException, IllegalAccessException
   {
      try {
         Object result = MethodHelper.invokeMethod(method, instance, parameters);

         AssertionError error = RecordAndReplayExecution.endCurrentReplayIfAny();

         if (error != null) {
            Utilities.filterStackTrace(error);
            throw error;
         }

         TestRun.verifyExpectationsOnAnnotatedMocks();

         return result;
      }
      catch (InvocationTargetException e) {
         RecordAndReplayExecution.endCurrentReplayIfAny();
         Utilities.filterStackTrace(e.getCause());
         throw e;
      }
      catch (IllegalAccessException e) {
         RecordAndReplayExecution.endCurrentReplayIfAny();
         throw e;
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
