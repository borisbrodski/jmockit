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
import org.junit.runner.*;

import mockit.integration.junit4.*;

@SuppressWarnings({"UnusedDeclaration"})
@RunWith(JMockit.class)
public final class VerificationsTest
{
   public static class Dependency
   {
      public Dependency() {}
      private Dependency(int i) {}

      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public void editABunchMoreStuff() {}
      public void notifyBeforeSave() {}
      public void prepare() {}
      public void save() {}

      private void privateMethod() {}
      private static void privateStaticMethod(String s, boolean b) {}
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

      new Verifications()
      {{
         mock.prepare(); repeats(1);
         mock.editABunchMoreStuff();
         mock.setSomething(45);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyUnrecordedInvocationThatNeverHappens()
   {
      mock.setSomething(123);
      mock.prepare();

      new Verifications()
      {{
         mock.editABunchMoreStuff();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyRecordedInvocationThatNeverHappens()
   {
      new NonStrictExpectations()
      {
         {
            mock.editABunchMoreStuff();
         }
      };

      mock.setSomething(123);
      mock.prepare();

      new Verifications()
      {{
         mock.editABunchMoreStuff();
      }};
   }

   @Test
   public void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.save();

      new Verifications()
      {{
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

      new Verifications()
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

      new Verifications()
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.save(); repeatsAtLeast(0);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyUnrecordedInvocationThatShouldHappenButDoesnt()
   {
      mock.setSomething(1);

      new Verifications()
      {{
         mock.notifyBeforeSave();
      }};
   }

   @Test
   public void verifyInvocationsWithInvocationCount()
   {
      mock.setSomething(3);
      mock.save();
      mock.setSomethingElse("test");
      mock.save();

      new Verifications()
      {{
         mock.save(); repeats(2);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyInvocationsWithInvocationCountLargerThanOccurred()
   {
      mock.setSomething(3);
      mock.save();
      mock.setSomethingElse("test");
      mock.save();

      new Verifications()
      {{
         mock.save(); repeats(3);
      }};
   }

   @Test
   public void verifySimpleInvocationsInIteratingBlock()
   {
      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();

      new Verifications(2)
      {{
         mock.setSomething(withAny(1));
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySimpleInvocationInBlockWithWrongNumberOfIterations()
   {
      mock.setSomething(123);

      new Verifications(3)
      {{
         mock.setSomething(123);
      }};
   }

   @Test
   public void verifyWithArgumentMatcher()
   {
      exerciseCodeUnderTest();

      new Verifications()
      {{
         mock.setSomething(withAny(0));
      }};
   }

   @Test
   public void verifyWithArgumentMatcherAndIndividualInvocationCounts()
   {
      exerciseCodeUnderTest();

      new Verifications(1)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyWithArgumentMatcherAndIndividualInvocationCountsInIteratingBlock()
   {
      for (int i = 0; i < 2; i++) {
         exerciseCodeUnderTest();
      }

      new Verifications(2)
      {{
         mock.prepare(); repeatsAtMost(1);
         mock.setSomething(withAny(0)); repeatsAtLeast(2);
         mock.editABunchMoreStuff(); repeats(0, 5);
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyInvocationsToPrivateMethodsAndConstructors()
   {
      new Dependency(9).privateMethod();
      Dependency.privateStaticMethod("test", true);

      new Verifications()
      {{
         newInstance(Dependency.class.getName(), 9);
         invoke(mock, "privateMethod");
         invoke(Dependency.class, "privateStaticMethod", "test", true);
      }};
   }
}
