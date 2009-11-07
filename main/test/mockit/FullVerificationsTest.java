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
public final class FullVerificationsTest extends JMockitTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(char value) {}
      public boolean editABunchMoreStuff() { return false; }
      public void notifyBeforeSave() {}
      public void prepare() {}
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

      new FullVerifications()
      {{
         mock.prepare(); repeatsAtLeast(1);
         mock.editABunchMoreStuff();
         mock.notifyBeforeSave(); repeatsAtMost(1);
         mock.setSomething(anyInt); repeats(0, 2);
         mock.setSomethingElse(anyChar);
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyAllInvocationsWithSomeOfThemRecorded()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); returns(true);
         mock.setSomething(45);
      }};

      exerciseCodeUnderTest();

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(anyInt);
         mock.setSomethingElse(anyChar);
         mock.editABunchMoreStuff();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyAllInvocationsWithThoseRecordedAsExpectedToOccurVerifiedImplicitly()
   {
      new NonStrictExpectations()
      {{
         mock.setSomething(45); repeats(1);
         mock.editABunchMoreStuff(); returns(true); repeatsAtLeast(1);
      }};

      exerciseCodeUnderTest();

      // TODO: review documentation (API and tutorial) once this redundant verification is fixed
      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(123);
         mock.setSomethingElse(anyChar);
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithOneMissing()
   {
      exerciseCodeUnderTest();

      new FullVerifications()
      {{
         mock.prepare();
         mock.notifyBeforeSave();
         mock.setSomething(anyChar);
         mock.setSomethingElse(anyChar);
         mock.save();
      }};
   }

   @Test
   public void verifyUnrecordedInvocationThatWasExpectedToNotHappen()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomething(45);

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(anyInt); repeats(2);
         mock.notifyBeforeSave(); repeats(0);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyUnrecordedInvocationThatShouldNotHappenButDoes()
   {
      mock.setSomething(1);
      mock.notifyBeforeSave();

      new FullVerifications()
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

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(anyInt);
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyRecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen()
   {
      new NonStrictExpectations() {{ mock.save(); }};

      mock.prepare();
      mock.setSomething(123);

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(anyInt);
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test
   public void verifyUnrecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(anyInt);
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyUnrecordedInvocationThatShouldHappenButDoesnt()
   {
      mock.setSomething(1);

      new FullVerifications()
      {{
         mock.notifyBeforeSave();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyRecordedInvocationThatShouldHappenButDoesnt()
   {
      new NonStrictExpectations()
      {{
         mock.notifyBeforeSave();
      }};

      mock.setSomething(1);

      new FullVerifications()
      {{
         mock.notifyBeforeSave();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithExtraVerification()
   {
      mock.prepare();
      mock.setSomething(123);

      new FullVerifications()
      {{
         mock.prepare();
         mock.setSomething(123);
         mock.notifyBeforeSave();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithInvocationCountOneLessThanActual()
   {
      mock.setSomething(123);
      mock.setSomething(45);

      new FullVerifications()
      {{
         mock.setSomething(anyInt); repeats(1);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithInvocationCountTwoLessThanActual()
   {
      mock.setSomething(123);
      mock.setSomething(45);
      mock.setSomething(0);

      new FullVerifications()
      {{
         mock.setSomething(anyInt); repeats(1);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithInvocationCountMoreThanActual()
   {
      mock.setSomethingElse('f');

      new FullVerifications()
      {{
         mock.setSomethingElse(anyChar); repeats(3);
      }};
   }

   @Test
   public void verifyAllInvocationsInIteratingBlock()
   {
      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();

      new FullVerifications(2)
      {{
         mock.setSomething(anyInt);
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySingleInvocationInBlockWithWrongNumberOfIterations()
   {
      mock.setSomething(123);

      new FullVerifications(3)
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

      new FullVerifications(2)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(anyInt); repeatsAtLeast(2);
         mock.setSomethingElse('a');
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.notifyBeforeSave();
         mock.save(); repeats(1);
      }};
   }
}
