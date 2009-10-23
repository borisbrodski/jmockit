/*
 * JMockit Annotations
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
package mockit.internal.annotations;

import java.lang.reflect.*;

import mockit.internal.util.*;

final class MockState
{
   private final Class<?> realClass;
   final String mockNameAndDesc;

   // Expectations on the number of invocations of the mock as specified by the @Mock annotation,
   // initialized with the default values as specified in @Mock annotation definition:
   int expectedInvocations = -1;
   int minExpectedInvocations;
   int maxExpectedInvocations = -1;

   // Current mock invocation state:
   private int invocationCount;
   private ThreadLocal<Boolean> onReentrantCall;

   // Helper field just for synchronization:
   private final Object invocationCountLock = new Object();

   MockState(Class<?> realClass, String mockNameAndDesc)
   {
      this.realClass = realClass;
      this.mockNameAndDesc = mockNameAndDesc;
   }

   Class<?> getRealClass()
   {
      return realClass;
   }

   boolean isReentrant()
   {
      return onReentrantCall != null;
   }

   void makeReentrant()
   {
      onReentrantCall = new ThreadLocal<Boolean>()
      {
         @Override
         protected Boolean initialValue() { return false; }
      };
   }

   boolean isWithExpectations()
   {
      return expectedInvocations >= 0 || minExpectedInvocations > 0 || maxExpectedInvocations >= 0;
   }

   void update()
   {
      synchronized (invocationCountLock) {
         invocationCount++;
      }

      if (onReentrantCall != null) {
         onReentrantCall.set(true);
      }
   }

   boolean isOnReentrantCall()
   {
      return onReentrantCall != null && onReentrantCall.get();
   }

   void exitReentrantCall()
   {
      onReentrantCall.set(false);
   }

   void verifyExpectations()
   {
      int timesInvoked;

      synchronized (invocationCountLock) {
         timesInvoked = invocationCount;
      }

      if (expectedInvocations >= 0 && timesInvoked != expectedInvocations) {
         throw new AssertionError(errorMessage("exactly", expectedInvocations, timesInvoked));
      }
      else if (timesInvoked < minExpectedInvocations) {
         throw new AssertionError(errorMessage("at least", minExpectedInvocations, timesInvoked));
      }
      else if (maxExpectedInvocations >= 0 && timesInvoked > maxExpectedInvocations) {
         throw new AssertionError(errorMessage("at most", maxExpectedInvocations, timesInvoked));
      }
   }

   private String errorMessage(String quantifier, int numExpectedInvocations, int timesInvoked)
   {
      String realClassName = getRealClassName();

      return
         "Expected " + quantifier + ' ' + numExpectedInvocations + " invocation(s) of " +
         new MethodFormatter(realClassName, mockNameAndDesc) +
         ", but was invoked " + timesInvoked + " time(s)";
   }

   private String getRealClassName()
   {
      if (realClass == null) {
         return null;
      }

      if (Proxy.isProxyClass(realClass)) {
         Class<?>[] interfaces = realClass.getInterfaces();

         if (interfaces.length <= 2) {
            return interfaces[0].getName();
         }
      }

      return realClass.getName();
   }
}
