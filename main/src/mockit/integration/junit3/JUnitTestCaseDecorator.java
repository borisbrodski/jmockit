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
package mockit.integration.junit3;

import java.lang.reflect.*;

import junit.framework.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Provides an startup mock that modifies the JUnit 3.8 test runner so that it calls back to JMockit
 * for each test execution.
 * When that happens, JMockit will assert any expectations set during the test, including
 * expectations specified through {@link Mock} as well as in {@link Expectations} subclasses.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at
 * startup.
 */
@MockClass(realClass = TestCase.class)
public final class JUnitTestCaseDecorator extends TestRunnerDecorator
{
   private static final Object[] NO_ARGS = new Object[0];

   public TestCase it;

   @Mock(reentrant = true)
   public void runBare() throws Throwable
   {
      updateTestClassState(it, it.getClass());

      TestRun.setRunningIndividualTest(it);
      AssertionError error;

      try {
         it.runBare();

         error = RecordAndReplayExecution.endCurrentReplayIfAny();
         TestRun.finishCurrentTestExecution();
      }
      catch (Throwable t) {
         //noinspection ThrowableResultOfMethodCallIgnored
         RecordAndReplayExecution.endCurrentReplayIfAny();
         TestRun.finishCurrentTestExecution();
         Utilities.filterStackTrace(t);
         throw t;
      }

      if (error != null) {
         Utilities.filterStackTrace(error);
         throw error;
      }

      try {
         TestRun.verifyExpectationsOnAnnotatedMocks();
      }
      catch (AssertionError t) {
         Utilities.filterStackTrace(t);
         throw t;
      }
   }

   @Mock
   public void runTest() throws Throwable
   {
      String testMethodName = it.getName();
      Method runMethod = findTestMethod(testMethodName);

      if (runMethod == null) {
         Assert.fail("Method \"" + testMethodName + "\" not found");
      }

      SavePoint savePoint = new SavePoint();
      Object[] args = createInstancesForMockParametersIfAny(it, runMethod, NO_ARGS);

      try {
         runMethod.invoke(it, args);
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
         savePoint.rollback();
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
}
