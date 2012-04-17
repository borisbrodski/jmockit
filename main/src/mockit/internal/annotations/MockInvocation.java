/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;

import mockit.*;
import mockit.internal.util.*;

/**
 * An invocation to a {@code @Mock} method.
 */
public final class MockInvocation extends Invocation
{
   private final MockState mockState;

   public MockInvocation(Object invokedInstance, Object[] invokedArguments, MockState mockState)
   {
      super(
         invokedInstance, invokedArguments,
         mockState.getTimesInvoked(), mockState.getMinInvocations(), mockState.getMaxInvocations());
      this.mockState = mockState;
   }

   @Override
   protected void onChange()
   {
      mockState.minExpectedInvocations = getMinInvocations();
      mockState.maxExpectedInvocations = getMaxInvocations();
   }

   @Override
   protected Method getRealMethod()
   {
      RealMethod realMethod = mockState.getRealMethod();
      return realMethod == null ? null : realMethod.method;
   }
}
