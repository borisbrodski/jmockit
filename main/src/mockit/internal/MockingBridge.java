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

   private int targetId;
   private String mockClassInternalName;
   private String mockName;
   private String mockDesc;
   private int mockIndex;
   private Object[] mockArgs;

   public Object invoke(Object mocked, Method method, Object[] args) throws Throwable
   {
      mockClassInternalName = (String) args[2];

      if (isCallThatParticipatesInClassLoading(mocked)) {
         return Void.class;
      }

      targetId = (Integer) args[0];
      mockIndex = targetId < FIRST_TARGET_WITH_EXTRA_ARG ? -1 : (Integer) args[5];

      if (targetId == UPDATE_MOCK_STATE) {
         return TestRun.updateMockState(mockClassInternalName, mockIndex);
      }
      else if (targetId == EXIT_REENTRANT_MOCK) {
         TestRun.exitReentrantMock(mockClassInternalName, mockIndex);
         return null;
      }

      extractMockMethodAndArguments(args);

      if (targetId != RECORD_OR_REPLAY) {
         return callMock(mocked);
      }

      if (TestRun.isInsideNoMockingZone()) {
         return Void.class;
      }

      TestRun.enterNoMockingZone();

      try {
         int mockAccess = (Integer) args[1];
         boolean withRealImpl = targetId == RECORD_OR_REPLAY && (Integer) args[5] == 1;

         return
            RecordAndReplayExecution.recordOrReplay(
               mocked, mockAccess, mockClassInternalName, mockName + mockDesc, withRealImpl,
               mockArgs);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private boolean isCallThatParticipatesInClassLoading(Object mocked)
   {
      if (mocked != null) {
         Class<?> mockedClass = mocked.getClass();

         if (
            mockedClass == File.class || mockedClass == URL.class ||
            Vector.class.isInstance(mocked) || Hashtable.class.isInstance(mocked)
         ) {
            StackTraceElement[] st = new Throwable().getStackTrace();

            for (int i = 3; i < st.length; i++) {
               StackTraceElement ste = st[i];

               if (
                  "ClassLoader.java".equals(ste.getFileName()) &&
                  "loadClass".equals(ste.getMethodName())
               ) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void extractMockMethodAndArguments(Object[] args)
   {
      mockName = (String) args[3];
      mockDesc = (String) args[4];

      int i = targetId > RECORD_OR_REPLAY && targetId < FIRST_TARGET_WITH_EXTRA_ARG ? 5 : 6;

      if (args.length > i) {
         mockArgs = new Object[args.length - i];
         System.arraycopy(args, i, mockArgs, 0, mockArgs.length);
      }
      else {
         mockArgs = EMPTY_ARGS;
      }
   }

   private Object callMock(Object mocked)
   {
      if (targetId == CALL_CONSTRUCTOR_MOCK) {
         String mockClassName = getMockClassName();
         Class<?>[] paramClasses = Utilities.getParameterTypes(mockDesc);
         Utilities.newInstance(mockClassName, paramClasses, mockArgs);
         return null;
      }

      Class<?> mockClass;
      Object mock;

      if (targetId == CALL_STATIC_MOCK) {
         mock = mocked;
         String mockClassName = getMockClassName();
         mockClass = Utilities.loadClass(mockClassName);
      }
      else {
         assert targetId == CALL_INSTANCE_MOCK;

         if (mockIndex < 0) { // call to instance mock method on mock not yet instantiated
            String mockClassName = getMockClassName();
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

   private String getMockClassName()
   {
      return mockClassInternalName.replace('/', '.');
   }

   private void setItFieldIfAny(Class<?> mockClass, Object mock, Object mocked)
   {
      try {
         Field itField = mockClass.getDeclaredField("it");
         Utilities.setFieldValue(itField, mock, mocked);
      }
      catch (NoSuchFieldException ignore) {}
   }
}
