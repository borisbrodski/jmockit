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

import org.junit.*;

import static org.junit.Assert.*;

public final class MisusedExpectationsTest
{
   @SuppressWarnings({"UnusedDeclaration"})
   static class Blah
   {
      int value() { return 0; }
      void setValue(int value) {}
      String doSomething(boolean b) { return ""; }
   }

   @Mocked Blah mock;

   @Test
   public void multipleReplayPhasesWithFirstSetOfExpectationsFullyReplayed()
   {
      // First record phase:
      new Expectations()
      {{
         new Blah().value(); result = 5;
      }};

      // First replay phase:
      assertEquals(5, new Blah().value());

      // Second record phase:
      new Expectations()
      {{
         mock.value(); result = 6;
         mock.value(); result = 3;
      }};

      // Second replay phase:
      assertEquals(6, mock.value());
      assertEquals(3, mock.value());
   }

   @Test
   public void multipleReplayPhasesWithFirstSetOfExpectationsPartiallyReplayed()
   {
      // First record phase:
      new Expectations()
      {{
         mock.value(); returns(1, 2);
      }};

      // First replay phase:
      assertEquals(1, mock.value());

      // Second record phase:
      new Expectations()
      {{
         mock.value(); returns(3, 4);
      }};

      // Second replay phase:
      assertEquals(2, mock.value());
      assertEquals(3, mock.value());
      assertEquals(4, mock.value());
   }

   @Test(expected = IllegalArgumentException.class)
   public void recordDuplicateInvocationWithNoArguments()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
         mock.value(); result = 2;
      }};
   }

   @Test(expected = IllegalArgumentException.class)
   public void recordDuplicateInvocationWithArgumentMatcher()
   {
      new NonStrictExpectations()
      {{
         mock.setValue(anyInt);
         mock.setValue(anyInt); result = new UnknownError();
      }};
   }

   @Test(expected = IllegalArgumentException.class)
   public void recordDuplicateInvocationInSeparateNonStrictExpectationBlocks()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
      }};

      new NonStrictExpectations()
      {{
         mock.value(); result = 2;
      }};
   }

   @Test(expected = IllegalArgumentException.class)
   public void recordSameInvocationInNonStrictExpectationBlockThenInStrictOne()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
      }};

      new Expectations()
      {{
         mock.value(); result = 2;
      }};
   }

   @Test
   public void recordNonStrictExpectationAfterInvokingSameMethodInReplayPhase()
   {
      assertEquals(0, mock.value());

      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
      }};

      assertEquals(1, mock.value());
   }

   @Test
   public void recordStrictExpectationAfterInvokingSameMethodInReplayPhase() throws Exception
   {
      assertEquals(0, mock.value());

      new Expectations()
      {{
         mock.value(); result = 1;
      }};

      assertEquals(1, mock.value());
   }

   @Test
   public void recordInvocationUsingDynamicMockingWhichDiffersOnlyOnTheMatchedInstance()
   {
      final Blah blah = new Blah();

      new NonStrictExpectations(blah)
      {{
         onInstance(mock).doSomething(true); result = "first";
         blah.value(); result = 123;
         onInstance(blah).doSomething(true); result = "second";
      }};

      assertEquals("first", mock.doSomething(true));
      assertEquals("second", blah.doSomething(true));
   }

   public static class Foo
   {
      void doIt() {}
   }

   public static class SubFoo extends Foo
   {
   }

   @Test(expected = IllegalArgumentException.class)
   public void recordDuplicateInvocationOnTwoDynamicMocksOfDifferentTypesButSharedBaseClass()
   {
      final Foo f1 = new Foo();
      final SubFoo f2 = new SubFoo();

      new NonStrictExpectations(f1, f2)
      {{
         f1.doIt();
         f2.doIt();
      }};
   }

   @Test
   public void recordAmbiguousExpectationsUsingArgumentMatchers()
   {
      new NonStrictExpectations()
      {{
         mock.setValue(1);
         mock.setValue(anyInt); result = new UnknownError();

         mock.doSomething(withEqual(true)); result = "first";
         mock.doSomething(withNotEqual(false)); result = "second";
      }};

      mock.setValue(1);
      mock.setValue(1); // won't throw an error

      assertNull(mock.doSomething(false));
      assertEquals("first", mock.doSomething(true));
   }
}
