/*
 * JMockit
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
 * Provides an startup mock that modifies the TestNG 5.9/5.10/5.11 test runner so that it calls back
 * to JMockit for each test execution.
 * When that happens, JMockit will assert any expectations set during the test, including
 * expectations specified through {@link Mock} as well as in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at
 * startup.
 */
@MockClass(realClass = MethodHelper.class)
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
            TestRun.generateIdForNextTest();
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
         TestRun.generateIdForNextTest();
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
