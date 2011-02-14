/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import mockit.*;

final class MockInvocation extends Invocation
{
   MockInvocation(Object invokedInstance, int invocationCount, int minInvocations, int maxInvocations)
   {
      super(invokedInstance, invocationCount, minInvocations, maxInvocations);
   }
}
