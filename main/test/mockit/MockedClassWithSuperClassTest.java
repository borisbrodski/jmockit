/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.*;

public final class MockedClassWithSuperClassTest
{
   static final class SubclassOfJREClass extends Writer
   {
      @Override public void write(char[] cbuf, int off, int len) {}
      @Override public void flush() {}
      @Override public void close() { throw new UnsupportedOperationException(); }
   }

   @Test
   public void mockedClassExtendingJREClass(final SubclassOfJREClass mock) throws Exception
   {
      new NonStrictExpectations() {{
         mock.append(anyChar); result = mock;
      }};

      // Mocked:
      assertNull(mock.append("a"));
      assertSame(mock, mock.append('a'));
      mock.close();

      // Not mocked:
      Writer w = new Writer() {
         @Override public void write(char[] cbuf, int off, int len) {}
         @Override public void flush() {}
         @Override public void close() {}
      };
      assertSame(w, w.append("Test1"));
      assertSame(w, w.append('b'));
   }

   static class BaseClass { int doSomething() { return 123; }}
   static final class Subclass extends BaseClass {}

   @Test
   public void mockedClassExtendingNonJREClass(@Mocked final Subclass mock)
   {
      new NonStrictExpectations() {{ mock.doSomething(); result = 45; }};

      // Mocked:
      assertEquals(45, mock.doSomething());
      assertEquals(45, new Subclass().doSomething());

      // Not mocked:
      BaseClass b1 = new BaseClass();
      BaseClass b2 = new BaseClass() { @Override int doSomething() { return super.doSomething() - 23; } };
      assertEquals(123, b1.doSomething());
      assertEquals(100, b2.doSomething());

      new Verifications() {{ mock.doSomething(); times = 2; }};
   }
}
