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

@SuppressWarnings({"UnusedDeclaration"})
public final class VerificationsInOrderTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public void editABunchMoreStuff() {}
      public void notifyBeforeSave() {}
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
   public void verifySimpleInvocations()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(45);
         mock.editABunchMoreStuff();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyUnrecordedInvocationThatShouldHappenButDoesnt()
   {
      mock.setSomething(1);

      new VerificationsInOrder()
      {{
         mock.notifyBeforeSave();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyRecordedInvocationThatShouldHappenButDoesnt()
   {
      new NonStrictExpectations()
      {
         {
            mock.setSomething(1);
            mock.notifyBeforeSave();
         }
      };

      mock.setSomething(1);

      new VerificationsInOrder()
      {{
         mock.setSomething(1);
         mock.notifyBeforeSave();
      }};
   }

   @Test
   public void verifyAllInvocationsWithSomeOfThemRecorded()
   {
      new NonStrictExpectations()
      {
         {
            mock.prepare();
            mock.editABunchMoreStuff();
         }
      };

      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare(); repeatsAtLeast(1);
         mock.setSomethingElse(withAny(""));
         mock.setSomething(withAny(0)); repeats(1, 2);
         mock.editABunchMoreStuff();
         mock.notifyBeforeSave(); repeatsAtMost(1);
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyInvocationsWithOneRecordedButNotReplayed()
   {
      new NonStrictExpectations()
      {
         {
            mock.prepare();
            mock.editABunchMoreStuff();
         }
      };

      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("a");
      mock.save();

      new VerificationsInOrder()
      {{
         mock.setSomething(withAny(0));
         mock.setSomethingElse(withAny(""));
         mock.save();
      }};
   }

   @Test
   public void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.save();

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyRecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen()
   {
      new NonStrictExpectations() {{ mock.save(); }};

      mock.prepare();
      mock.setSomething(123);

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyUnrecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen()
   {
      mock.prepare();
      mock.setSomething(123);

      new VerificationsInOrder()
      {{
         mock.setSomething(withAny(0));
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyIntermediateUnrecordedInvocationThatDoesNotHappenButCould()
   {
      mock.prepare();
      mock.setSomething(123);

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.editABunchMoreStuff(); repeatsAtLeast(0);
         mock.setSomething(withAny(0));
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySimpleInvocationsWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(123);
      }};
   }

   @Test
   public void verifyRepeatingInvocation()
   {
      mock.setSomething(123);
      mock.setSomething(123);

      new VerificationsInOrder()
      {{
         mock.setSomething(123); repeats(2);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyRepeatingInvocationThatOccursOneTimeMoreThanExpected()
   {
      mock.setSomething(123);
      mock.setSomething(123);

      new VerificationsInOrder()
      {{
         mock.setSomething(123); repeatsAtMost(1);
      }};
   }

   @Test
   public void verifySimpleInvocationInIteratingBlock()
   {
      mock.setSomething(123);
      mock.setSomething(123);

      new VerificationsInOrder(2)
      {{
         mock.setSomething(123);
      }};
   }

   @Test
   public void verifyRepeatingInvocationInIteratingBlock()
   {
      mock.setSomething(123);
      mock.setSomething(123);
      mock.setSomething(123);
      mock.setSomething(123);

      new VerificationsInOrder(2)
      {{
         mock.setSomething(123); repeatsAtLeast(2);
      }};
   }

   @Test
   public void verifyRepeatingInvocationUsingMatcher()
   {
      mock.setSomething(123);
      mock.setSomething(45);

      new VerificationsInOrder()
      {{
         mock.setSomething(withAny(1)); repeats(2);
      }};
   }

   @Test
   public void verifySimpleInvocationInIteratingBlockUsingMatcher()
   {
      mock.setSomething(123);
      mock.setSomething(45);

      new VerificationsInOrder(2)
      {{
         mock.setSomething(withAny(1));
      }};
   }

   @Test
   public void verifySimpleInvocationsInIteratingBlock()
   {
      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();

      new VerificationsInOrder(2)
      {{
         mock.setSomething(withAny(1));
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySimpleInvocationInBlockWithWrongNumberOfIterations()
   {
      mock.setSomething(123);

      new VerificationsInOrder(3)
      {{
         mock.setSomething(123);
      }};
   }

   @Test
   public void verifyWithArgumentMatcher()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyWithIndividualInvocationCountsForNonConsecutiveInvocations()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare(); repeats(1);
         mock.setSomething(withAny(0)); repeats(2);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyWithArgumentMatchersWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomething(45);

      new VerificationsInOrder()
      {{
         mock.setSomething(withAny(0));
         mock.setSomething(withAny(1));
         mock.setSomethingElse(withAny(""));
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyWithArgumentMatcherAndIndividualInvocationCountWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();
      mock.setSomething(45);

      new VerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0)); repeats(2);
      }};
   }
}
