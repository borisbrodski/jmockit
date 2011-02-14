/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
         mock.prepare(); times = 1;
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
         mock.save(); times = 1;
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
         mock.setSomething(anyInt);
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
         mock.setSomethingElse(anyString);
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
