/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.*;

import mockit.Invocation;

final class DelegateInvocation extends Invocation
{
   private final InvocationArguments invocationArguments;

   DelegateInvocation(
      Object invokedInstance, Object[] invokedArguments,
      ExpectedInvocation expectedInvocation, InvocationConstraints constraints)
   {
      super(
         invokedInstance, invokedArguments,
         constraints.invocationCount, constraints.minInvocations, constraints.maxInvocations);
      invocationArguments = expectedInvocation.arguments;
   }

   @Override
   protected Method getRealMethod() { return invocationArguments.getRealMethod().method; }
}
