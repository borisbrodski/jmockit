/*
 * JMockit Annotations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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

import java.sql.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class MockUpTest
{
   static final class Collaborator
   {
      final boolean b;

      Collaborator(boolean b) { this.b = b; }
      int doSomething(String s) { return s.length(); }
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
}
