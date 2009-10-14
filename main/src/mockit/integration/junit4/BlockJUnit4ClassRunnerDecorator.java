/*
 * JMockit
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
package mockit.integration.junit4;

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
 * Startup mock that modifies the JUnit 4.5+ test runner so that it calls back to JMockit
 * immediately after every test executes. When that happens, JMockit will assert any expectations
 * set during the test, including expectations specified through {@link Mock} as well as in
 * {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it
 * at startup.
 */
@MockClass(realClass = FrameworkMethod.class)
public final class BlockJUnit4ClassRunnerDecorator extends TestRunnerDecorator
{
   public FrameworkMethod it;

   @Mock(reentrant = true)
   public Object invokeExplosively(Object target, Object... params) throws Throwable
   {
      Class<?> testClass = target == null ? it.getMethod().getDeclaringClass() : target.getClass();

      if (testClass.isAnnotationPresent(SuiteClasses.class)) {
         setUpClassLevelMocksAndStubs(testClass);
      }
      else {
         updateTestClassState(target, testClass);
      }

      // In case it isn't a test method, but a before/after method:
      if (it.getAnnotation(Test.class) == null) {
         TestRun.setRunningIndividualTest(target);
         TestRun.setRunningTestMethod(false);

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

      TestRun.setRunningTestMethod(true);

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
         AssertionError expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();

         try {
            if (nothingThrownByTest && expectationsFailure == null) {
               TestRun.verifyExpectationsOnAnnotatedMocks();
            }
         }
         finally {
            savePoint.rollback();
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
