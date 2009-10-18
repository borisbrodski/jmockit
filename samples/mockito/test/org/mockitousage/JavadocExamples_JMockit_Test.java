/*
 * JMockit Samples
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
package org.mockitousage;

import java.util.*;

import org.hamcrest.beans.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public class JavadocExamples_JMockit_Test
{
   @Mocked private List<String> mockedList;

   @Test
   public void verifyBehavior()
   {
      mockedList.add("one");
      mockedList.clear();

      new Verifications()
      {
         {
            mockedList.add("one");
            mockedList.clear();
         }
      };
   }

   @Test
   public void stubInvocations()
   {
      new NonStrictExpectations()
      {
         {
            mockedList.get(0); returns("first");
            mockedList.get(1); throwsException(new RuntimeException());
         }
      };

      assertEquals("first", mockedList.get(0));

      try {
         mockedList.get(1);
      }
      catch (RuntimeException e) {
         // OK
      }

      assertNull(mockedList.get(999));
   }

   @Test
   public void stubAndVerifyInvocation()
   {
      new NonStrictExpectations()
      {
         {
            mockedList.get(0); returns("first");
         }
      };

      assertEquals("first", mockedList.get(0));

      new Verifications()
      {
         {
            mockedList.get(0);
         }
      };
   }

   @Test
   public void stubAndVerifyInvocationWithoutRepeatingItInExpectationAndVerificationBlocks()
   {
      new NonStrictExpectations()
      {
         {
            // Notice that this can't be done in Mockito, which requires the repetition of
            // "mockedList.get(0);" in the verification phase.
            mockedList.get(0); returns("first"); repeats(1);

            // Notice also that if the expectation above was strict (ie, recorded inside an
            // "Expectations" block) then the call "repeats(1);" could be removed.
         }
      };

      assertEquals("first", mockedList.get(0));
   }

   @Test
   public void useArgumentMatchers()
   {
      new NonStrictExpectations()
      {
         {
            mockedList.get(withAny(0)); returns("element");
            mockedList.contains(with(new HasProperty<String>("abc"))); returns(true);
         }
      };

      assertEquals("element", mockedList.get(999));

      new Verifications()
      {
         {
            mockedList.get(withAny(1));
         }
      };
   }

   @Test
   public void verifyNumberOfInvocations()
   {
      // Using mock:
      mockedList.add("once");

      mockedList.add("twice");
      mockedList.add("twice");

      mockedList.add("three times");
      mockedList.add("three times");
      mockedList.add("three times");

      new Verifications()
      {
         {
            // Following two verifications work exactly the same:
            mockedList.add("once"); // repeatsAtLeast(1) is the default
            mockedList.add("once"); repeats(1);

            // Verifies exact number of invocations:
            mockedList.add("twice"); repeats(2);
            mockedList.add("three times"); repeats(3);

            // Verifies no invocations occurred:
            mockedList.add("never happened"); repeats(0);

            // Verifies min/max number of invocations:
            mockedList.add("three times"); repeatsAtLeast(1);
            mockedList.add("three times"); repeatsAtLeast(2);
            mockedList.add("three times"); repeatsAtMost(5);
         }
      };
   }

   @Test(expected = RuntimeException.class)
   public void stubVoidMethodsWithExceptions()
   {
      new NonStrictExpectations()
      {
         {
            // void/non-void methods are handled the same way:
            mockedList.clear(); throwsException(new RuntimeException());
         }
      };

      mockedList.clear();
   }

   @Test
   public void verifyInOrder(final List<String> firstMock, final List<String> secondMock)
   {
      // Using mocks:
      firstMock.add("was called first");
      secondMock.add("was called second");

      new VerificationsInOrder()
      {
         {
            // Verifies that firstMock was called before secondMock:
            firstMock.add("was called first");
            secondMock.add("was called second");
         }
      };
   }

   @SuppressWarnings({"UnusedDeclaration"})
   @Test
   public void verifyThatInvocationsNeverHappened(List<String> mockTwo, List<String> mockThree)
   {
      // Using mocks - only mockedList is invoked:
      mockedList.add("one");

      // Verify that the two other mocks were never invoked.
      new FullVerifications()
      {
         {
            // Ordinary verification:
            mockedList.add("one");

            // Verify that method was never called on a mock:
            mockedList.add("two"); repeats(0);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyThatInvocationsNeverHappenedWhenTheyDid(List<String> mockTwo)
   {
      mockedList.add("one");
      mockTwo.size();

      new FullVerifications()
      {
         {
            mockedList.add("one");
         }
      };
   }

   @Test
   public void verifyAllInvocations()
   {
      mockedList.add("one");
      mockedList.add("two");

      // Verify all invocations to mockedList.
      new FullVerifications()
      {
         {
            // Verifies first invocation:
            mockedList.add("one");

            // Verifies second (and last) invocation:
            mockedList.add("two");
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWhenMoreOfThemHappen()
   {
      mockedList.add("one");
      mockedList.add("two");
      mockedList.size();

      // Verify all invocations to mockedList.
      new FullVerifications()
      {
         {
            mockedList.add("one");
            mockedList.add("two");
         }
      };
   }

   @Test
   public void verifyAllInvocationsInOrder()
   {
      mockedList.add("one");
      mockedList.size();
      mockedList.add("two");

      new FullVerificationsInOrder()
      {
         {
            mockedList.add("one");
            mockedList.size();
            mockedList.add("two");
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsInOrderWhenMoreOfThemHappen()
   {
      mockedList.add("one");
      mockedList.add("two");
      mockedList.size();

      new FullVerificationsInOrder()
      {
         {
            mockedList.add("one");
            mockedList.add("two");
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsInOrderWithOutOfOrderVerifications()
   {
      mockedList.add("one");
      mockedList.add("two");

      new FullVerificationsInOrder()
      {
         {
            mockedList.add("two");
            mockedList.add("one");
         }
      };
   }

   @Test
   public void consecutiveCallsWithStrictExpectations(final Iterator<String> mock)
   {
      new Expectations()
      {
         {
            mock.next(); throwsException(new IllegalStateException()); returns("foo");
            repeatsAtLeast(1);
         }
      };

      verifyConsecutiveCallsWithRegularAssertions(mock);
   }

   private void verifyConsecutiveCallsWithRegularAssertions(Iterator<String> mock)
   {
      // First call: throws exception.
      try {
         mock.next();
         fail();
      }
      catch (IllegalStateException e) {
         // OK
      }

      // Second call: prints "foo".
      assertEquals("foo", mock.next());

      // Any consecutive call: prints "foo" as well.
      assertEquals("foo", mock.next());
   }

   @Test
   public void consecutiveCallsWithNonStrictExpectations(final Iterator<String> mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.next(); throwsException(new IllegalStateException()); returns("foo");
         }
      };

      verifyConsecutiveCallsWithRegularAssertions(mock);
   }

   @Test
   public void stubbingWithCallbacks()
   {
      TestedClass mock = new TestedClass();

      Mockit.setUpMock(TestedClass.class, new Object()
      {
         TestedClass it;

         @Mock
         public String addItem(String s)
         {
            System.out.println("Real instance: " + it);
            return "called with: " + s;
         }
      });

      assertEquals("called with: foo", mock.addItem("foo"));
   }

   static final class TestedClass
   {
      private final List<String> items = new LinkedList<String>();

      public String addItem(String item) { items.add(item); return ""; }
      public int getItemCount() { return items.size(); }
      public Object getItem(int index) { return items.get(index); }
   }

   @Test // essentially equivalent to "spyingOnRealObjects", with some differences in behavior
   public void dynamicPartialMocking()
   {
      final TestedClass partialMock = new TestedClass();

      // Optionally, you can record some invocations:
      new NonStrictExpectations(partialMock)
      {
         {
            partialMock.getItemCount(); returns(100);

            // When recording invocations, real methods are never called, so this would not throw an
            // IndexOutOfBoundsException, but it would prevent the real "getItem" method from being
            // executed in the replay phase:
            // partialMock.getItem(1); returns("an item");
         }
      };

      // Using the mock calls real methods, except those with recorded invocations:
      partialMock.addItem("one");
      partialMock.addItem("two");

      assertEquals("one", partialMock.getItem(0));
      assertEquals(100, partialMock.getItemCount());

      // Optionally, you can verify the actual execution of recorded invocations:
      new Verifications()
      {
         {
            // This works, but adding a call to "repeats(1);" when recording the invocation would
            // have been simpler:
            partialMock.getItemCount();

            // Since no invocations were recorded for the "addItem" method, it was not mocked during
            // the replay phase. Therefore the following call will NOT verify anything; instead, it
            // will execute the real method.
            // If a test really needs to verify the execution of such a method, it can be done by
            // recording a strict invocation, or by specifying the minimum or exact invocation count
            // on the recording of a non-strict invocation; but then the real method would not be
            // executed in the replay phase.
            partialMock.addItem("three");
         }
      };

      // From the above, we can see that invocations to REAL (unmocked) methods cannot be explicitly
      // verified with JMockit. It is doubtful that such a thing would be useful in real-world
      // tests, since the point of not mocking a method is to allow it to be exercised normally as
      // part of the code under test.
   }
}
