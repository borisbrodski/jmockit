/*
 * JMockit Samples
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
package org.mockitousage;

import java.util.*;

import org.junit.*;

import mockit.*;

import org.hamcrest.beans.*;
import static org.junit.Assert.*;

// Even though JMockit tests may take more lines of code than the equivalent Mockito tests (mostly because of code
// formatting style), the number of uses of each mocking API (considering methods/constructors called, fields accessed,
// and annotations applied) is usually less in the JMockit ones, and never more.
public final class JavadocExamples_JMockit_Test
{
   @Mocked List<String> mockedList;

   @Test // Uses of JMockit API: 1
   public void verifyBehavior(final MockedClass mock)
   {
      // Mock is used (replay phase):
      mock.doSomething("one", true);
      mock.someMethod("test");

      // Invocations to mock are verified (verify phase):
      new Verifications()
      {
         {
            mock.doSomething("one", true);
            mock.someMethod("test");
         }
      };
   }

   @Test // Uses of JMockit API: 3
   public void stubInvocations(final MockedClass mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.getItem(0); result = "first";
            mock.getItem(1); result = new RuntimeException();
         }
      };

      assertEquals("first", mock.getItem(0));

      try {
         mock.getItem(1);
      }
      catch (RuntimeException e) {
         // OK
      }

      assertNull(mock.getItem(999));
   }

   @Test // Uses of JMockit API: 3
   public void stubAndVerifyInvocation()
   {
      new NonStrictExpectations()
      {
         {
            mockedList.get(0); result = "first";
         }
      };

      assertEquals("first", mockedList.get(0));

      // Note that verifying a stubbed invocation isn't "just redundant" if the test cares that the
      // invocation occurs at least once. If this is the case, then it's not safe to expect the test
      // to break without an explicit verification, because the method under test may never call the
      // stubbed one, and that would be a bug that the test should detect.
      new Verifications()
      {
         {
            mockedList.get(0);
         }
      };
   }

   @Test // Uses of JMockit API: 3
   public void stubAndVerifyInvocationWithoutRepeatingItInExpectationAndVerificationBlocks()
   {
      new NonStrictExpectations()
      {
         {
            // Notice that this can't be done in Mockito, which requires the repetition of
            // "mockedList.get(0);" in the verification phase.
            mockedList.get(0); result = "first"; times = 1;

            // Notice also that if the expectation above was strict (ie, recorded inside an
            // "Expectations" block) then the "times = 1;" constraint could be removed.
         }
      };

      assertEquals("first", mockedList.get(0));
   }

   @Test // Uses of JMockit API: 7
   public void useArgumentMatchers()
   {
      new NonStrictExpectations()
      {
         {
            mockedList.get(anyInt); result = "element";
            mockedList.contains(with(new HasProperty<String>("abc"))); result = true;
         }
      };

      assertEquals("element", mockedList.get(999));

      new Verifications()
      {
         {
            mockedList.get(anyInt);
         }
      };
   }

   @Test // Uses of JMockit API: 8
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
            mockedList.add("once"); // minTimes == 1 is the default
            mockedList.add("once"); times = 1;

            // Verifies exact number of invocations:
            mockedList.add("twice"); times = 2;
            mockedList.add("three times"); times = 3;

            // Verifies no invocations occurred:
            mockedList.add("never happened"); times = 0;

            // Verifies min/max number of invocations:
            mockedList.add("three times"); minTimes = 1;
            mockedList.add("three times"); minTimes = 2;
            mockedList.add("three times"); maxTimes = 5;
         }
      };
   }

   @Test(expected = RuntimeException.class) // Uses of JMockit API: 2
   public void stubVoidMethodsWithExceptions()
   {
      new NonStrictExpectations()
      {
         {
            // void/non-void methods are handled the same way, with a consistent API:
            mockedList.clear(); result = new RuntimeException();
         }
      };

      mockedList.clear();
   }

   @Test // Uses of JMockit API: 1
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

   @Test // Uses of JMockit API: 2
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
            mockedList.add("two"); times = 0;
         }
      };
   }

   @Test(expected = AssertionError.class) // Uses of JMockit API: 1
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

   @Test // Uses of JMockit API: 1
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

   @Test(expected = AssertionError.class) // Uses of JMockit API: 1
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

   @Test // Uses of JMockit API: 1
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

   @Test(expected = AssertionError.class) // Uses of JMockit API: 1
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

   @Test(expected = AssertionError.class) // Uses of JMockit API: 1
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

   @Test // Uses of JMockit API: 4
   public void consecutiveCallsWithStrictExpectations(final Iterator<String> mock)
   {
      new Expectations()
      {
         {
            mock.next(); result = new IllegalStateException(); result = "foo"; minTimes = 1;
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

   @Test // Uses of JMockit API: 3
   public void consecutiveCallsWithNonStrictExpectations(final Iterator<String> mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.next(); result = new IllegalStateException(); result = "foo";
         }
      };

      verifyConsecutiveCallsWithRegularAssertions(mock);
   }

   @Test // Uses of JMockit API: 4
   public void stubbingWithCallbacksUsingDelegate(final MockedClass mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.someMethod(anyString);
            result = new Delegate()
            {
               String delegate(String s)
               {
                  return "called with arguments: " + s;
               }
            };
         }
      };

      assertEquals("called with arguments: foo", mock.someMethod("foo"));
   }

   @Test // Uses of JMockit API: 2
   public void stubbingWithCallbacksUsingMockUp()
   {
      final MockedClass mock = new MockedClass();

      new MockUp<MockedClass>()
      {
         MockedClass it;

         @Mock
         String someMethod(String s)
         {
            assertSame(mock, it);
            return "called with arguments: " + s;
         }
      };

      assertEquals("called with arguments: foo", mock.someMethod("foo"));
   }

   // Equivalent to "spyingOnRealObjects", but real implementations execute only on replay.
   @Test // Uses of JMockit API: 5
   public void dynamicPartialMocking()
   {
      final MockedClass dynamicMock = new MockedClass();

      // Optionally, you can record some invocations:
      new NonStrictExpectations(dynamicMock)
      {
         {
            dynamicMock.getSomeValue(); result = 100;

            // When recording invocations the real implementations are never executed, so this call
            // would never throw an exception:
            dynamicMock.getItem(1); result = "an item";
         }
      };

      // Using the mock calls real methods, except for calls which match recorded expectations:
      dynamicMock.doSomething("one", true);
      dynamicMock.doSomething("two", false);

      assertEquals("one", dynamicMock.getItem(0));
      assertEquals("an item", dynamicMock.getItem(1));
      assertEquals(100, dynamicMock.getSomeValue());

      // Optionally, you can verify invocations to the dynamically mocked types/objects:
      new Verifications()
      {
         {
            // When verifying invocations, real implementations are never executed:
            dynamicMock.doSomething("one", true);
            dynamicMock.doSomething("two", anyBoolean);
         }
      };
   }

   @Test // Uses of JMockit API: 5
   public void validatingSingleArgumentForEachInvocation(final MockedClass mock)
   {
      mock.doSomething(new Person("John"));
      mock.doSomething(new Person("Jane"));

      new Verifications()
      {
         {
            mock.doSomething((Person) any); times = 2;
            forEachInvocation = new Object()
            {
               String[] expectedNames = {"John", "Jane"};

               void validatePerson(Invocation i, Person p)
               {
                  assertEquals(expectedNames[i.getInvocationIndex()], p.getName());
               }
            };
         }
      };
   }

   @Test // Uses of JMockit API: 4
   public void validatingMultipleArgumentsForEachInvocation(final MockedClass mock)
   {
      mock.doSomething("test", true);

      new Verifications()
      {
         {
            mock.doSomething(anyString, anyBoolean);
            forEachInvocation = new Object()
            {
               void validateArguments(String s, boolean b)
               {
                  assertEquals("test", s);
                  assertTrue(b);
               }
            };
         }
      };
   }

   @Test // Uses of JMockit API: 4
   public void chainingMethodCallsWithCascading(@Cascading final MockedClass mock)
   {
      new NonStrictExpectations()
      {
         {
            mock.getPerson().getName(); result = "deep";
         }
      };

      assertEquals("deep", mock.getPerson().getName());

      new Verifications()
      {
         {
            // Not likely to be useful often, but such verifications do work:
            mock.getPerson();
            mock.getPerson().getName();
         }
      };
   }
}
