/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import java.lang.reflect.*;
import java.util.*;

import org.junit.*;
import org.junit.runners.model.*;
import org.junit.runners.Suite.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

// When loading the java agent on demand (without "-javaagent" in the command line, and therefore
// not executing "premain"), we can't redefine the actual JUnit test runner because its methods will
// be in the current thread's execution stack at the time the redefinition occurs.

/**
 * Startup mock that modifies the JUnit 4.5+ test runner so that it calls back to JMockit immediately after every test
 * executes.
 * When that happens, JMockit will assert any expectations set during the test, including expectations specified through
 * {@link Mock} as well as in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = FrameworkMethod.class, instantiation = Instantiation.PerMockedInstance)
public final class JUnit4TestRunnerDecorator extends TestRunnerDecorator
{
   public FrameworkMethod it;
   private boolean generateTestIdForNextBeforeMethod;

   @Mock(reentrant = true)
   public Object invokeExplosively(Object target, Object... params) throws Throwable
   {
      Method method = it.getMethod();
      Class<?> testClass = target == null ? method.getDeclaringClass() : target.getClass();

      if (testClass.isAnnotationPresent(SuiteClasses.class)) {
         setUpClassLevelMocksAndStubs(testClass);
      }
      else {
         updateTestClassState(target, testClass);
      }

      // In case it isn't a test method, but a before/after method:
      if (it.getAnnotation(Test.class) == null) {
         if (generateTestIdForNextBeforeMethod && it.getAnnotation(Before.class) != null) {
            TestRun.prepareForNextTest();
            generateTestIdForNextBeforeMethod = false;
         }

         TestRun.setRunningIndividualTest(target);
         TestRun.setRunningTestMethod(null);

         try {
            return it.invokeExplosively(target, params);
         }
         catch (Throwable t) {
            //noinspection ThrowableResultOfMethodCallIgnored
            RecordAndReplayExecution.endCurrentReplayIfAny();
            Utilities.filterStackTrace(t);
            throw t;
         }
         finally {
            TestRun.setRunningIndividualTest(null);
         }
      }

      if (generateTestIdForNextBeforeMethod) {
         TestRun.prepareForNextTest();
      }

      TestRun.setRunningTestMethod(method);
      generateTestIdForNextBeforeMethod = true;

      try {
         executeTest(target, params);
         return null; // it's a test method, therefore has void return type
      }
      catch (Throwable t) {
         Utilities.filterStackTrace(t);
         throw t;
      }
      finally {
         TestRun.finishCurrentTestExecution();
      }
   }

   private void executeTest(Object target, Object... params) throws Throwable
   {
      SavePoint savePoint = new SavePoint();
      boolean nothingThrownByTest = false;

      try {
         //noinspection AssignmentToMethodParameter
         params = createInstancesForMockParametersIfAny(target, it.getMethod(), params);

         TestRun.setRunningIndividualTest(target);
         it.invokeExplosively(target, params);
         nothingThrownByTest = true;
      }
      finally {
         TestRun.enterNoMockingZone();
         AssertionError expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();

         try {
            if (nothingThrownByTest && expectationsFailure == null) {
               TestRun.verifyExpectationsOnAnnotatedMocks();
            }
         }
         finally {
            TestRun.resetExpectationsOnAnnotatedMocks();
            savePoint.rollback();
            TestRun.exitNoMockingZone();
         }

         if (nothingThrownByTest && expectationsFailure != null) {
            //noinspection ThrowFromFinallyBlock
            throw expectationsFailure;
         }
      }
   }

   @Mock(reentrant = true)
   public void validatePublicVoidNoArg(boolean isStatic, List<Throwable> errors)
   {
      if (!isStatic && it.getMethod().getParameterTypes().length > 0) {
         it.validatePublicVoid(false, errors);
         return;
      }

      it.validatePublicVoidNoArg(isStatic, errors);
   }
}
