/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class MockUpTest
{
   static final class Collaborator
   {
      final boolean b;

      Collaborator() { b = false; }
      Collaborator(boolean b) { this.b = b; }
      int doSomething(String s) { return s.length(); }

      @SuppressWarnings({"UnusedDeclaration"})
      <N extends Number> N genericMethod(N n) { return null; }
   }

   @Test
   public void mockUpClass() throws Exception
   {
      new MockUp<Collaborator>()
      {
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

   @Test
   public void mockUpInterface() throws Exception
   {
      ResultSet mock = new MockUp<ResultSet>()
      {
         @Mock
         boolean next() { return true; }
      }.getMockInstance();

      assertTrue(mock.next());
   }

   @Test
   public <M extends Runnable & ResultSet> void mockUpTwoInterfacesAtOnce() throws Exception
   {
      M mock = new MockUp<M>()
      {
         @Mock(invocations = 1)
         void run() {}

         @Mock
         boolean next() { return true; }
      }.getMockInstance();

      mock.run();
      assertTrue(mock.next());
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
      AtomicIntegerFieldUpdater<?> atomicCount =
         Deencapsulation.getField(Main.class, AtomicIntegerFieldUpdater.class);

      new MockUp<AtomicIntegerFieldUpdater<Main>>(atomicCount.getClass())
      {
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

   @Test
   public void mockGenericMethod()
   {
      new MockUp<Collaborator>()
      {
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

   public static final class GenericClass<T>
   {
      public void methodWithGenericParameter(T t) { System.out.println("t=" + t); }
   }

   @Ignore @Test
   public void mockMethodWithGenericTypeArgument()
   {
      // Currently, this isn't supported but it can be. If the mock method has a generic signature,
      // then it can be used when comparing to real methods. The MockUp class can pass the type
      // arguments ("StringBuilder") defined for "MockUp<T>".
      new MockUp<GenericClass<StringBuilder>>()
      {
         @Mock
         public void methodWithGenericParameter(StringBuilder s)
         {
            s.setLength(0);
            s.append("mock");
         }
      };

      StringBuilder s = new StringBuilder("test");
      new GenericClass<StringBuilder>().methodWithGenericParameter(s);
      assertEquals("mock", s.toString());
   }

   public interface GenericInterface<T> { void method(T t); }
   public interface ConcreteInterface extends GenericInterface<Long> {}

   @Ignore @Test
   public void mockMethodOfSubInterfaceWithGenericTypeArgument()
   {
      ConcreteInterface mock = new MockUp<ConcreteInterface>()
      {
         @Mock(invocations = 1)
         public void method(Long l)
         {
            assertTrue(l > 0);
         }
      }.getMockInstance();

      mock.method(123L);
   }
}
