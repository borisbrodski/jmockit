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

public final class ExpectationsWithIterationsTest
{
   @SuppressWarnings({"UnusedDeclaration"})
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public int editABunchMoreStuff() { return 1; }
      public boolean notifyBeforeSave() { return true; }
      public void prepare() {}
      public void save() {}
   }

   @Mocked private Dependency mock;

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
   public void recordWithArgumentMatcherAndIndividualInvocationCounts()
   {
      new Expectations(1)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.setSomethingElse(withAny("")); notStrict();
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.notifyBeforeSave(); notStrict();
         mock.save();
      }};

      exerciseCodeUnderTest();
   }

   @Test
   public void recordStrictInvocationsInIteratingBlock()
   {
      new Expectations(2)
      {{
         mock.setSomething(withAny(1));
         mock.save();
      }};

      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();
   }

   @Test
   public void recordNonStrictInvocationsInIteratingBlock()
   {
      new Expectations(2)
      {{
         mock.setSomething(withAny(1)); notStrict();
         mock.save(); notStrict();
      }};

      mock.setSomething(123);
      mock.setSomething(45);
      mock.save();
      mock.save();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationInBlockWithWrongNumberOfIterations()
   {
      new Expectations(3)
      {{
         mock.setSomething(123);
      }};

      mock.setSomething(123);
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationInBlockWithNumberOfIterationsTooSmall()
   {
      new Expectations(2)
      {{
         mock.setSomething(123);
         mock.editABunchMoreStuff();
      }};

      for (int i = 0; i < 3; i++) {
         mock.setSomething(123);
         mock.editABunchMoreStuff();
      }
   }

   @Test
   public void recordWithArgumentMatcherAndIndividualInvocationCountsInIteratingBlock()
   {
      new Expectations(2)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.editABunchMoreStuff(); repeats(1, 5);
         mock.save();
      }};

      for (int i = 0; i < 2; i++) {
         mock.prepare();
         mock.setSomething(123);
         mock.setSomething(45);
         mock.editABunchMoreStuff();
         mock.editABunchMoreStuff();
         mock.editABunchMoreStuff();
         mock.save();
      }
   }

   @Test
   public void recordRepeatingInvocationInIteratingBlock()
   {
      new Expectations(2)
      {{
         mock.setSomething(123); repeats(2);
      }};

      mock.setSomething(123);
      mock.setSomething(123);
      mock.setSomething(123);
      mock.setSomething(123);
   }

   @Test
   public void recordInvocationsInASimpleBlockFollowedByAnIteratingOne()
   {
      new Expectations()
      {{
         mock.setSomething(123);
      }};

      new Expectations(2)
      {{
         mock.save();
      }};

      mock.setSomething(123);
      mock.save();
      mock.save();
   }

   @SuppressWarnings({"MethodWithMultipleLoops"})
   @Test
   public void recordInvocationsInMultipleIteratingBlocks()
   {
      new Expectations(2)
      {{
         mock.setSomething(withAny(1));
         mock.save();
      }};

      new Expectations(3)
      {{
         mock.prepare();
         mock.setSomethingElse(withNotEqual("")); notStrict();
         mock.editABunchMoreStuff(); notStrict();
         mock.save();
      }};

      for (int i = 0; i < 2; i++) {
         mock.setSomething(123 + i);
         mock.save();
      }

      for (int i = 0; i < 3; i++) {
         mock.prepare();
         mock.editABunchMoreStuff();

         if (i != 1) {
            mock.setSomethingElse("" + i);
         }

         mock.save();
      }
   }
}
