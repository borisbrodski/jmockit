/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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

@SuppressWarnings({"UnusedDeclaration"})
public final class NonStrictExpectationsTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public int editABunchMoreStuff() { return 1; }
      public boolean notifyBeforeSave() { return true; }
      public void prepare() {}
      public void save() {}

      static int staticMethod(Object o, Exception e) { return -1; }
   }

   @Mocked Dependency mock;

   private void exerciseCodeUnderTest()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomething(45);
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save();
   }

   @Test
   public void recordSimpleInvocations()
   {
      new NonStrictExpectations()
      {{
         mock.prepare();
         mock.editABunchMoreStuff();
         mock.setSomething(45);
      }};

      exerciseCodeUnderTest();
   }

   @Test
   public void recordInvocationThatWillNotOccur()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); returns(123);
      }};

      mock.setSomething(123);
      mock.prepare();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithExactExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); repeats(1);
      }};
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithMinimumExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); repeatsAtLeast(2);
      }};

      mock.editABunchMoreStuff();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithMaximumExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); repeatsAtMost(1);
      }};

      mock.editABunchMoreStuff();
      mock.editABunchMoreStuff();
   }

   @Test
   public void recordInvocationsWithExpectedInvocationCounts()
   {
      new NonStrictExpectations()
      {{
         mock.setSomethingElse(withAny("")); repeatsAtLeast(1);
         mock.save(); repeats(2);
      }};

      mock.setSomething(3);
      mock.save();
      mock.setSomethingElse("test");
      mock.save();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationsWithMinInvocationCountLargerThanWillOccur()
   {
      new NonStrictExpectations()
      {{
         mock.save(); repeatsAtLeast(2);
      }};

      mock.save();
   }

   @Test
   public void recordWithArgumentMatcherAndIndividualInvocationCounts()
   {
      new NonStrictExpectations(1)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.save(); repeats(1);
      }};

      exerciseCodeUnderTest();
   }

   @Test
   public void recordWithMaxInvocationCountFollowedByReturnValue()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         repeatsAtMost(1);
         returns(1);
      }};

      assertEquals(1, Dependency.staticMethod(new Object(), new Exception()));
   }

   @Test(expected = AssertionError.class)
   public void recordWithMaxInvocationCountFollowedByReturnValueButReplayOneTimeBeyondMax()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         repeatsAtMost(1);
         returns(1);
      }};

      Dependency.staticMethod(null, null);
      Dependency.staticMethod(null, null);
   }

   @Test
   public void recordWithReturnValueFollowedByExpectedInvocationCount()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         returns(1);
         repeats(1);
      }};

      assertEquals(1, Dependency.staticMethod(null, null));
   }

   @Test
   public void recordWithMinInvocationCountFollowedByReturnValueUsingDelegate()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         repeatsAtLeast(1);
         returns(new Delegate()
         {
            int staticMethod(Object o, Exception e) { return 1; }
         });
      }};

      assertEquals(1, Dependency.staticMethod(null, null));
   }

   @Test
   public void recordInvocationsInIteratingBlock()
   {
      new NonStrictExpectations(2)
      {{
         mock.setSomething(withAny(1)); repeats(1);
         mock.save(); repeats(1);
      }};

      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationInBlockWithWrongNumberOfIterations()
   {
      new NonStrictExpectations(3)
      {{
         mock.setSomething(123); repeatsAtLeast(1);
      }};

      mock.setSomething(123);
   }

   @Test
   public void recordWithArgumentMatcherAndIndividualInvocationCountsInIteratingBlock()
   {
      new NonStrictExpectations(2)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.editABunchMoreStuff(); repeats(1, 5);
         mock.save(); repeats(1);
      }};

      for (int i = 0; i < 2; i++) {
         exerciseCodeUnderTest();
      }
   }
}
