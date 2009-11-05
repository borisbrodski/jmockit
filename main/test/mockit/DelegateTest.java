/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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

import static org.junit.Assert.*;

import mockit.integration.junit3.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class DelegateTest extends JMockitTestCase
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

   public void testReturnsDelegate()
   {
      Collaborator collaborator = new Collaborator();
      final boolean bExpected = true;
      final int[] iExpected = new int[0];
      final String sExpected = "test";

      new Expectations()
      {
         Collaborator mock;

         {
            mock.getValue(); returns(new Delegate() { int getValue() { return 2; } });

            mock.doSomething(bExpected, iExpected, sExpected);
            returns(new Delegate()
            {
               String doSomething(boolean b, int[] i, String s)
               {
                  assertEquals(bExpected, b);
                  assertArrayEquals(iExpected, i);
                  assertEquals(sExpected, s);
                  return "";
               }
            });
         }
      };

      assertEquals(2, collaborator.getValue());
      assertEquals("", collaborator.doSomething(bExpected, iExpected, sExpected));
   }

   public void testReturnsTwoConsecutiveReturnValuesThroughDelegates()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.getValue();
            returns(new Delegate() { int getValue() { return 1; } });
            returns(new Delegate() { int getValue() { return 2; } });
         }
      };

      Collaborator collaborator = new Collaborator();
      assertEquals(1, collaborator.getValue());
      assertEquals(2, collaborator.getValue());
   }

   public void testReturnsMultipleReturnValuesThroughSingleDelegate(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.getValue();
            returns(new Delegate()
            {
               int i = 1;

               int getValue() { return i++; }
            });
         }
      };

      assertEquals(1, collaborator.getValue());
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue());
   }

   public void testDelegateForConstructor()
   {
      final ConstructorDelegate delegate = new ConstructorDelegate();

      new Expectations()
      {
         Collaborator mock;

         {
            new Collaborator(withAny(0)); returns(delegate);
         }
      };

      new Collaborator(4);

      assertTrue(delegate.capturedArgument > 0);
   }

   static class ConstructorDelegate implements Delegate
   {
      int capturedArgument;

      void $init(int i) { capturedArgument = i; }
   }

   public void testDelegateForStaticMethod()
   {
      new Expectations()
      {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod();
            returns(new Delegate() { boolean staticMethod() { return false; } });
         }
      };

      assertFalse(Collaborator.staticMethod());
   }

   public void testDelegateWithStaticMethods()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            Collaborator.staticMethod(withAny(1));
            returns(new StaticDelegate());

            mock.doSomething(withAny(false), (int[]) withAny(), withAny(""));
            returns(new StaticDelegate());
         }
      };

      assertTrue(Collaborator.staticMethod(34));
      assertEquals("test", new Collaborator().doSomething(false, null, "replay"));
   }

   static final class StaticDelegate implements Delegate
   {
      static boolean staticMethod(int i)
      {
         assertEquals(34, i);
         return true;
      }
      
      static String doSomething(boolean b, int[] i, String s)
      {
         assertFalse(b);
         assertNull(i);
         assertEquals("replay", s);
         return "test";
      }
   }

   public void testDelegateForNativeMethod()
   {
      new Expectations()
      {
         @NonStrict Collaborator mock;

         {
            mock.nativeMethod(withAny(false));
            returns(new Delegate()
            {
               Long nativeMethod(boolean b) { assertTrue(b); return 0L; }
            });
         }
      };

      assertEquals(0L, new Collaborator().nativeMethod(true));
   }

   public void testDelegateForFinalMethod()
   {
      new Expectations()
      {
         @NonStrict Collaborator mock;

         {
            mock.finalMethod();
            returns(new Delegate() { char finalMethod() { return 'M'; } });
         }
      };

      assertEquals('M', new Collaborator().finalMethod());
   }

   public void testDelegateForPrivateMethod(@NonStrict final Collaborator collaborator)
   {
      new Expectations()
      {
         {
            invoke(collaborator, "privateMethod");
            returns(new Delegate() { float privateMethod() { return 0.5F; } });
         }
      };

      assertEquals(0.5F, collaborator.privateMethod(), 0);
   }

   public void testDelegateForMethodWithCompatibleButDistinctParameterType()
   {
      new Expectations()
      {
         @NonStrict Collaborator collaborator;

         {
            collaborator.addElements(this.<Collection<String>> withNotNull());
            returns(new Delegate()
            {
               void addElements(Collection<String> elements) { elements.add("test"); }
            });
         }
      };

      List<String> elements = new ArrayList<String>();
      new Collaborator().addElements(elements);

      assertTrue(elements.contains("test"));
   }

   public void testDelegateReceivingNullArguments()
   {
      new NonStrictExpectations()
      {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, null);
            returns(new Delegate()
            {
               void doSomething(boolean b, int[] i, String s) {}
            });
         }
      };

      assertNull(new Collaborator().doSomething(true, null, null));
   }

   public void testDelegateWithTwoMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.doSomething(true, null, "str");
            returns(new Delegate()
            {
               String someOther() { return ""; }
               void doSomething(boolean b, int[] i, String s) {}
            });
         }
      };

      assertNull(collaborator.doSomething(true, null, "str"));
   }

   public void testDelegateWithSingleMethodOfDifferentName()
   {
      new NonStrictExpectations()
      {
         Collaborator collaborator;

         {
            collaborator.doSomething(true, null, "str");
            returns(new Delegate()
            {
               void onReplay(boolean b, int[] i, String s)
               {
                  assertTrue(b);
                  assertNull(i);
                  assertEquals("str", s);
               }
            });
         }
      };

      assertNull(new Collaborator().doSomething(true, null, "str"));
   }

   public void testDelegateWithTwoInvalidMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.doSomething(true, null, "str");
            returns(new Delegate()
            {
               String someOther() { return ""; }
               void doSomethingElse(boolean b, int[] i, String s) {}
            });
         }
      };

      try {
         assertNull(collaborator.doSomething(true, null, "str"));
         fail();
      }
      catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().startsWith("No compatible method found"));
      }
   }
}
