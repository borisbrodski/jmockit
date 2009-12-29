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

import mockit.*;
import mockit.internal.expectations.*;
import mockit.internal.util.*;

public final class InvocationHandler extends InvocationResult
{
   private final Object handler;
   private final Method handlerMethod;
   private boolean hasInvocationParameter;
   private Object[] args;

   public InvocationHandler(Object handler)
   {
      this.handler = handler;
      handlerMethod = findNonPrivateHandlerMethod();
      determineWhetherMethodHasInvocationParameter();
   }

   private Method findNonPrivateHandlerMethod()
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

   private void determineWhetherMethodHasInvocationParameter()
   {
      Class<?>[] parameters = handlerMethod.getParameterTypes();
      hasInvocationParameter = parameters.length > 0 && parameters[0] == Invocation.class;
   }

   public void evaluateInvocation(Expectation expectation)
   {
      args = expectation.invocation.getArgumentValues();

      if (hasInvocationParameter) {
         executeDelegateWithInvocationContext(expectation.constraints);
      }
      else {
         Utilities.invoke(handler, handlerMethod, args);
      }
   }

   @Override
   Object produceResult(
      ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      throws Throwable
   {
      this.args = args;

      Object result;

      if (hasInvocationParameter) {
         result = executeDelegateWithInvocationContext(constraints);
      }
      else {
         result = Utilities.invoke(handler, handlerMethod, args);
      }

      return result;
   }

   private Object executeDelegateWithInvocationContext(InvocationConstraints constraints)
   {
      Invocation invocation =
         new DelegateInvocation(
            constraints.invocationCount, constraints.minInvocations, constraints.maxInvocations);
      Object[] delegateArgs = getDelegateArgumentsWithExtraInvocationObject(invocation);

      try {
         return Utilities.invoke(handler, handlerMethod, delegateArgs);
      }
      finally {
         constraints.setLimits(invocation.getMinInvocations(), invocation.getMaxInvocations());
      }
   }

   private Object[] getDelegateArgumentsWithExtraInvocationObject(Invocation invocation)
   {
      Object[] delegateArgs = new Object[args.length + 1];
      delegateArgs[0] = invocation;
      System.arraycopy(args, 0, delegateArgs, 1, args.length);
      return delegateArgs;
   }
}