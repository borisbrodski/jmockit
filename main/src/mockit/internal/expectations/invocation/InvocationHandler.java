/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import mockit.internal.expectations.*;
import mockit.internal.util.*;

public final class InvocationHandler extends DynamicInvocationResult
{
   public InvocationHandler(Object handler)
   {
      super(handler, Utilities.findNonPrivateHandlerMethod(handler));
   }

   public void evaluateInvocation(Expectation expectation, Object[] args)
   {
      invokeMethodOnTargetObject(expectation.invocation.instance, expectation.constraints, args);
   }

   @Override
   Object produceResult(
      Object invokedObject, ExpectedInvocation invocation, InvocationConstraints constraints, Object[] args)
      throws Throwable
   {
      return invokeMethodOnTargetObject(invokedObject, constraints, args);
   }
}