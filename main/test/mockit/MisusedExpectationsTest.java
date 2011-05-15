/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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

   @Test
   public void recordDuplicateInvocationWithNoArguments()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
         mock.value(); result = 2; // second recording overrides the first
      }};

      assertEquals(2, mock.value());
      assertEquals(2, mock.value());
   }

   @Test
   public void recordDuplicateInvocationWithArgumentMatcher()
   {
      new NonStrictExpectations()
      {{
         mock.setValue(anyInt); result = new UnknownError();
         mock.setValue(anyInt); // overrides the previous one
      }};

      mock.setValue(3);
   }

   @Test
   public void recordDuplicateInvocationInSeparateNonStrictExpectationBlocks()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
      }};

      new NonStrictExpectations()
      {{
         mock.value(); result = 2; // overrides the previous expectation
      }};

      assertEquals(2, mock.value());
   }

   @Test(expected = AssertionError.class)
   public void recordSameInvocationInNonStrictExpectationBlockThenInStrictOne()
   {
      new NonStrictExpectations()
      {{
         mock.value(); result = 1;
      }};

      new Expectations()
      {{
         // This expectation can never be replayed, so it will cause the test to fail:
         mock.value(); result = 2;
      }};

      assertEquals(1, mock.value());
      assertEquals(1, mock.value());
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
      boolean doIt() { return true; }
   }

   public static class SubFoo extends Foo {}

   @Test
   public void recordDuplicateInvocationOnTwoDynamicMocksOfDifferentTypesButSharedBaseClass()
   {
      final Foo f1 = new Foo();
      final SubFoo f2 = new SubFoo();

      new NonStrictExpectations(f1, f2)
      {{
         // These two expectations should be recorded with "onInstance(fn)" instead:
         f1.doIt(); result = true;
         f2.doIt(); result = false;
      }};

      assertFalse(f1.doIt());
      assertFalse(f2.doIt());
   }

   @BeforeClass
   public static void recordExpectationsInStaticContext()
   {
      try {
         new NonStrictExpectations()
         {
            Blah blah;

            {
               blah.doSomething(anyBoolean); result = "invalid";
            }
         };
      }
      catch (IllegalStateException ignored) {
         // OK
      }
   }

   @SuppressWarnings({"UnusedParameters"})
   static class BlahBlah
   {
      int value() { return 0; }
      void setValue(int value) {}
      String doSomething(boolean b) { return ""; }
      void doSomethingElse(Object o) {}
   }

   @SuppressWarnings({"StaticFieldReferencedViaSubclass"})
   @Test
   public void accessSpecialFieldsInExpectationBlockThroughClassQualifierInsteadOfDirectly(final BlahBlah mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.value(); Expectations.result = 123; Expectations.minTimes = 1; Expectations.maxTimes = 2;

            mock.doSomething(Expectations.anyBoolean); NonStrictExpectations.result = "test";
            NonStrictExpectations.times = 1;

            mock.setValue(withNotEqual(0));
         }
      };

      assertEquals(123, mock.value());
      assertEquals("test", mock.doSomething(true));
      mock.setValue(1);
   }

   boolean verified;

   @SuppressWarnings({"StaticFieldReferencedViaSubclass"})
   @Test
   public void accessSpecialFieldsInVerificationBlockThroughClassQualifierInsteadOfDirectly(final BlahBlah mock)
   {
      assertNull(mock.doSomething(true));
      mock.setValue(1);

      new Verifications()
      {
         {
            mock.doSomething(false); Verifications.times = 0;

            mock.doSomethingElse(Expectations.any); FullVerificationsInOrder.maxTimes = 0;

            mock.setValue(FullVerifications.anyInt); VerificationsInOrder.forEachInvocation = new Object()
            {
               void setValue(int v) { assertTrue(v > 0); verified = true; }
            };
         }
      };

      assertTrue(verified);
   }
}
