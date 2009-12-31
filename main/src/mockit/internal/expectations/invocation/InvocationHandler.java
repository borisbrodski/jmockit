/*
 * JMockit Expectations & Verifications
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
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;

import mockit.internal.expectations.*;

public final class InvocationHandler extends DynamicInvocationResult
{
   public InvocationHandler(Object handler)
   {
      super(handler, findNonPrivateHandlerMethod(handler));
   }

   private static Method findNonPrivateHandlerMethod(Object handler)
   {
      Method[] declaredMethods = handler.getClass().getDeclaredMethods();
      Method nonPrivateMethod = null;

      for (Method declaredMethod : declaredMethods) {
         if (!Modifier.isPrivate(declaredMethod.getModifiers())) {
            if (nonPrivateMethod != null) {
               throw new IllegalArgumentException(
                  "More than one non-private invocation handler method found");
            }

            nonPrivateMethod = declaredMethod;
         }
      }

      if (nonPrivateMethod == null) {
         throw new IllegalArgumentException("No non-private invocation handler method found");
      }

      return nonPrivateMethod;
   }

   public void evaluateInvocation(Expectation expectation)
   {
      ExpectedInvocation invocation = expectation.invocation;
      Object[] args = invocation.getArgumentValues();
      invokeMethodOnTargetObject(invocation.instance, expectation.constraints, args);
   }

   @Override
   Object produceResult(
      Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints,
      Object[] args)
      throws Throwable
   {
      return invokeMethodOnTargetObject(invokedObject, constraints, args);
   }
}