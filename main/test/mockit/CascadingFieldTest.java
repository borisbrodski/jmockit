/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

public final class CascadingFieldTest
{
   static class Foo
   {
      Bar getBar() { return null; }

      static Bar globalBar() { return null; }

      void doSomething(String s) { throw new RuntimeException(s); }
      int getIntValue() { return 1; }
      private Boolean getBooleanValue() { return true; }
      String getStringValue() { return "abc"; }
      public final Date getDate() { return null; }
      final List<Integer> getList() { return null; }
   }

   static class Bar
   {
      Bar() { throw new RuntimeException(); }
      int doSomething() { return 1; }
      boolean isDone() { return false; }
      Short getShort() { return 1; }
   }

   @Cascading Foo foo;

   @Before
   public void recordCommonExpectations()
   {
      new NonStrictExpectations()
      {{
         foo.getBar().isDone(); result = true;
      }};
   }

   @Test
   public void cascadeOneLevel()
   {
      assertTrue(foo.getBar().isDone());
      assertEquals(0, foo.getBar().doSomething());
      assertEquals(0, Foo.globalBar().doSomething());
      assertNotSame(foo.getBar(), Foo.globalBar());
      assertNull(foo.getBar().getShort());

      foo.doSomething("test");
      assertEquals(0, foo.getIntValue());
      assertNull(foo.getBooleanValue());
      assertNull(foo.getStringValue());
      assertNotNull(foo.getDate());
      assertTrue(foo.getList().isEmpty());

      new Verifications() {{ foo.doSomething(anyString); }};
   }

   @Test
   public void exerciseCascadingMockAgain()
   {
      assertTrue(foo.getBar().isDone());
   }
}
