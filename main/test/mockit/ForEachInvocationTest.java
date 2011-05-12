/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import org.junit.*;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class ForEachInvocationTest
{
   static class Collaborator
   {
      Collaborator() {}

      Collaborator(int i) {}

      int getValue() { return -1; }
      void doSomething() {}
      void doSomething(int i) {}
      String doSomething(boolean b, int[] i, String s) { return s + b + i[0]; }
      static boolean staticMethod() { return true; }
      static boolean staticMethod(int i) { return i > 0; }
      native long nativeMethod(boolean b);
      final char finalMethod() { return 's'; }
      private void privateMethod(short s) {}
      void addElements(Collection<String> elements) { elements.add("one element"); }
   }

   @Test
   public void recordExpectationsWithHandlersForEachInvocation()
   {
      Collaborator collaborator = new Collaborator();
      final boolean bExpected = true;
      final int[] iExpected = new int[0];
      final String sExpected = "test";

      new Expectations()
      {
         Collaborator mock;

         {
            mock.getValue(); forEachInvocation = new Object() { int getValue() { return 2; } };

            mock.doSomething(bExpected, iExpected, sExpected);
            forEachInvocation = new Object()
            {
               String invoked(Boolean b, int[] i, String s)
               {
                  assertEquals(bExpected, b);
                  assertArrayEquals(iExpected, i);
                  assertEquals(sExpected, s);
                  return "";
               }
            };
         }
      };

      assertEquals(2, collaborator.getValue());
      assertEquals("", collaborator.doSomething(bExpected, iExpected, sExpected));
   }

   @Test
   public void verifyExpectationsWithHandlersForEachInvocation(final Collaborator mock)
   {
      Collaborator collaborator = new Collaborator();
      collaborator.addElements(asList("a", "B", "c"));
      collaborator.addElements(asList("B", "123"));

      collaborator.doSomething(true, new int[0], "test");

      new Verifications()
      {
         {
            //noinspection unchecked
            mock.addElements((Collection<String>) any);
            forEachInvocation = new Object()
            {
               void verify(Collection<String> elements) { assert elements.contains("B"); }
            };

            mock.doSomething(anyBoolean, null, null);
            forEachInvocation = new Object()
            {
               void invoked(Boolean b, int[] i, String s)
               {
                  assertTrue(b);
                  assertArrayEquals(new int[0], i);
                  assertEquals("test", s);
               }
            };
         }
      };
   }

   @Test
   public void returnsMultipleReturnValuesThroughSingleHandler(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.getValue();
            forEachInvocation = new Object()
            {
               int i = 1;

               int getValue() { return i++; }
            };
         }
      };

      assertEquals(1, collaborator.getValue());
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue());
   }

   @Test
   public void recordExpectationWithHandlerForEachInvocationOfConstructor()
   {
      final ConstructorHandler handler = new ConstructorHandler();

      new Expectations()
      {
         Collaborator mock;

         {
            new Collaborator(anyInt); forEachInvocation = handler;
         }
      };

      new Collaborator(4);

      assertTrue(handler.capturedArgument > 0);
   }

   static class ConstructorHandler
   {
      int capturedArgument;

      void init(int i) { capturedArgument = i; }
   }

   @Test
   public void verifyExpectationWithHandlerForEachInvocationOfConstructor(Collaborator mock)
   {
      final Collaborator[] collaborators =
         {new Collaborator(5), new Collaborator(4), new Collaborator(1024)};

      new FullVerifications()
      {
         {
            new Collaborator(anyInt);
            forEachInvocation = new Object()
            {
               void checkIt(Invocation invocation, int i)
               {
                  assert i > 0;
                  Collaborator collaborator = collaborators[invocation.getInvocationIndex()];
                  assert collaborator == invocation.getInvokedInstance();
               }
            };
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyExpectationWithHandlerWhichFailsAssertion(Collaborator mock)
   {
      new Collaborator(0);

      new FullVerifications()
      {
         {
            new Collaborator(anyInt);
            forEachInvocation = new Object()
            {
               void checkIt(int i) { assert i > 0; }
            };
         }
      };
   }

   @Test
   public void recordExpectationWithHandlerForStaticMethod()
   {
      new Expectations()
      {
         final Collaborator unused = null;

         {
            Collaborator.staticMethod();
            forEachInvocation = new Delegate() { boolean staticInvocation() { return false; } };
         }
      };

      assertFalse(Collaborator.staticMethod());
   }

   @Test
   public void verifyExpectationsOnStaticMethodsWithHandlers(Collaborator unused)
   {
      Collaborator.staticMethod();
      Collaborator.staticMethod(1);
      Collaborator.staticMethod(2);
      Collaborator.staticMethod(3);

      new FullVerificationsInOrder()
      {
         {
            Collaborator.staticMethod();
            forEachInvocation = new Object() { boolean staticInvocation() { return false; } };

            Collaborator.staticMethod(1);
            forEachInvocation = new Object() { void verify(int i) { assert i == 1; } };

            Collaborator.staticMethod(anyInt); times = 2;
            forEachInvocation = new Object() { void verify(int i) { assert i == 2 || i == 3; } };
         }
      };
   }

   @Test
   public void recordExpectationWithInvocationHandlerWhichDefinesStaticHandlerMethod()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.doSomething(anyBoolean, null, null);
            //noinspection InstantiationOfUtilityClass
            forEachInvocation = new StaticDelegate();
         }
      };

      assertEquals("test", new Collaborator().doSomething(false, null, "replay"));
   }

   static final class StaticDelegate
   {
      static String verifyArgs(boolean b, int[] i, String s)
      {
         assertFalse(b);
         assertNull(i);
         assertEquals("replay", s);
         return "test";
      }
   }

   @Test
   public void verifyExpectationWithHandlerForNativeMethod(@NonStrict final Collaborator mock)
   {
      new Collaborator().nativeMethod(true);

      new Verifications()
      {
         {
            mock.nativeMethod(anyBoolean);
            forEachInvocation = new Object()
            {
               void verify(boolean b) { assertTrue(b); }
            };
         }
      };
   }

   @Test
   public void recordExpectationWithHandlerForFinalMethod()
   {
      new Expectations()
      {
         @NonStrict Collaborator mock;

         {
            mock.finalMethod();
            forEachInvocation = new Object() { char finalMethod() { return 'M'; } };
         }
      };

      assertEquals('M', new Collaborator().finalMethod());
   }

   @Test
   public void verifyExpectationWithHandlerForPrivateMethod(
      @NonStrict final Collaborator collaborator)
   {
      collaborator.privateMethod((short) 5);

      new VerificationsInOrder()
      {
         {
            invoke(collaborator, "privateMethod", (short) 5); times = 1;
            forEachInvocation = new Object() { void privateMethod(int i) { assert i == 5; } };
         }
      };
   }

   @Test
   public void recordExpectationWithHandlerForMethodWithCompatibleButDistinctParameterType()
   {
      new Expectations()
      {
         @NonStrict Collaborator collaborator;

         {
            collaborator.addElements(this.<Collection<String>> withNotNull());
            forEachInvocation = new Object()
            {
               void addElements(Collection<String> elements) { elements.add("test"); }
            };
         }
      };

      List<String> elements = new ArrayList<String>();
      new Collaborator().addElements(elements);

      assertTrue(elements.contains("test"));
   }

   @Test
   public void recordExpectationWithHandlerDefiningTwoMethods(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.doSomething(true, null, "str");
            forEachInvocation = new Object()
            {
               void doSomething(boolean b, int[] i, String s) { assert b; }
               private String someOther() { return ""; }
            };
         }
      };

      assertNull(collaborator.doSomething(true, null, "str"));
   }

   @Test
   public void verifyExpectationWithHandlerDefiningTwoNonPrivateMethods(
      final Collaborator collaborator)
   {
      collaborator.doSomething(true, null, "str");

      new Verifications()
      {
         {
            collaborator.doSomething(true, null, "str");

            try {
               forEachInvocation = new Object()
               {
                  void doSomething(boolean b, int[] i, String s) { assert b; }
                  void someOther() {}
               };
               fail();
            }
            catch (IllegalArgumentException e) {
               assert e.getMessage().startsWith("");
            }
         }
      };
   }

   @Test
   public void recordExpectationWithHandlerMissingNonPrivateMethod(final Collaborator collaborator)
   {
      new NonStrictExpectations()
      {
         {
            collaborator.doSomething(true, null, "str");

            try {
               forEachInvocation = new Object()
               {
                  private String someOther() { return ""; }
                  private void doSomethingElse(boolean b, int[] i, String s) {}
               };
               fail();
            }
            catch (IllegalArgumentException e) {
               assert e.getMessage().startsWith("No non-private ");
            }
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void handlerForFailedUnorderedVerification(final Collaborator mock)
   {
      mock.doSomething();

      new Verifications()
      {
         {
            mock.doSomething(); minTimes = 2;
            forEachInvocation = new Object() { void verify() {} };
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void handlerForFailedOrderedVerification(final Collaborator mock)
   {
      mock.doSomething();

      new VerificationsInOrder()
      {
         {
            mock.doSomething(); maxTimes = 0;
            forEachInvocation = new Object() { void verify() {} };
         }
      };
   }

   @Test
   public void verifyOrderedExpectationsWithHandlerForMultipleInvocations(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(2);
      mock.doSomething(3);

      final SequentialInvocationHandler handler = new SequentialInvocationHandler();

      new VerificationsInOrder()
      {
         {
            mock.doSomething(anyInt);
            forEachInvocation = handler;
            times = 3;
         }
      };

      assertEquals(3, handler.index);
   }

   static class SequentialInvocationHandler
   {
      int index;

      void verify(int i) { index++; assert i == index; }
   }

   @Test
   public void verifyUnorderedExpectationsWithHandlerForMultipleInvocations(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething(2);
      mock.doSomething(3);

      final SequentialInvocationHandler handler = new SequentialInvocationHandler();

      new FullVerifications()
      {
         {
            mock.doSomething(anyInt);
            forEachInvocation = handler;
            times = 3;
         }
      };

      assertEquals(3, handler.index);
   }

   @Test
   public void verifyExpectationWithHandlerWhichDefinesInvocationParameter(final Collaborator mock)
   {
      mock.doSomething(1);
      mock.doSomething();
      mock.doSomething(2);
      mock.doSomething(3);
      mock.finalMethod();
      mock.doSomething(4);

      final Object handler = new Object()
      {
         void verify(Invocation invocation, int i)
         {
            assert mock == invocation.getInvokedInstance();
            assert i == invocation.getInvocationCount();
         }
      };

      new VerificationsInOrder()
      {
         {
            mock.doSomething(anyInt);
            forEachInvocation = handler;
         }
      };

      new Verifications()
      {
         {
            mock.doSomething(anyInt);
            forEachInvocation = handler;
         }
      };
   }

   @Test
   public void verifyInvocationsWithHandlersHavingAlsoRecordedExpectations(final Collaborator mock)
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(anyInt);

         mock.doSomething(anyBoolean, null, null);
         result = new Delegate()
         {
            String delegate(boolean b, int[] i, String s)
            {
               assertTrue(b);
               assertNotNull(i);
               assertEquals("test", s);
               return "mocked";
            }
         };
      }};

      assertEquals("mocked", mock.doSomething(true, new int[0], "test"));
      mock.doSomething(1);
      assertEquals("mocked", mock.doSomething(true, new int[0], "test"));

      new Verifications()
      {{
         mock.doSomething(anyInt); times = 1;
         forEachInvocation = new Object()
         {
            void validate(int i) { assertEquals(1, i); }
         };
      }};

      new VerificationsInOrder()
      {{
         mock.doSomething(anyBoolean, null, null);
         forEachInvocation = new Object()
         {
            void validate(boolean b, int[] i, String s)
            {
               assertTrue(b);
               assertNotNull(i);
               assertEquals("test", s);
            }
         };
      }};
   }
}