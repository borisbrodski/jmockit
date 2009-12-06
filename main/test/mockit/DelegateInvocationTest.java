/*
 * JMockit Expectations
 * Copyright (c) 2009 JMockit Developers
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

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class DelegateInvocationTest
{
   static class Collaborator
   {
      Collaborator() {}
      Collaborator(int i) {}

      int getValue() { return -1; }
      String doSomething(boolean b, int[] i, String s) { return s + b + i[0]; }
      static boolean staticMethod() { return true; }
      static boolean staticMethod(int i) { return i > 0; }
      native long nativeMethod(boolean b);
      final char finalMethod() { return 's'; }
      private float privateMethod() { return 1.2F; }
      void addElements(Collection<String> elements) { elements.add("one element"); }
   }

   @Test
   public void delegateWithContextObject()
   {
      new NonStrictExpectations()
      {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod();
            returns(new Delegate()
            {
               boolean staticMethod(Invocation context)
               {
                  assertEquals(context.getInvocationCount() - 1, context.getInvocationIndex());
                  return context.getInvocationCount() > 0;
               }
            });
         }
      };

      assertTrue(Collaborator.staticMethod());
      assertTrue(Collaborator.staticMethod());
   }

   static class ConstructorDelegate implements Delegate
   {
      int capturedArgument;

      void $init(Invocation context, int i)
      {
         capturedArgument = i + context.getInvocationCount();
      }
   }

   @Test
   public void delegateForConstructorWithContext()
   {
      final ConstructorDelegate delegate = new ConstructorDelegate();

      new Expectations()
      {
         Collaborator mock;

         {
            new Collaborator(anyInt); returns(delegate);
         }
      };

      new Collaborator(4);

      assertEquals(5, delegate.capturedArgument);
   }

   @Test
   public void delegateReceivingNullArguments()
   {
      new NonStrictExpectations()
      {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, null);
            returns(new Delegate()
            {
               void doSomething(Invocation invocation, Boolean b, int[] i, String s)
               {
                  assertEquals(1, invocation.getInvocationCount());
                  assertTrue(b);
                  assertNull(i);
                  assertNull(s);
               }
            });
         }
      };

      assertNull(new Collaborator().doSomething(true, null, null));
   }

   @Test
   public void delegateWithAnotherMethodOnTheDelegateClass()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.getValue();
            returns(new Delegate()
            {
               int getValue(Invocation context)
               {
                  return context.getInvocationCount();
               }

               void otherMethod(Invocation context)
               {
                  fail();
               }
            });
         }
      };

      assertEquals(1, new Collaborator().getValue());
      assertEquals(2, new Collaborator().getValue());
   }

   @Test
   public void delegateClassWithMultipleMethodsAndInexactButValidMatch()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            Collaborator.staticMethod(1);
            returns(new Delegate()
            {
               void otherMethod(int i)
               {
                  fail();
               }

               boolean staticMethod(Invocation invocation, Number i)
               {
                  return i.intValue() > 0;
               }
            });
         }
      };

      assertTrue(Collaborator.staticMethod(1));
   }

   @Test
   public void delegateOverridingInvocationCountConstraints(final Collaborator mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.finalMethod(); repeatsAtMost(1);
            returns(new Delegate()
            {
               char finalMethod(Invocation invocation)
               {
                  invocation.setMinInvocations(2);
                  invocation.setMaxInvocations(2);
                  return 'a';
               }
            });
         }
      };

      assertEquals('a', mock.finalMethod());
      assertEquals('a', mock.finalMethod());
   }

   @Test(expected = IllegalArgumentException.class)
   public void delegateClassWithNoMethodMatchingTheExpectationSignature(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.privateMethod();
            returns(new Delegate()
            {
               float someDelegate(Invocation invocation) { return 1.0F; }
               void someOtherMethod() {}
            });
         }
      };

      assertEquals(1.0, mock.privateMethod(), 0.0);
   }

   @Test
   public void delegateWithDifferentMethodName()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.nativeMethod(anyBoolean);
            returns(new Delegate()
            {
               long differentName(Invocation invocation, boolean b)
               {
                  assertEquals(1, invocation.getInvocationCount());
                  assertTrue(b);
                  return 3L;
               }
            });
         }
      };

      assertEquals(3L, new Collaborator().nativeMethod(true));
   }

   @Test
   public void delegatesForTwoSeparateExpectations(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.addElements((Collection<String>) any);
            returns(new Delegate()
            {
               void delegate1(Invocation invocation, Collection<String> elements)
               {
                  assertEquals(1, invocation.getInvocationCount());
                  assertNotNull(elements);
               }
            });

            mock.addElements(null);
            returns(new Delegate()
            {
               void delegate2(Invocation invocation, Collection<String> elements)
               {
                  assertEquals(1, invocation.getInvocationCount());
                  assertNull(elements);
               }
            });
         }
      };

      mock.addElements(Collections.<String>emptyList());
      mock.addElements(null);
   }

   @Test
   public void consecutiveDelegatesForTheSameExpectation(final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.getValue();
            returns(
               new Delegate()
               {
                  int delegate(Invocation invocation)
                  {
                     return invocation.getInvocationCount();
                  }
               },
               new Delegate()
               {
                  int delegate(Invocation invocation)
                  {
                     return invocation.getInvocationCount();
                  }
               },
               new Delegate()
               {
                  int delegate(Invocation invocation)
                  {
                     assertEquals(3, invocation.getInvocationCount());
                     throw new SecurityException();
                  }
               });
         }
      };

      assertEquals(1, mock.getValue());
      assertEquals(2, mock.getValue());

      try {
         mock.getValue();
         fail();
      }
      catch (SecurityException e) {
         // OK
      }
   }
}
