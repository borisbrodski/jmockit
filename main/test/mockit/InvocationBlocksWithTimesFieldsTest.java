/*
 * JMockit Expectations & Verifications
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

import java.util.*;

import org.junit.*;

public final class InvocationBlocksWithTimesFieldsTest
{
   private final CodeUnderTest codeUnderTest = new CodeUnderTest();

   static class CodeUnderTest
   {
      private final Collaborator dependency = new Collaborator();

      void doSomething()
      {
         dependency.provideSomeService();
      }

      void doSomethingElse()
      {
         dependency.simpleOperation(1, "b", null);
      }
   }

   static class Collaborator
   {
      Collaborator() {}

      @SuppressWarnings({"UnusedDeclaration"})
      Collaborator(int value) {}

      void provideSomeService() {}

      @SuppressWarnings({"UnusedDeclaration"})
      final void simpleOperation(int a, String b, Date c) {}
   }

   // Tests with recorded strict expectations /////////////////////////////////////////////////////////////////////////

   @Test
   public void strict_expectTwiceByUsingInvocationCount()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = AssertionError.class)
   public void strict_expectTwiceByUsingInvocationCountButReplayOnlyOnce()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void strict_expectAtLeastOnceAndReplayTwice()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = AssertionError.class)
   public void strict_expectAtLeastTwiceButReplayOnceWithSingleExpectation()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void strict_expectAtLeastTwiceButReplayOnceWithTwoConsecutiveExpectations()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void strict_minTimesAndMaxTimesOutOfOrder()
   {
      new Expectations()
      {
         @Mocked Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2; minTimes = 1;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void strict_expectAtMostTwiceAndReplayOnce()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test(expected = AssertionError.class)
   public void strict_expectAtMostOnceButReplayTwice()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void strict_expectAtMostZero()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void strict_expectAtMostZeroButReplayOnce()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService();
            maxTimes = 0;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void strict_maxTimesDoesNotOverwriteMinTimes()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test
   public void strict_expectSameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
      final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.simpleOperation(1, "", null); minTimes = 1; maxTimes = 2;
            mock.simpleOperation(2, "", null);
         }
      };

      mock.simpleOperation(1, "", null);
      mock.simpleOperation(2, "", null);
   }

   @Test
   public void strict_expectTwoOrThreeTimes()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void strict_expectZeroOrMoreTimesAndReplayTwice()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void strict_expectZeroOrMoreTimesAndReplayNone()
   {
      new Expectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomethingElse();
   }

   // Tests with recorded non-strict expectations /////////////////////////////////////////////////////////////////////

   @Test
   public void nonStrict_expectTwiceByUsingInvocationCount()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void nonStrict_expectTwiceByUsingInvocationCountButReplayOnlyOnce()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); times = 2;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void nonStrict_expectAtLeastOnceAndReplayTwice()
   {
      new Expectations()
      {
         @NonStrict Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void nonStrict_minTimesAndMaxTimesOutOfOrder()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2; minTimes = 1;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @Test
   public void nonStrict_expectAtMostTwiceAndReplayOnce()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void nonStrict_expectAtMostOnceButReplayTwice()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); maxTimes = 1;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
   }

   @Test
   public void nonStrict_expectAtMostZero()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void nonStrict_expectAtMostZeroButReplayOnce()
   {
      new Expectations()
      {
         @NonStrict Collaborator mock;

         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void nonStrict_maxTimesDoesNotOverwriteMinTimes()
   {
      new NonStrictExpectations()
      {
         @Mocked Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };

      codeUnderTest.doSomething();
   }

   @Test
   public void nonStrict_expectSameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
      @NonStrict final Collaborator mock)
   {
      new Expectations()
      {
         {
            mock.simpleOperation(1, "", null); minTimes = 1; maxTimes = 2;
            mock.simpleOperation(2, "", null);
         }
      };

      mock.simpleOperation(2, "", null);
      mock.simpleOperation(1, "", null);
   }

   @Test
   public void nonStrict_expectTwoOrThreeTimes()
   {
      new Expectations()
      {
         @NonStrict @Mocked Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
   }

   @Test
   public void nonStrict_expectZeroOrMoreTimesAndReplayTwice()
   {
      final Collaborator collaborator = new Collaborator();

      new NonStrictExpectations(collaborator)
      {
         {
            collaborator.simpleOperation(1, "b", null);
            collaborator.provideSomeService(); minTimes = 0; maxTimes = -1;
         }
      };

      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
   }

   @Test
   public void nonStrict_expectZeroOrMoreTimesAndReplayNone()
   {
      new NonStrictExpectations()
      {
         Collaborator mock;

         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };

      codeUnderTest.doSomethingElse();
   }

   // Tests with ordered verifications ////////////////////////////////////////////////////////////////////////////////

   @Test
   public void ordered_verifyTwiceByUsingInvocationCount(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void ordered_verifyTwiceByUsingInvocationCountButReplayOnlyOnce(final Collaborator mock)
   {
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      new FullVerificationsInOrder()
      {
         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); times = 2;
         }
      };
   }

   @Test
   public void ordered_verifyAtLeastOnceAndReplayTwice(@NonStrict final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); minTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test
   public void ordered_minTimesAndMaxTimesOutOfOrder(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); maxTimes = 2; minTimes = 1;
         }
      };
   }

   @Test
   public void ordered_verifyAtMostTwiceAndReplayOnce(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new FullVerificationsInOrder()
      {
         {
            mock.provideSomeService(); maxTimes = 2;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void ordered_verifyAtMostOnceButReplayTwice(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); maxTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test
   public void ordered_verifyAtMostZero(final Collaborator mock)
   {
      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void ordered_verifyAtMostZeroButReplayOnce(@NonStrict final Collaborator mock)
   {
      codeUnderTest.doSomething();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void ordered_maxTimesDoesNotOverwriteMinTimes(@Mocked final Collaborator mock)
   {
      codeUnderTest.doSomething();

      new FullVerificationsInOrder()
      {
         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };
   }

   @Test
   public void ordered_verifySameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
      @NonStrict final Collaborator mock)
   {
      mock.simpleOperation(1, "", null);
      mock.simpleOperation(2, "", null);

      new VerificationsInOrder()
      {
         {
            mock.simpleOperation(1, "", null); minTimes = 1; maxTimes = 2;
            mock.simpleOperation(2, "", null);
         }
      };
   }

   @Test
   public void ordered_verifyTwoOrThreeTimes(@NonStrict @Mocked final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new VerificationsInOrder()
      {
         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @SuppressWarnings({"UnusedDeclaration"})
   @Test
   public void ordered_verifyZeroOrMoreTimesAndReplayTwice(Collaborator mock)
   {
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();

      final Collaborator collaborator = new Collaborator();

      new VerificationsInOrder()
      {
         {
            collaborator.simpleOperation(1, "b", null);
            collaborator.provideSomeService(); minTimes = 0; maxTimes = -1;
         }
      };
   }

   @Test
   public void ordered_verifyZeroOrMoreTimesAndReplayNone(final Collaborator mock)
   {
      codeUnderTest.doSomethingElse();

      new FullVerificationsInOrder()
      {
         {
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   // Tests with unordered verifications //////////////////////////////////////////////////////////////////////////////

   @Test
   public void unordered_verifyTwiceByUsingInvocationCount(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      new Verifications()
      {
         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unordered_verifyTwiceByUsingInvocationCountButReplayOnlyOnce(final Collaborator mock)
   {
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      new FullVerifications()
      {
         {
            mock.provideSomeService(); times = 2;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test
   public void unordered_verifyAtLeastOnceAndReplayTwice(@NonStrict final Collaborator mock)
   {
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();

      new Verifications()
      {
         {
            mock.provideSomeService(); minTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test
   public void unordered_minTimesAndMaxTimesOutOfOrder(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
      codeUnderTest.doSomething();

      new Verifications()
      {
         {
            mock.provideSomeService(); maxTimes = 2; minTimes = 1;
         }
      };
   }

   @Test
   public void unordered_verifyAtMostTwiceAndReplayOnce(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();

      new FullVerifications()
      {
         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); maxTimes = 2;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unordered_verifyAtMostOnceButReplayTwice(final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      new Verifications()
      {
         {
            mock.provideSomeService(); maxTimes = 1;
            mock.simpleOperation(1, "b", null);
         }
      };
   }

   @Test
   public void unordered_verifyAtMostZero(final Collaborator mock)
   {
      new Verifications()
      {
         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unordered_verifyAtMostZeroButReplayOnce(@NonStrict final Collaborator mock)
   {
      codeUnderTest.doSomething();

      new Verifications()
      {
         {
            mock.provideSomeService(); maxTimes = 0;
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unordered_maxTimesDoesNotOverwriteMinTimes(@Mocked final Collaborator mock)
   {
      codeUnderTest.doSomething();

      new FullVerifications()
      {
         {
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };
   }

   @Test
   public void unordered_verifySameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
      @NonStrict final Collaborator mock)
   {
      mock.simpleOperation(2, "", null);
      mock.simpleOperation(1, "", null);

      new Verifications()
      {
         {
            mock.simpleOperation(1, "", null); minTimes = 1; maxTimes = 2;
            mock.simpleOperation(2, "", null);
         }
      };
   }

   @Test
   public void unordered_verifyTwoOrThreeTimes(@NonStrict @Mocked final Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      new FullVerifications()
      {
         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); minTimes = 2; maxTimes = 3;
         }
      };
   }

   @SuppressWarnings({"UnusedDeclaration"})
   @Test
   public void unordered_verifyZeroOrMoreTimesAndReplayTwice(Collaborator mock)
   {
      codeUnderTest.doSomething();
      codeUnderTest.doSomethingElse();
      codeUnderTest.doSomething();

      final Collaborator collaborator = new Collaborator();

      new Verifications()
      {
         {
            collaborator.simpleOperation(1, "b", null);
            collaborator.provideSomeService(); minTimes = 0; maxTimes = -1;
         }
      };
   }

   @Test
   public void unordered_verifyZeroOrMoreTimesAndReplayNone(final Collaborator mock)
   {
      codeUnderTest.doSomethingElse();

      new Verifications()
      {
         {
            mock.simpleOperation(1, "b", null);
            mock.provideSomeService(); minTimes = 0; maxTimes = -1;
         }
      };
   }
}