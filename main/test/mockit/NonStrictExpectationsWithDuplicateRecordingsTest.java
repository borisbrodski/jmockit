/*
 * JMockit Expectations
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
      long doSomething(Long o) { return -1L; }
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
         // Ok when recorded from most specific to least specific:
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
   public void recordSameMethodWithExactArgumentAndArgMatcherButInWrongOrder()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(anyInt); result = false;
         mock.doSomething(1); result = true;
      }};

      assertFalse(mock.doSomething(1)); // not ok, matches two but most specific comes last
      assertFalse(mock.doSomething(2)); // ok, matches only one expectation
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
