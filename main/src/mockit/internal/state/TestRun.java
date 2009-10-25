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
package mockit.internal.state;

import java.security.*;

import mockit.internal.expectations.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.capturing.*;

/**
 * A singleton which stores several data structures which in turn hold global state for individual
 * test methods, test classes, and for the test run as a whole.
 */
public final class TestRun
{
   private static final TestRun instance = new TestRun();

   private TestRun() {}

   // Fields with global state ////////////////////////////////////////////////////////////////////

   private Class<?> currentTestClass;
   private Object currentTestInstance;
   private boolean runningTestMethod;

   private CaptureOfImplementationsForTestClass captureOfSubtypes;
   private SharedFieldTypeRedefinitions sharedFieldTypeRedefinitions;

   private final ProxyClasses proxyClasses = new ProxyClasses();
   private final MockFixture mockFixture = new MockFixture();

   private final ExecutingTest executingTest = new ExecutingTest();
   private final MockClasses mockClasses = new MockClasses();

   // Static "getters" for global state ///////////////////////////////////////////////////////////

   public static Class<?> getCurrentTestClass() { return instance.currentTestClass; }

   public static Object getCurrentTestInstance() { return instance.currentTestInstance; }

   public static boolean isRunningTestCode(ProtectionDomain protectionDomain)
   {
      if (instance.currentTestInstance != null) {
         return protectionDomain == instance.currentTestClass.getProtectionDomain();
      }

      return
         protectionDomain != null &&
         !protectionDomain.getCodeSource().getLocation().getPath().endsWith(".jar");
   }

   public static CaptureOfImplementationsForTestClass getCaptureOfSubtypes() { return instance.captureOfSubtypes; }

   public static SharedFieldTypeRedefinitions getSharedFieldTypeRedefinitions()
   {
      return instance.sharedFieldTypeRedefinitions;
   }

   public static ProxyClasses proxyClasses() { return instance.proxyClasses; }

   public static MockFixture mockFixture() { return instance.mockFixture; }

   public static ExecutingTest getExecutingTest() { return instance.executingTest; }

   public static RecordAndReplayExecution getRecordAndReplayForRunningTest(boolean create)
   {
      if (instance.currentTestInstance == null) {
         return null;
      }

      return getExecutingTest().getRecordAndReplay(instance.runningTestMethod && create);
   }

   public static MockClasses getMockClasses() { return instance.mockClasses; }

   public static void verifyExpectationsOnAnnotatedMocks()
   {
      getMockClasses().getMockStates().verifyExpectations();
   }

   // Static "mutators" for global state //////////////////////////////////////////////////////////

   public static void setCurrentTestClass(Class<?> testClass)
   {
      instance.currentTestClass = testClass;
   }

   public static void setRunningTestMethod(boolean runningTestMethod)
   {
      instance.runningTestMethod = runningTestMethod;
   }

   public static void setRunningIndividualTest(Object testInstance)
   {
      instance.currentTestInstance = testInstance;
   }

   public static void setCaptureOfSubtypes(CaptureOfImplementationsForTestClass captureOfSubtypes)
   {
      instance.captureOfSubtypes = captureOfSubtypes;
   }

   public static void setSharedFieldTypeRedefinitions(SharedFieldTypeRedefinitions redefinitions)
   {
      instance.sharedFieldTypeRedefinitions = redefinitions;
   }

   public static void finishCurrentTestExecution()
   {
      instance.currentTestInstance = null;
      instance.runningTestMethod = false;
      instance.executingTest.finishExecution();
   }

   // Methods to be called only from generated bytecode or from the MockingBridge /////////////////

   public static Object getMock(int index)
   {
      return instance.mockClasses.regularMocks.getMock(index);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static Object getStartupMock(int index)
   {
      return instance.mockClasses.startupMocks.getMock(index);
   }

   public static Object getMock(Class<?> mockClass, Object mockedInstance)
   {
      return instance.mockClasses.regularMocks.getMock(mockClass, mockedInstance);
   }

   public static boolean updateMockState(String mockClassDesc, int mockIndex)
   {
      return instance.mockClasses.annotatedMockStates.updateMockState(mockClassDesc, mockIndex);
   }

   public static void exitReentrantMock(String mockClassDesc, int mockIndex)
   {
      instance.mockClasses.annotatedMockStates.exitReentrantMock(mockClassDesc, mockIndex);
   }
}
