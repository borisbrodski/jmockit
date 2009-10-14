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

import mockit.integration.junit4.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class FullVerificationsInOrderTest extends JMockitTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(char value) {}
      public int editABunchMoreStuff() { return 0; }
      public void notifyBeforeSave() {}
      public boolean prepare() { return false; }
      public void save() {}
   }

   @Mocked private Dependency mock;

   private void exerciseCodeUnderTest()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse('a');
      mock.setSomething(45);
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save();
   }

   @Test
   public void verifyAllInvocations()
   {
      exerciseCodeUnderTest();

      new FullVerificationsInOrder()
      {{
         mock.prepare(); repeatsAtLeast(1);
         mock.setSomething(withAny(0)); repeats(0, 2);
         mock.setSomethingElse(withAny(' '));
         mock.setSomething(withAny(0)); repeats(1, 2);
         mock.editABunchMoreStuff();
         mock.notifyBeforeSave(); repeatsAtMost(1);
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyAllInvocationsWithSomeOfThemRecorded()
   {
      new NonStrictExpectations()
      {
         {
            mock.prepare(); returns(true);
            mock.editABunchMoreStuff(); returns(5);
         }
      };

      exerciseCodeUnderTest();

      new FullVerificationsInOrder()
      {{
         mock.prepare(); repeatsAtLeast(1);
         mock.setSomething(withAny(0)); repeats(0, 2);
         mock.setSomethingElse(withAny(' '));
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
            mock.prepare(); returns(true);
            mock.editABunchMoreStuff(); returns(5);
         }
      };

      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse('a');
      mock.save();

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.setSomethingElse(withAny(' '));
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(123);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithSomeMissing()
   {
      exerciseCodeUnderTest();

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.setSomethingElse(withAny('0'));
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyInvocationThatNeverHappens()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(123);
         mock.notifyBeforeSave(); repeats(0);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyInvocationThatShouldNeverHappenButDoes()
   {
      mock.setSomething(1);
      mock.notifyBeforeSave();

      new FullVerificationsInOrder()
      {{
         mock.setSomething(1);
         mock.notifyBeforeSave(); repeats(0);
      }};
   }

   @Test
   public void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.save();

      new FullVerificationsInOrder()
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

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyUnrecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyIntermediateUnrecordedInvocationThatDoesNotHappenButCould()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.editABunchMoreStuff(); repeatsAtLeast(0);
         mock.setSomething(withAny(0));
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithExtraVerification()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerificationsInOrder()
      {{
         mock.prepare();
         mock.setSomething(123);
         mock.notifyBeforeSave();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithInvocationCountLessThanActual()
   {
      mock.setSomething(123);
      mock.setSomething(45);

      new FullVerificationsInOrder()
      {{
         mock.setSomething(withAny(0)); repeats(1);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithInvocationCountMoreThanActual()
   {
      mock.setSomethingElse('f');

      new FullVerificationsInOrder()
      {{
         mock.setSomethingElse(withAny('T')); repeats(3, 6);
      }};
   }

   @Test
   public void verifyAllInvocationsInIteratingBlock()
   {
      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();

      new FullVerificationsInOrder(2)
      {{
         mock.setSomething(withAny(1));
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySingleInvocationInBlockWithWrongNumberOfIterations()
   {
      mock.setSomething(123);

      new FullVerificationsInOrder(3)
      {{
         mock.setSomething(123);
      }};
   }

   @Test
   public void verifyWithArgumentMatcherAndIndividualInvocationCountsInIteratingBlock()
   {
      for (int i = 0; i < 2; i++) {
         exerciseCodeUnderTest();
      }

      new FullVerificationsInOrder(2)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(1);
         mock.setSomethingElse('a');
         mock.setSomething(withAny(0)); repeatsAtMost(1);
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.notifyBeforeSave();
         mock.save(); repeats(1);
      }};
   }
}
