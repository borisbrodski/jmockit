/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

@SuppressWarnings("UnusedDeclaration")
public final class InvocationProceedTest
{
   public static class ClassToBeMocked
   {
      private final String name;

      public ClassToBeMocked() { name = ""; }
      public ClassToBeMocked(String name) { this.name = name; }

      public void methodToBeMocked() { throw new UnsupportedOperationException("From real method"); }
      protected int methodToBeMocked(int i) throws IOException { return i; }

      private int methodToBeMocked(int i, Object... args)
      {
         int result = i;

         for (Object arg : args) {
            if (arg != null) result++;
         }

         return result;
      }

      String anotherMethodToBeMocked(String s, boolean b, List<Integer> ints)
      { return (b ? s.toUpperCase() : s.toLowerCase()) + ints; }

      public static boolean staticMethodToBeMocked() throws FileNotFoundException { throw new FileNotFoundException(); }

      static native void nativeMethod();
   }

   /// Tests for "@Mock" methods //////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void proceedFromMockMethodWithoutParameters()
   {
      new MockUp<ClassToBeMocked>() {
         @Mock(invocations = 1) void methodToBeMocked(Invocation inv) { inv.proceed(); }
      };

      try { new ClassToBeMocked().methodToBeMocked(); fail(); } catch (UnsupportedOperationException ignored) {}
   }

   @Test
   public void proceedFromMockMethodWithParameters() throws Exception
   {
      new MockUp<ClassToBeMocked>() {
         @Mock int methodToBeMocked(Invocation inv, int i) { int j = inv.proceed(); return j + 1; }

         @Mock(reentrant = true)
         String anotherMethodToBeMocked(Invocation inv, String s, boolean b, List<Number> ints)
         {
            if (!b) {
               return "";
            }

            ints.add(45);
            return inv.proceed();
         }

         @Mock(maxInvocations = 1)
         private int methodToBeMocked(Invocation inv, int i, Object... args)
         {
            args[2] = "mock";
            return inv.proceed();
         }
      };

      ClassToBeMocked mocked = new ClassToBeMocked();

      assertEquals(124, mocked.methodToBeMocked(123));
      assertEquals(-8, mocked.methodToBeMocked(-9));

      assertEquals("", mocked.anotherMethodToBeMocked(null, false, null));

      List<Integer> values = new ArrayList<Integer>();
      assertEquals("TEST[45]", mocked.anotherMethodToBeMocked("test", true, values));

      assertEquals(7, mocked.methodToBeMocked(3, "Test", new Object(), null, 45));
   }

   @Test
   public void proceedFromMockMethodWhichThrowsCheckedException()
   {
      new MockUp<ClassToBeMocked>() {
         @Mock(minInvocations = 1)
         boolean staticMethodToBeMocked(Invocation inv) throws Exception
         {
            if (inv.getInvocationIndex() == 0) {
               return inv.proceed();
            }

            throw new InterruptedException("fake");
         }
      };

      try { ClassToBeMocked.staticMethodToBeMocked(); fail(); } catch (FileNotFoundException ignored) {}

      try {
         ClassToBeMocked.staticMethodToBeMocked();
         fail();
      }
      catch (Exception e) {
         assertTrue(e instanceof InterruptedException);
      }
   }

   @Test
   public void proceedFromMockMethodIntoRealMethodWithModifiedArguments() throws Exception
   {
      class MockUpWhichModifiesArguments extends MockUp<ClassToBeMocked>
      {
         @Mock
         final int methodToBeMocked(Invocation invocation, int i) { return invocation.proceed(i + 2); }
      }

      new MockUpWhichModifiesArguments() {
         @Mock
         synchronized int methodToBeMocked(Invocation inv, int i, Object... args) { return inv.proceed(1, 2, "3"); }
      };

      ClassToBeMocked mocked = new ClassToBeMocked();
      assertEquals(3, mocked.methodToBeMocked(1));
      assertEquals(3, mocked.methodToBeMocked(-2, null, "Abc", true, 'a'));
   }

   @Test(expected = IllegalArgumentException.class)
   public void cannotProceedFromMockMethodIntoNativeMethod()
   {
      new MockUp<ClassToBeMocked>() {
         @Mock void nativeMethod(Invocation inv) { inv.proceed(); }
      };

      fail("Should not get here");
      ClassToBeMocked.nativeMethod();
   }

   @Test(expected = UnsupportedOperationException.class)
   public void cannotProceedFromMockMethodIntoConstructor() throws Exception
   {
      new MockUp<ClassToBeMocked>() {
//         ClassToBeMocked it;

         @Mock void $init(Invocation inv)
         {
//            assertNotNull(it);
//            assertSame(it, inv.getInvokedInstance());
            inv.proceed();
         }

//         @Mock void $init(Invocation inv, String arg)
//         {
//            assertNotNull(it);
//            assertSame(it, inv.getInvokedInstance());
//            inv.proceed("mock");
//         }
      };

      assertEquals("", new ClassToBeMocked().name);
//      assertEquals("mock", new ClassToBeMocked("test").name);
   }

   /// Tests for "Delegate" methods ///////////////////////////////////////////////////////////////////////////////////

   @Test
   public void proceedFromDelegateMethodWithoutParameters(@Injectable final ClassToBeMocked mocked)
   {
      new Expectations() {{
         mocked.methodToBeMocked();
         result = new Delegate() {
            void delegate(Invocation inv) { inv.proceed(); }
         };
      }};

      try { mocked.methodToBeMocked(); fail(); } catch (UnsupportedOperationException ignored) {}
   }

   @Test
   public void proceedFromDelegateMethodWithParameters() throws Exception
   {
      final ClassToBeMocked mocked = new ClassToBeMocked();

      new NonStrictExpectations(mocked) {{
         mocked.methodToBeMocked(anyInt);
         result = new Delegate() { int delegate(Invocation inv, int i) { int j = inv.proceed(); return j + 1; } };

         mocked.anotherMethodToBeMocked(anyString, anyBoolean, null);
         result = new Delegate() {
            String delegate(Invocation inv, String s, boolean b, List<Number> ints)
            {
               if (!b) {
                  return "";
               }

               ints.add(45);
               return inv.proceed();
            }
         };

         mocked.methodToBeMocked(anyInt, (Object[]) any); maxTimes = 1;
         result = new Delegate() {
            int delegate(Invocation inv, int i, Object... args)
            {
               args[2] = "mock";
               return inv.proceed();
            }
         };
      }};

      assertEquals(124, mocked.methodToBeMocked(123));
      assertEquals(-8, mocked.methodToBeMocked(-9));

      assertEquals("", mocked.anotherMethodToBeMocked(null, false, null));

      List<Integer> values = new ArrayList<Integer>();
      assertEquals("TEST[45]", mocked.anotherMethodToBeMocked("test", true, values));

      assertEquals(7, mocked.methodToBeMocked(3, "Test", new Object(), null, 45));
   }

   @Test
   public void proceedFromDelegateMethodIntoRealMethodWithModifiedArguments() throws Exception
   {
      final ClassToBeMocked mocked = new ClassToBeMocked();

      new Expectations(ClassToBeMocked.class) {{
         mocked.methodToBeMocked(anyInt);
         result = new Delegate() {
            int delegate1(Invocation invocation, int i) { return invocation.proceed(i + 2); }
         };

         mocked.methodToBeMocked(anyInt, (Object[]) any);
         result = new Delegate() {
            int delegate2(Invocation inv, int i, Object... args) { return inv.proceed(1, 2, "3"); }
         };
      }};

      assertEquals(3, mocked.methodToBeMocked(1));
      assertEquals(3, mocked.methodToBeMocked(-2, null, "Abc", true, 'a'));
   }

   @Test
   public void proceedFromDelegateMethodIntoConstructor()
   {
      new NonStrictExpectations(ClassToBeMocked.class) {{
         new ClassToBeMocked();
         result = new Delegate() {
            void init(Invocation inv)
            {
               assertNotNull(inv.getInvokedInstance());
               inv.proceed();
            }
         };

         new ClassToBeMocked(anyString);
         result = new Delegate() {
            void init(Invocation inv, String name)
            {
               assertNotNull(inv.getInvokedInstance());

               if ("proceed".equals(name)) {
                  inv.proceed();
               }
            }
         };
      }};

      assertEquals("", new ClassToBeMocked().name);
      assertEquals("proceed", new ClassToBeMocked("proceed").name);
      assertNull(new ClassToBeMocked("do not proceed").name);
   }

   @Test(expected = UnsupportedOperationException.class)
   public void cannotProceedFromDelegateMethodIntoConstructorWithNewArguments()
   {
      new Expectations(ClassToBeMocked.class) {{
         new ClassToBeMocked(anyString);
         result = new Delegate() {
            void init(Invocation inv, String name) { inv.proceed("mock"); }
         };
      }};

      new ClassToBeMocked("will fail");
   }
}
