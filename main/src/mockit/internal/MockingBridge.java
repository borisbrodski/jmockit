/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import mockit.*;
import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockingBridge implements InvocationHandler
{
   public static final int RECORD_OR_REPLAY = 1;
   public static final int CALL_STATIC_MOCK = 3;
   public static final int CALL_INSTANCE_MOCK = 4;
   public static final int UPDATE_MOCK_STATE = 5;
   public static final int EXIT_REENTRANT_MOCK = 6;

   private static final Object[] EMPTY_ARGS = {};

   @SuppressWarnings("UnusedDeclaration")
   public static final MockingBridge MB = new MockingBridge();

   public static void preventEventualClassLoadingConflicts()
   {
      // Pre-load certain JMockit classes to avoid NoClassDefFoundError's when mocking certain JRE classes,
      // such as ArrayList.
      try {
         Class.forName("mockit.Capturing");
         Class.forName("mockit.Delegate");
         Class.forName("mockit.internal.expectations.invocation.InvocationResults");
         Class.forName("mockit.internal.expectations.mocking.BaseTypeRedefinition$MockedClass");
         Class.forName("mockit.internal.expectations.mocking.SharedFieldTypeRedefinitions");
         Class.forName("mockit.internal.expectations.mocking.TestedClasses");
         Class.forName("mockit.internal.expectations.argumentMatching.EqualityMatcher");
      }
      catch (ClassNotFoundException ignore) {}

      wasCalledDuringClassLoading();
      DefaultValues.computeForReturnType("()J");
   }

   public Object invoke(Object mocked, Method method, Object[] args) throws Throwable
   {
      int targetId = (Integer) args[0];
      String mockDesc = (String) args[4];

      if ((targetId == CALL_STATIC_MOCK || targetId == CALL_INSTANCE_MOCK) && wasCalledDuringClassLoading()) {
         return DefaultValues.computeForReturnType(mockDesc);
      }

      String mockClassDesc = (String) args[2];

      if (
         mocked == null && "java/lang/System".equals(mockClassDesc) && wasCalledDuringClassLoading() ||
         mocked != null && instanceOfClassThatParticipatesInClassLoading(mocked) && wasCalledDuringClassLoading()
      ) {
         return targetId == UPDATE_MOCK_STATE ? false : Void.class;
      }

      int mockStateIndex = (Integer) args[7];

      if (targetId == UPDATE_MOCK_STATE) {
         return TestRun.updateMockState(mockClassDesc, mockStateIndex);
      }
      else if (targetId == EXIT_REENTRANT_MOCK) {
         TestRun.exitReentrantMock(mockClassDesc, mockStateIndex);
         return null;
      }

      String mockName = (String) args[3];
      int executionMode = (Integer) args[9];
      Object[] mockArgs = extractMockArguments(args);

      if (targetId != RECORD_OR_REPLAY) {
         int mockIndex = (Integer) args[8];
         boolean startupMock = executionMode > 0;
         return callMock(
            mocked, targetId, mockClassDesc, mockName, mockDesc, mockStateIndex, mockIndex, startupMock, mockArgs);
      }

      if (TestRun.isInsideNoMockingZone()) {
         return Void.class;
      }

      TestRun.enterNoMockingZone();

      try {
         int mockAccess = (Integer) args[1];
         String genericSignature = (String) args[5];
         String exceptions = (String) args[6];

         return
            RecordAndReplayExecution.recordOrReplay(
               mocked, mockAccess, mockClassDesc, mockName + mockDesc, genericSignature, exceptions,
               executionMode, mockArgs);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private static boolean instanceOfClassThatParticipatesInClassLoading(Object mocked)
   {
      Class<?> mockedClass = mocked.getClass();
      return
         mockedClass == File.class || mockedClass == URL.class || mockedClass == FileInputStream.class ||
         mockedClass == JarFile.class || Vector.class.isInstance(mocked) || Hashtable.class.isInstance(mocked);
   }

   private static boolean wasCalledDuringClassLoading()
   {
      StackTrace st = new StackTrace(new Throwable());
      int n = st.getDepth();

      for (int i = 3; i < n; i++) {
         StackTraceElement ste = st.getElement(i);

         if ("ClassLoader.java".equals(ste.getFileName()) && "loadClass".equals(ste.getMethodName())) {
            return true;
         }
      }

      return false;
   }

   private static Object[] extractMockArguments(Object[] args)
   {
      int i = 10;

      if (args.length > i) {
         Object[] mockArgs = new Object[args.length - i];
         System.arraycopy(args, i, mockArgs, 0, mockArgs.length);
         return mockArgs;
      }

      return EMPTY_ARGS;
   }

   private static Object callMock(
      Object mocked, int targetId, String mockClassInternalName, String mockName, String mockDesc,
      int mockStateIndex, int mockInstanceIndex, boolean startupMock, Object[] mockArgs)
   {
      Class<?> mockClass;
      Object mock;

      if (targetId == CALL_STATIC_MOCK) {
         mock = mocked;
         String mockClassName = getMockClassName(mockClassInternalName);
         mockClass = Utilities.loadClass(mockClassName);
      }
      else {
         assert targetId == CALL_INSTANCE_MOCK;

         if (mockInstanceIndex < 0) { // call to instance mock method on mock not yet instantiated
            String mockClassName = getMockClassName(mockClassInternalName);
            mock = Utilities.newInstance(mockClassName);
         }
         else if (startupMock) {
            mock = TestRun.getStartupMock(mockInstanceIndex);
         }
         else { // call to instance mock method on mock already instantiated
            mock = TestRun.getMock(mockInstanceIndex);
         }

         mockClass = mock.getClass();
         setItFieldIfAny(mockClass, mock, mocked);
      }

      Class<?>[] paramClasses = Utilities.getParameterTypes(mockDesc);

      if (paramClasses.length > 0 && paramClasses[0] == Invocation.class) {
         Invocation invocation = TestRun.createMockInvocation(mockClassInternalName, mockStateIndex, mocked);
         //noinspection AssignmentToMethodParameter
         mockArgs = Utilities.argumentsWithExtraFirstValue(mockArgs, invocation);
      }

      Object result = Utilities.invoke(mockClass, mock, mockName, paramClasses, mockArgs);
      return result;
   }

   private static String getMockClassName(String mockClassInternalName)
   {
      return mockClassInternalName.replace('/', '.');
   }

   private static void setItFieldIfAny(Class<?> mockClass, Object mock, Object mocked)
   {
      try {
         Field itField = mockClass.getDeclaredField("it");
         Utilities.setFieldValue(itField, mock, mocked);
      }
      catch (NoSuchFieldException ignore) {}
   }
}
