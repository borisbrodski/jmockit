/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
public final class MockUpTest
{
   @Test(expected = IllegalArgumentException.class)
   public void attemptToCreateMockUpWithoutTheTypeToBeMocked()
   {
      new MockUp() {};
   }

   @Deprecated
   static final class Collaborator
   {
      @Deprecated final boolean b;

      @Deprecated Collaborator() { b = false; }
      Collaborator(boolean b) { this.b = b; }

      @Ignore("test") int doSomething(@Deprecated String s) { return s.length(); }

      @SuppressWarnings("UnusedDeclaration")
      <N extends Number> N genericMethod(N n) { return null; }

      @Deprecated static boolean doSomethingElse() { return false; }
   }

   // Mock-ups for non-generic classes ////////////////////////////////////////////////////////////////////////////////

   @Test(expected = IllegalArgumentException.class)
   public void attemptToCreateMockUpWithMockMethodLackingCorrespondingRealMethod()
   {
      new MockUp<Collaborator>() { @Mock void $init(int i) { System.out.println(i); } };
   }

   @Test
   public void mockUpClass() throws Exception
   {
      new MockUp<Collaborator>() {
         @Mock(invocations = 1)
         void $init(boolean b)
         {
            assertTrue(b);
         }

         @Mock(minInvocations = 1)
         int doSomething(String s)
         {
            assertEquals("test", s);
            return 123;
         }
      };

      assertEquals(123, new Collaborator(true).doSomething("test"));
   }

   static final class Main
   {
      static final AtomicIntegerFieldUpdater<Main> atomicCount =
         AtomicIntegerFieldUpdater.newUpdater(Main.class, "count");

      volatile int count;
      int max = 2;

      boolean increment()
      {
         while (true) {
            int currentCount = count;

            if (currentCount >= max) {
               return false;
            }

            if (atomicCount.compareAndSet(this, currentCount, currentCount + 1)) {
               return true;
            }
         }
      }
   }

   @Test
   public void mockUpGivenClass()
   {
      final Main main = new Main();
      AtomicIntegerFieldUpdater<?> atomicCount = Deencapsulation.getField(Main.class, AtomicIntegerFieldUpdater.class);

      new MockUp(atomicCount.getClass()) {
         boolean second;

         @Mock(invocations = 2)
         public boolean compareAndSet(Object obj, int expect, int update)
         {
            assertSame(main, obj);
            assertEquals(0, expect);
            assertEquals(1, update);

            if (second) {
               return true;
            }

            second = true;
            return false;
         }
      };

      assertTrue(main.increment());
   }

   // Mock-ups for non-generic interfaces /////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockUpInterface() throws Exception
   {
      ResultSet mock = new MockUp<ResultSet>() {
         @Mock
         boolean next() { return true; }
      }.getMockInstance();

      assertTrue(mock.next());
   }

   @Test
   public void mockUpGivenInterface()
   {
      Runnable r = new MockUp<Runnable>(Runnable.class) {
         @Mock(minInvocations = 1)
         public void run() {}
      }.getMockInstance();

      r.run();
   }

   @Test
   public <M extends Runnable & ResultSet> void mockUpTwoInterfacesAtOnce() throws Exception
   {
      M mock = new MockUp<M>() {
         @Mock(invocations = 1)
         void run() {}

         @Mock
         boolean next() { return true; }
      }.getMockInstance();

      mock.run();
      assertTrue(mock.next());
   }

   // Mock-ups for generic classes/methods ////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockGenericMethod()
   {
      new MockUp<Collaborator>() {
         @Mock <T extends Number> T genericMethod(T t) { return t; }

         // This also works (same erasure):
         // @Mock Number genericMethod(Number t) { return t; }
      };

      Integer n = new Collaborator().genericMethod(123);
      assertEquals(123, n.intValue());

      Long l = new Collaborator().genericMethod(45L);
      assertEquals(45L, l.longValue());

      Short s = new Collaborator().genericMethod((short) 6);
      assertEquals(6, s.shortValue());

      Double d = new Collaborator().genericMethod(0.5);
      assertEquals(0.5, d, 0);
   }

   @SuppressWarnings("UnusedParameters")
   public static final class GenericClass<T1, T2>
   {
      public void aMethod(T1 t) { throw new RuntimeException("t=" + t); }
      public int anotherMethod(T1 t, int i, T2 p) { return 2 * i; }
      public int anotherMethod(Integer t, int i, String p) { return -2 * i; }
   }

   @Test
   public void mockGenericClassWithUnspecifiedTypeArguments()
   {
      new MockUp<GenericClass<?, ?>>() {
         @Mock(minInvocations = 1)
         void aMethod(Object o)
         {
            StringBuilder s = (StringBuilder) o;
            s.setLength(0);
            s.append("mock");
            s.toString();
         }

         @Mock
         int anotherMethod(Object o, int i, Object list)
         {
            assertTrue(o instanceof StringBuilder);
            //noinspection unchecked
            assertEquals(0, ((Collection<String>) list).size());
            return -i;
         }
      };

      StringBuilder s = new StringBuilder("test");
      GenericClass<StringBuilder, List<String>> g = new GenericClass<StringBuilder, List<String>>();

      g.aMethod(s);
      int r1 = g.anotherMethod(new StringBuilder("test"), 58, Collections.<String>emptyList());
      int r2 = g.anotherMethod(123, 65, "abc");

      assertEquals("mock", s.toString());
      assertEquals(-58, r1);
      assertEquals(-130, r2);
   }

   @Test
   public void mockBothGenericAndNonGenericMethodsInGenericClass()
   {
      new MockUp<GenericClass<String, Boolean>>() {
         @Mock int anotherMethod(Integer t, int i, String p) { return 2; }
         @Mock int anotherMethod(String t, int i, Boolean p) { return 1; }
      };

      GenericClass<String, Boolean> o = new GenericClass<String, Boolean>();
      assertEquals(1, o.anotherMethod("generic", 1, true));
      assertEquals(2, o.anotherMethod(123, 2, "non generic"));
   }

   @Test(expected = IllegalArgumentException.class)
   public void cannotMockGenericClassMethodWhenParameterTypeInMockMethodDiffersFromTypeArgument()
   {
      new MockUp<GenericClass<String, Boolean>>() {
         @Mock void aMethod(Integer t) {}
      };
   }

   static class GenericBaseClass<T, U> { U find(@SuppressWarnings("UnusedParameters") T id) { return null; } }

   @Test
   public void mockGenericMethodWithMockMethodHavingParameterTypesMatchingTypeArguments()
   {
      new MockUp<GenericBaseClass<String, Integer>>() {
         @Mock
         Integer find(String id) { return id.hashCode(); }
      };

      int i = new GenericBaseClass<String, Integer>().find("test");
      assertEquals("test".hashCode(), i);
   }

   @Test
   public void cannotCallGenericMethodWhenSomeMockMethodExpectsDifferentTypes()
   {
      new MockUp<GenericBaseClass<String, Integer>>() { @Mock Integer find(String id) { return 1; } };

      try {
         new GenericBaseClass<Integer, String>().find(1);
         fail();
      }
      catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().startsWith("Failure to invoke method: "));
      }
   }

   static class NonGenericSuperclass extends GenericBaseClass<Integer, String> {}
   final class NonGenericSubclass extends NonGenericSuperclass {}

   @Test
   public void mockGenericMethodFromInstantiationOfNonGenericSubclass()
   {
      new MockUp<NonGenericSubclass>() {
         @Mock
         String find(Integer id) { return "mocked" + id; }
      };

      String s = new NonGenericSubclass().find(1);
      assertEquals("mocked1", s);
   }

   static class GenericSuperclass<I> extends GenericBaseClass<I, String> {}
   final class AnotherNonGenericSubclass extends GenericSuperclass<Integer> {}

   @Test
   public void mockGenericMethodFromInstantiationOfNonGenericSubclassWhichExtendsAGenericIntermediateSuperclass()
   {
      new MockUp<AnotherNonGenericSubclass>() {
         @Mock
         String find(Integer id) { return "mocked" + id; }
      };

      String s = new AnotherNonGenericSubclass().find(1);
      assertEquals("mocked1", s);
   }

   @SuppressWarnings("UnusedParameters")
   static class NonGenericClassWithGenericMethods
   {
      static <T> T staticMethod(Class<T> cls, String s) { throw new RuntimeException(); }
      <C> void instanceMethod(Class<C> cls, String s) { throw new RuntimeException(); }
      <N extends Number> void instanceMethod(Class<N> cls) { throw new RuntimeException(); }
   }

   @Test
   public void mockGenericMethodsOfNonGenericClass()
   {
      new MockUp<NonGenericClassWithGenericMethods>() {
         @Mock <T> T staticMethod(Class<T> cls, String s) { return null; }
         @Mock <C> void instanceMethod(Class<C> cls, String s) {}
         @Mock void instanceMethod(Class<?> cls) {}
      };

      new NonGenericClassWithGenericMethods().instanceMethod(Integer.class);
      NonGenericClassWithGenericMethods.staticMethod(Collaborator.class, "test1");
      new NonGenericClassWithGenericMethods().instanceMethod(Byte.class, "test2");
   }

   // Mock-ups for generic interfaces /////////////////////////////////////////////////////////////////////////////////

   public interface GenericInterface<T> { void method(T t); }

   @Test
   public void mockGenericInterfaceMethodWithMockMethodHavingParameterOfTypeObject()
   {
      GenericInterface<Boolean> mock = new MockUp<GenericInterface<Boolean>>() {
         @Mock
         void method(Object b) { assertTrue((Boolean) b); }
      }.getMockInstance();

      mock.method(true);
   }

   public interface NonGenericSubInterface extends GenericInterface<Long> {}

   @MockClass(realClass = NonGenericSubInterface.class)
   static final class MockForNonGenericSubInterface
   {
      @Mock(invocations = 1)
      void method(Long l) { assertTrue(l > 0); }
   }

   @Test
   public void mockMethodOfSubInterfaceWithGenericTypeArgument()
   {
      NonGenericSubInterface mock = new MockUp<NonGenericSubInterface>() {
         @Mock(invocations = 1)
         void method(Long l) { assertTrue(l > 0); }
      }.getMockInstance();

      mock.method(123L);
   }

   @Test(expected = IllegalArgumentException.class)
   public void cannotMockGenericInterfaceMethodWhenParameterTypeInMockMethodDiffersFromTypeArgument()
   {
      new MockUp<Comparable<String>>() { @Mock int compareTo(Integer i) { return 1; } };
   }

   @Test
   public void mockGenericInterfaceMethod()
   {
      Comparable<Integer> cmp = new MockUp<Comparable<Integer>>() {
         @Mock
         int compareTo(Integer i) { assertEquals(123, i.intValue()); return 2; }
      }.getMockInstance();

      assertEquals(2, cmp.compareTo(123));
   }

   // Mock-ups for other situations ///////////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockUpWithItFieldAndReentrantMockMethod()
   {
      new MockUp<Collaborator>() {
         Collaborator it;

         @Mock(invocations = 1, reentrant = false)
         void $init(boolean b)
         {
            assertFalse(it.b);
            assertTrue(b);
         }

         @Mock(reentrant = true)
         int doSomething(String s)
         {
            return it.doSomething(s + ": mocked");
         }
      };

      int i = new Collaborator(true).doSomething("test");

      assertEquals(12, i);
   }

   @Test(expected = IllegalArgumentException.class)
   public void cannotReenterConstructors()
   {
      new MockUp<Collaborator>() {
         @Mock(reentrant = true) void $init(boolean b) {}
      };
   }

   @Test
   public void mockingOfAnnotatedClass() throws Exception
   {
      new MockUp<Collaborator>() {
         @Mock void $init() {}
         @Mock int doSomething(String s) { assertNotNull(s); return 123; }
         @Mock(reentrant = true) boolean doSomethingElse() { return true; }
      };

      assertEquals(123, new Collaborator().doSomething(""));

      assertTrue(Collaborator.class.isAnnotationPresent(Deprecated.class));
      assertTrue(Collaborator.class.getDeclaredField("b").isAnnotationPresent(Deprecated.class));
      assertTrue(Collaborator.class.getDeclaredConstructor().isAnnotationPresent(Deprecated.class));

      Method mockedMethod = Collaborator.class.getDeclaredMethod("doSomething", String.class);
      Ignore ignore = mockedMethod.getAnnotation(Ignore.class);
      assertNotNull(ignore);
      assertEquals("test", ignore.value());
      assertTrue(mockedMethod.getParameterAnnotations()[0][0] instanceof Deprecated);

      assertTrue(Collaborator.doSomethingElse());
      assertTrue(Collaborator.class.getDeclaredMethod("doSomethingElse").isAnnotationPresent(Deprecated.class));
   }

   static class A
   {
      void method1() { throw new RuntimeException("1"); }
      void method2() { throw new RuntimeException("2"); }
   }

   @Test
   public void mockSameClassTwiceUsingSeparateMockups()
   {
      A a = new A();

      class MockUp1 extends MockUp<A> { @Mock void method1() {} }
      new MockUp1();
      a.method1();

      new MockUp<A>() { @Mock void method2() {} };
      a.method1(); // still mocked
      a.method2();
   }

   interface B { int aMethod(); }

   @Test
   public void mockNonPublicInterface()
   {
      B b = new MockUp<B>() {
         @Mock int aMethod() { return 1; }
      }.getMockInstance();

      assertEquals(1, b.aMethod());
   }

   public interface C
   {
      int method1();
      int method2();
   }

   @Test
   public void mockSameInterfaceTwiceUsingSeparateMockups()
   {
      class MockUp1 extends MockUp<C> { @Mock int method1() { return 1; } }
      C c1 = new MockUp1().getMockInstance();
      assertEquals(1, c1.method1());
      assertEquals(0, c1.method2());

      C c2 = new MockUp<C>() { @Mock int method2() { return 2; } }.getMockInstance();
      assertEquals(0, c2.method1()); // not mocked because c2 belongs to a second implementation class for C
      assertEquals(2, c2.method2());

      // Instances c1 and c2 belong to different mocked classes, so c1 is unaffected:
      assertEquals(1, c1.method1());
      assertEquals(0, c1.method2());
   }
}
