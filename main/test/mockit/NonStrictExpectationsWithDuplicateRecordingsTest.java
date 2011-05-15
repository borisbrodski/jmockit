/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class NonStrictExpectationsWithDuplicateRecordingsTest
{
   @SuppressWarnings({"UnusedDeclaration"})
   static class Blah
   {
      void setValue(int value) {}
      String doSomething(boolean b) { return ""; }
      String doSomething(String s) { return ""; }
      long doSomething(Long l) { return -1L; }
      long doSomething(Long l, Object o) { return 1L; }
      boolean doSomething(int i) { return true; }
      int doSomething(char c) { return 123; }
   }

   @Mocked Blah mock;

   @Test
   public void recordSameMethodWithDisjunctiveArgumentMatchers()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(withEqual(1)); result = false;
         mock.doSomething(withNotEqual(1)); result = true;
      }};

      assertFalse(mock.doSomething(1));
      assertTrue(mock.doSomething(2));
      assertTrue(mock.doSomething(0));
      assertFalse(mock.doSomething(1));
   }

   @Test
   public void recordAmbiguousExpectationsUsingConstantArgumentValueAndArgumentMatcher()
   {
      new NonStrictExpectations()
      {{
         mock.setValue(1);
         mock.setValue(anyInt); result = new UnknownError();
      }};

      mock.setValue(1);
      mock.setValue(1); // won't throw an error
   }

   @Test
   public void recordAmbiguousExpectationsUsingArgumentMatchers()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(withNotEqual('x')); result = 1;
         mock.doSomething(anyChar); result = 2;
      }};

      assertEquals(1, mock.doSomething('W'));
      assertEquals(2, mock.doSomething('x'));
   }

   @Test
   public void recordSameMethodWithIdenticalArgumentMatchers()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(anyInt); result = false;
         mock.doSomething(anyInt); result = true; // overrides the previous expectation

         mock.doSomething(withNotEqual(5L), withInstanceOf(String.class)); result = 1L;
         mock.doSomething(withNotEqual(5L), withInstanceOf(String.class)); result = 2L; // same here
      }};

      assertTrue(mock.doSomething(1));
      assertTrue(mock.doSomething(0));

      assertEquals(2, mock.doSomething(null, "test 1"));
      assertEquals(2, mock.doSomething(1L, "test 2"));
   }

   @Test
   public void recordSameMethodWithOverlappingArgumentMatchers()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(withEqual(0));
         mock.doSomething(anyInt); result = true;

         mock.doSomething((Long) withNull()); result = 1L;
         mock.doSomething((Long) any); result = 2L;
      }};

      assertTrue(mock.doSomething(1));
      assertFalse(mock.doSomething(0));

      assertEquals(1, mock.doSomething((Long) null));
      assertEquals(2, mock.doSomething(1L));
   }

   @Test
   public void recordSameMethodWithOverlappingArgumentMatchersButInTheWrongOrder()
   {
      new NonStrictExpectations()
      {{
         // Invalid, since the least specific expectation is recorded first:
         mock.doSomething((String) any); result = "";
         mock.doSomething(withEqual("str")); result = null;
      }};

      assertEquals("", mock.doSomething((String) null)); // ok, matches only one expectation
      assertNotNull(mock.doSomething("str")); // not ok, since the most specific won't be matched
   }

   @Test
   public void recordSameMethodWithExactArgumentAndArgMatcher()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(anyInt); result = false;
         mock.doSomething(1); result = true;
      }};

      assertTrue(mock.doSomething(1)); // matches last recorded expectation
      assertFalse(mock.doSomething(2)); // matches only one expectation
   }

   @Test
   public void recordSameMethodWithArgumentsOrMatchersOfVaryingSpecificity()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(true); result = null;
         mock.doSomething(anyBoolean); result = "a";

         mock.doSomething(1); result = true;
         mock.doSomething(anyInt); result = false;

         mock.doSomething(withEqual('c')); result = 1;
         mock.doSomething(anyChar); result = 2;

         mock.doSomething((String) withNull());
         mock.doSomething(withEqual("str")); result = "b";
         mock.doSomething(anyString); result = "c";
      }};

      assertEquals("a", mock.doSomething(false)); // matches only one expectation
      assertNull(mock.doSomething(true)); // matches two, but most specific was recorded first

      assertTrue(mock.doSomething(1)); // matches two, but most specific came first
      assertFalse(mock.doSomething(2)); // matches only one expectation

      assertEquals(1, mock.doSomething('c')); // matches the first and most specific
      assertEquals(2, mock.doSomething('3')); // matches only one
      assertEquals(2, mock.doSomething('x')); // matches only one

      assertNull(mock.doSomething((String) null)); // matches one specific expectation
      assertEquals("b", mock.doSomething("str")); // matches another specific expectation
      assertEquals("c", mock.doSomething("")); // matches the non-specific expectation
   }
}
