/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import org.junit.*;

import mockit.*;
import mockit.internal.annotations.MockStateBetweenTestMethodsJUnit45Test.TheMockClass;
import mockit.internal.state.*;

@UsingMocksAndStubs(TheMockClass.class)
public final class MockStateBetweenTestMethodsJUnit45Test
{
   static final class RealClass
   {
      static void doSomething() {}
   }

   @MockClass(realClass = RealClass.class)
   static final class TheMockClass
   {
      static final String internalName = TheMockClass.class.getName().replace('.', '/');

      @Mock(invocations = 1)
      void doSomething() {}

      static void assertMockState(int expectedInvocationCount)
      {
         MockState mockState =
            TestRun.getMockClasses().getMockStates().getMockState(internalName, 0);

         assert mockState.isWithExpectations();
         assert !mockState.isReentrant();
         assert !mockState.isOnReentrantCall();
         assert mockState.getTimesInvoked() == expectedInvocationCount;
      }
   }

   @Test
   public void firstTest()
   {
      TheMockClass.assertMockState(0);
      RealClass.doSomething();
      TheMockClass.assertMockState(1);
   }

   @Test
   public void secondTest()
   {
      TheMockClass.assertMockState(0);
      RealClass.doSomething();
      TheMockClass.assertMockState(1);
   }
}