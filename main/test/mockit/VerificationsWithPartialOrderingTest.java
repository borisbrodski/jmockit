/*
 * JMockit Verifications
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

@SuppressWarnings({"UnusedDeclaration"})
public final class VerificationsWithPartialOrderingTest
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
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save();
   }

   @Test
   public void verifyFirstCallOnly()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyLastCallOnly()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test
   public void verifyLastTwoCalls()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyLastCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.save();
      mock.editABunchMoreStuff();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyLastTwoCallsWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.save();
      mock.notifyBeforeSave();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWhenOutOfOrder()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.editABunchMoreStuff();
      mock.save();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyFirstCallThenOthersInAnyOrder()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
      }};

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(123);
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifySomeCallsInAnyOrderThenFirstCallWhenOutOfOrder()
   {
      mock.setSomething(123);
      mock.prepare();
      mock.editABunchMoreStuff();

      new Verifications()
      {{
         mock.setSomething(123);
      }};

      new VerificationsInOrder()
      {{
         mock.prepare(); repeats(1);
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifySomeCallsInAnyOrderThenLastCall()
   {
      exerciseCodeUnderTest();

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(123);
      }};

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.save(); repeats(1);
      }};
   }

   @Test
   public void verifyFirstAndLastCalls()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithFirstOutOfOrder()
   {
      mock.editABunchMoreStuff();
      mock.prepare();
      mock.save();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastInvocationsWithSomeInvocationsInBetweenImplicitlyVerified()
   {
      new NonStrictExpectations()
      {
         {
            mock.setSomething(anyInt); minTimes = 1;
         }
      };

      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         // unverifiedInvocations() should be called here, even if verification occurs implicitly.
         mock.setSomethingElse(anyString);
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithLastOutOfOrder()
   {
      mock.prepare();
      mock.editABunchMoreStuff();
      mock.save();
      mock.notifyBeforeSave();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test
   public void verifyFirstAndLastCallsWithOthersInBetweenInAnyOrder()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};

      new Verifications()
      {{
         mock.setSomething(123);
         mock.setSomethingElse("anotherValue");
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyFirstAndLastCallsWithOthersInBetweenInAnyOrderWhenOutOfOrder()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.editABunchMoreStuff();
      mock.save();

      new Verifications()
      {{
         mock.setSomethingElse("anotherValue");
         mock.setSomething(withAny(0));
      }};

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.notifyBeforeSave();
         mock.save();
      }};
   }

   @Test
   public void verifyConsecutiveInvocations()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomethingElse("anotherValue");
         unverifiedInvocations();
         mock.save();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyConsecutiveInvocationsWhenNotConsecutive()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomething(45);
      mock.save();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomething(45);
         unverifiedInvocations();
      }};
   }

   @Test
   public void verifyConsecutiveInvocationsInTwoSequences()
   {
      exerciseCodeUnderTest();

      new VerificationsInOrder()
      {{
         mock.prepare();
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomethingElse(withAny(""));
         unverifiedInvocations();
         mock.notifyBeforeSave();
         unverifiedInvocations();
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyConsecutiveInvocationsInTwoSequencesWhenNotConsecutive()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomething(45);
      mock.setSomethingElse("anotherValue");
      mock.notifyBeforeSave();
      mock.save();

      new VerificationsInOrder()
      {{
         unverifiedInvocations();
         mock.setSomething(123);
         mock.setSomething(45);
         unverifiedInvocations();
         mock.save();
         unverifiedInvocations();
      }};
   }
}
