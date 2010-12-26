/*
 * JMockit Annotations
 * Copyright (c) 2010 Rogério Liesenfeld
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