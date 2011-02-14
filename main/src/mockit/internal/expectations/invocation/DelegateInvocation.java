/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import mockit.Invocation;

final class DelegateInvocation extends Invocation
{
   DelegateInvocation(Object invokedInstance, int invocationCount, int minInvocations, int maxInvocations)
   {
      super(invokedInstance, invocationCount, minInvocations, maxInvocations);
   }
}
