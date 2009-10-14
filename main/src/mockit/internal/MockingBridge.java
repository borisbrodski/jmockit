/*
 * JMockit Core
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
package mockit.internal;

import java.lang.reflect.*;

import mockit.internal.expectations.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockingBridge implements InvocationHandler
{
   public interface Target
   {
      int RECORD_OR_REPLAY = 1;
      int CALL_CONSTRUCTOR_MOCK = 2;
      int CALL_STATIC_MOCK = 3;
      int CALL_INSTANCE_MOCK = 4;
      int UPDATE_MOCK_STATE = 5;
      int EXIT_REENTRANT_MOCK = 6;

      int FIRST_TARGET_WITH_EXTRA_ARG = CALL_INSTANCE_MOCK;
   }

   private Integer targetId;
   private Integer mockAccess;
   private String mockClassInternalName;
   private String mockName;
   private String mockDesc;
   private int mockIndex;
   private Object[] mockArgs;

   public Object invoke(Object mocked, Method method, Object[] args) throws Throwable
   {
      extractIndividualDataItems(args);

      if (targetId == Target.RECORD_OR_REPLAY) {
         return
            RecordAndReplayExecution.recordOrReplay(
               mocked, mockAccess, mockClassInternalName, mockName + mockDesc, mockArgs);
      }

      if (targetId == Target.UPDATE_MOCK_STATE) {
         return TestRun.updateMockState(mockClassInternalName, mockIndex);
      }

      if (targetId == Target.EXIT_REENTRANT_MOCK) {
        TestRun.exitReentrantMock(mockClassInternalName, mockIndex);
        return null;
      }

      return callMock(mocked);
   }

   private void extractIndividualDataItems(Object[] args)
   {
      int i = 0;
      targetId = (Integer) args[i++];
      mockAccess = (Integer) args[i++];
      mockClassInternalName = (String) args[i++];
      mockName = (String) args[i++];
      mockDesc = (String) args[i++];
      mockIndex = targetId < Target.FIRST_TARGET_WITH_EXTRA_ARG ? -1 : (Integer) args[i++];

      mockArgs = new Object[args.length - i];
      System.arraycopy(args, i, mockArgs, 0, mockArgs.length);
   }

   private Object callMock(Object mocked)
   {
      if (targetId == Target.CALL_CONSTRUCTOR_MOCK) {
         String mockClassName = getMockClassName();
         Class<?>[] paramClasses = Utilities.getParameterTypes(mockDesc);
         Utilities.newInstance(mockClassName, paramClasses, mockArgs);
         return null;
      }

      Class<?> mockClass;
      Object mock;

      if (targetId == Target.CALL_STATIC_MOCK) {
         mock = mocked;
         String mockClassName = getMockClassName();
         mockClass = Utilities.loadClass(mockClassName);
      }
      else {
         assert targetId == Target.CALL_INSTANCE_MOCK;

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
