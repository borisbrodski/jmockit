/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import mockit.*;

public final class MockInvocation extends Invocation
{
   private final MockState mockState;

   public MockInvocation(Object invokedInstance, MockState mockState)
   {
      super(invokedInstance, mockState.getTimesInvoked(), mockState.getMinInvocations(), mockState.getMaxInvocations());
      this.mockState = mockState;
   }

   @Override
   protected void onChange()
   {
      mockState.minExpectedInvocations = getMinInvocations();
      mockState.maxExpectedInvocations = getMaxInvocations();
   }
}
