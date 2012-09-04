/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import mockit.internal.util.*;

public final class InvocationHandler extends DynamicInvocationResult
{
   public InvocationHandler(Object handler)
   {
      super(handler, Utilities.findNonPrivateHandlerMethod(handler));
   }

   @Override
   public Object produceResult(
      Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
   {
      if (numberOfRegularParameters == 0 && args.length > 0) {
         //noinspection AssignmentToMethodParameter
         args = Utilities.NO_ARGS;
      }

      Object result = invokeMethodOnTargetObject(invokedObject, invocation, constraints, args);

      if (Boolean.FALSE.equals(result)) {
         String message = '"' + methodToInvoke.getName() + "\" failed on invocation to" + invocation.toString(args);
         throw new AssertionError(message);
      }

      return result;
   }
}