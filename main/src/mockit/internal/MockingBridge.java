/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockingBridge implements InvocationHandler
{
   public static final int RECORD_OR_REPLAY = 1;
   public static final int CALL_CONSTRUCTOR_MOCK = 2;
   public static final int CALL_STATIC_MOCK = 3;
   public static final int CALL_INSTANCE_MOCK = 4;
   public static final int UPDATE_MOCK_STATE = 5;
   public static final int EXIT_REENTRANT_MOCK = 6;
   public static final int FIRST_TARGET_WITH_EXTRA_ARG = CALL_INSTANCE_MOCK;

   private static final Object[] EMPTY_ARGS = {};

   @SuppressWarnings({"UnusedDeclaration"})
   public static final MockingBridge MB = new MockingBridge();

   public synchronized Object invoke(Object mocked, Method method, Object[] args) throws Throwable
   {
      if (isCallThatParticipatesInClassLoading(mocked)) {
         return Void.class;
      }

      int targetId = (Integer) args[0];
      int mockIndex = targetId < FIRST_TARGET_WITH_EXTRA_ARG ? -1 : (Integer) args[5];
      String mockClassInternalName = (String) args[2];

      if (targetId == UPDATE_MOCK_STATE) {
         return TestRun.updateMockState(mockClassInternalName, mockIndex);
      }
      else if (targetId == EXIT_REENTRANT_MOCK) {
         TestRun.exitReentrantMock(mockClassInternalName, mockIndex);
         return null;
      }

      String mockName = (String) args[3];
      String mockDesc = (String) args[4];
      Object[] mockArgs = extractMockArguments(targetId, args);

      if (targetId != RECORD_OR_REPLAY) {
         return callMock(mocked, targetId, mockClassInternalName, mockName, mockDesc, mockIndex, mockArgs);
      }

      if (TestRun.isInsideNoMockingZone()) {
         return Void.class;
      }

      TestRun.enterNoMockingZone();

      try {
         int mockAccess = (Integer) args[1];
         int executionMode = (Integer) args[5];

         return
            RecordAndReplayExecution.recordOrReplay(
               mocked, mockAccess, mockClassInternalName, mockName + mockDesc, executionMode, mockArgs);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private static boolean isCallThatParticipatesInClassLoading(Object mocked)
   {
      if (mocked != null) {
         Class<?> mockedClass = mocked.getClass();

         if (
            mockedClass == File.class || mockedClass == URL.class || mockedClass == FileInputStream.class ||
            Vector.class.isInstance(mocked) || Hashtable.class.isInstance(mocked)
         ) {
            StackTraceElement[] st = new Throwable().getStackTrace();

            for (int i = 3; i < st.length; i++) {
               StackTraceElement ste = st[i];

               if ("ClassLoader.java".equals(ste.getFileName()) && "loadClass".equals(ste.getMethodName())) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static Object[] extractMockArguments(int targetId, Object[] args)
   {
      int i = targetId > RECORD_OR_REPLAY && targetId < FIRST_TARGET_WITH_EXTRA_ARG ? 5 : 6;

      if (args.length > i) {
         Object[] mockArgs = new Object[args.length - i];
         System.arraycopy(args, i, mockArgs, 0, mockArgs.length);
         return mockArgs;
      }

      return EMPTY_ARGS;
   }

   private static Object callMock(
      Object mocked, int targetId,
      String mockClassInternalName, String mockName, String mockDesc, int mockIndex, Object[] mockArgs)
   {
      if (targetId == CALL_CONSTRUCTOR_MOCK) {
         String mockClassName = getMockClassName(mockClassInternalName);
         Class<?>[] paramClasses = Utilities.getParameterTypes(mockDesc);
         Utilities.newInstance(mockClassName, paramClasses, mockArgs);
         return null;
      }

      Class<?> mockClass;
      Object mock;

      if (targetId == CALL_STATIC_MOCK) {
         mock = mocked;
         String mockClassName = getMockClassName(mockClassInternalName);
         mockClass = Utilities.loadClass(mockClassName);
      }
      else {
         assert targetId == CALL_INSTANCE_MOCK;

         if (mockIndex < 0) { // call to instance mock method on mock not yet instantiated
            String mockClassName = getMockClassName(mockClassInternalName);
            mock = Utilities.newInstance(mockClassName);
         }
         else { // call to instance mock method on mock already instantiated
            mock = TestRun.getMock(mockIndex);
         }

         mockClass = mock.getClass();
         setItFieldIfAny(mockClass, mock, mocked);
      }

      Class<?>[] paramClasses = Utilities.getParameterTypes(mockDesc);
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
