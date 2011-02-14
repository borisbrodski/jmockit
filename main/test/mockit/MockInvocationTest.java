/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static org.junit.Assert.*;

public final class MockInvocationTest
{
   static class Collaborator
   {
      int value;
      
      Collaborator() {}
      Collaborator(int i) { value = i; }

      int getValue() { return -1; }
      String doSomething(boolean b, int[] i, String s) { return s + b + i[0]; }
      static boolean staticMethod() { return true; }
   }

   @Ignore @Test
   public void mockMethodWithContextObject()
   {
      new MockUp<Collaborator>()
      {
         @Mock
         boolean staticMethod(Invocation context)
         {
            assertTrue(context.getInvokedInstance() instanceof Collaborator);
            assertEquals(context.getInvocationCount() - 1, context.getInvocationIndex());
            assertEquals(0, context.getMinInvocations());
            assertEquals(-1, context.getMaxInvocations());
            return context.getInvocationCount() <= 0;
         }
      };

      assertFalse(Collaborator.staticMethod());
      assertFalse(Collaborator.staticMethod());
   }

   @MockClass(realClass = Collaborator.class)
   static class MockForConstructor
   {
      int capturedArgument;

      @Mock(invocations = 1)
      void $init(Invocation context, int i)
      {
         assertTrue(context.getInvokedInstance() instanceof Collaborator);
         assertEquals(1, context.getMinInvocations());
         assertEquals(1, context.getMaxInvocations());
         capturedArgument = i + context.getInvocationCount();
      }
   }

   @Ignore @Test
   public void mockForConstructorWithContext()
   {
      MockForConstructor mock = new MockForConstructor();
      Mockit.setUpMocks(MockForConstructor.class);

      new Collaborator(4);

      assertEquals(5, mock.capturedArgument);
   }
}