/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Collections.*;

import mockit.internal.annotations.*;
import mockit.internal.capturing.*;
import mockit.internal.expectations.*;
import mockit.internal.expectations.mocking.*;

/**
 * A singleton which stores several data structures which in turn hold global state for individual
 * test methods, test classes, and for the test run as a whole.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class TestRun
{
   private static final TestRun STARTUP_INSTANCE = new TestRun();
   private static final Map<ClassLoader, TestRun> INSTANCES = synchronizedMap(new HashMap<ClassLoader, TestRun>());
   static
   {
      INSTANCES.put(ClassLoader.getSystemClassLoader(), STARTUP_INSTANCE);
   }

   private static TestRun getInstance()
   {
      ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
      TestRun instance = INSTANCES.get(contextCL);

      // Certain runtime environments (OpenEJB, at least) change the context class loader to a child of the system
      // class loader, so we try looking up the parent class loader before creating a new TestRun instance.
      if (instance == null) {
         instance = INSTANCES.get(contextCL.getParent());
      }

      if (instance == null) {
         instance = new TestRun();
         INSTANCES.put(contextCL, instance);
      }

      return instance;
   }

   private TestRun() {}

   // Fields with global state ////////////////////////////////////////////////////////////////////////////////////////

   private static final ThreadLocal<Integer> noMockingCount = new ThreadLocal<Integer>()
   {
      @Override
      protected Integer initialValue() { return 0; }

      @Override
      public void set(Integer valueToAdd)
      {
         super.set(get() + valueToAdd);
      }
   };

   // Used only by the Coverage tool:
   private int testId;

   private Class<?> currentTestClass;
   private Object currentTestInstance;
   private Method runningTestMethod;

   private SavePoint savePointForTestClass;
   private CaptureOfImplementationsForTestClass captureOfSubtypes;
   private SharedFieldTypeRedefinitions sharedFieldTypeRedefinitions;

   private final ProxyClasses proxyClasses = new ProxyClasses();
   private final MockFixture mockFixture = new MockFixture();

   private final ExecutingTest executingTest = new ExecutingTest();
   private final MockClasses mockClasses = new MockClasses();

   // Static "getters" for global state ///////////////////////////////////////////////////////////////////////////////

   public static boolean isInsideNoMockingZone() { return noMockingCount.get() > 0; }

   public static Class<?> getCurrentTestClass() { return getInstance().currentTestClass; }

   public static Object getCurrentTestInstance() { return getInstance().currentTestInstance; }

   public static int getTestId() { return getInstance().testId; }

   public static SavePoint getSavePointForTestClass()
   {
      return getInstance().savePointForTestClass;
   }

   public static CaptureOfImplementationsForTestClass getCaptureOfSubtypes()
   {
      return getInstance().captureOfSubtypes;
   }

   public static SharedFieldTypeRedefinitions getSharedFieldTypeRedefinitions()
   {
      return getInstance().sharedFieldTypeRedefinitions;
   }

   public static ProxyClasses proxyClasses() { return getInstance().proxyClasses; }

   public static MockFixture mockFixture() { return getInstance().mockFixture; }

   public static ExecutingTest getExecutingTest() { return getInstance().executingTest; }

   public static RecordAndReplayExecution getRecordAndReplayForRunningTest(boolean create)
   {
      TestRun testRun = getInstance();

      if (testRun.currentTestInstance == null) {
         return null;
      }

      return testRun.executingTest.getRecordAndReplay(testRun.runningTestMethod != null && create);
   }

   public static MockClasses getMockClasses() { return getInstance().mockClasses; }

   public static void verifyExpectationsOnAnnotatedMocks()
   {
      getMockClasses().getMockStates().verifyExpectations();
   }

   // Static "mutators" for global state //////////////////////////////////////////////////////////////////////////////

   public static void resetExpectationsOnAnnotatedMocks()
   {
      getMockClasses().getMockStates().resetExpectations();
   }

   public static void setCurrentTestClass(Class<?> testClass)
   {
      getInstance().currentTestClass = testClass;
   }

   public static void prepareForNextTest()
   {
      getInstance().testId++;
   }

   public static void setRunningTestMethod(Method runningTestMethod)
   {
      TestRun testRun = getInstance();
      testRun.runningTestMethod = runningTestMethod;

      if (runningTestMethod != null) {
         testRun.executingTest.clearRecordAndReplayForVerifications();
      }
   }

   public static void enterNoMockingZone() { noMockingCount.set(1); }
   public static void exitNoMockingZone() { noMockingCount.set(-1); }

   public static void setRunningIndividualTest(Object testInstance)
   {
      getInstance().currentTestInstance = testInstance;
   }

   public static void setSavePointForTestClass(SavePoint savePoint)
   {
      getInstance().savePointForTestClass = savePoint;
   }

   public static void setCaptureOfSubtypes(CaptureOfImplementationsForTestClass captureOfSubtypes)
   {
      getInstance().captureOfSubtypes = captureOfSubtypes;
   }

   public static void setSharedFieldTypeRedefinitions(SharedFieldTypeRedefinitions redefinitions)
   {
      getInstance().sharedFieldTypeRedefinitions = redefinitions;
   }

   public static void finishCurrentTestExecution()
   {
      TestRun testRun = getInstance();
      testRun.runningTestMethod = null;
      testRun.executingTest.finishExecution();
   }

   // Methods to be called only from generated bytecode or from the MockingBridge /////////////////////////////////////

   public static Object getMock(int index)
   {
      return getMockClasses().regularMocks.getMock(index);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static Object getStartupMock(int index)
   {
      return STARTUP_INSTANCE.mockClasses.startupMocks.getMock(index);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static Object getMock(Class<?> mockClass, Object mockedInstance)
   {
      return getMockClasses().regularMocks.getMock(mockClass, mockedInstance);
   }

   public static boolean updateMockState(String mockClassDesc, int mockIndex)
   {
      AnnotatedMockStates mockStates = getMockStates(mockClassDesc);
      return mockStates.updateMockState(mockClassDesc, mockIndex);
   }

   private static AnnotatedMockStates getMockStates(String mockClassDesc)
   {
      AnnotatedMockStates mockStates = getMockClasses().getMockStates();
      return mockStates.hasStates(mockClassDesc) ? mockStates : STARTUP_INSTANCE.mockClasses.getMockStates();
   }

   public static void exitReentrantMock(String mockClassDesc, int mockIndex)
   {
      AnnotatedMockStates mockStates = getMockStates(mockClassDesc);
      mockStates.exitReentrantMock(mockClassDesc, mockIndex);
   }
}
