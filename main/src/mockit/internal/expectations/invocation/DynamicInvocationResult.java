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
import mockit.internal.util.*;

abstract class DynamicInvocationResult extends InvocationResult
{
   final Object targetObject;
   Method methodToInvoke;
   private boolean hasInvocationParameter;

   DynamicInvocationResult(Object targetObject, Method methodToInvoke)
   {
      this.targetObject = targetObject;
      this.methodToInvoke = methodToInvoke;

      if (methodToInvoke != null) {
         determineWhetherMethodToInvokeHasInvocationParameter();
      }
   }

   final void determineWhetherMethodToInvokeHasInvocationParameter()
   {
      Class<?>[] parameters = methodToInvoke.getParameterTypes();
      hasInvocationParameter = parameters.length > 0 && parameters[0] == Invocation.class;
   }

   private Object invokeMethodWithContext(InvocationConstraints constraints, Object[] args)
   {
      Invocation invocation =
         new DelegateInvocation(
            constraints.invocationCount, constraints.minInvocations, constraints.maxInvocations);
      Object[] delegateArgs = getArgumentsWithExtraInvocationObject(invocation, args);

      try {
         return Utilities.invoke(targetObject, methodToInvoke, delegateArgs);
      }
      finally {
         constraints.setLimits(invocation.getMinInvocations(), invocation.getMaxInvocations());
      }
   }

   private Object[] getArgumentsWithExtraInvocationObject(Invocation invocation, Object[] args)
   {
      Object[] delegateArgs = new Object[args.length + 1];
      delegateArgs[0] = invocation;
      System.arraycopy(args, 0, delegateArgs, 1, args.length);
      return delegateArgs;
   }

   final Object invokeMethodOnTargetObject(InvocationConstraints constraints, Object[] args)
   {
      Object result;

      if (hasInvocationParameter) {
         result = invokeMethodWithContext(constraints, args);
      }
      else {
         result = Utilities.invoke(targetObject, methodToInvoke, args);
      }

      return result;
   }
}