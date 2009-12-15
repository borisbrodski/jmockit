/*
 * JMockit Expectations & Verifications
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

import static org.junit.Assert.*;

public final class InvocationsWithCustomMessagesTest
{
   static final String message = "custom message";
   @Mocked Collaborator mock;

   static class Collaborator
   {
      private int value;

      Collaborator() {}

      private static String doInternal() { return "123"; }

      void provideSomeService() {}

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToSpecifyErrorMessageWithNoExpectationRecorded()
   {
      new Expectations()
      {
         {
            $ = "error";
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void replayWithUnexpectedInvocation()
   {
      new Expectations()
      {
         {
            mock.getValue(); $ = message;
         }
      };

      try {
         mock.provideSomeService();
      }
      catch (AssertionError e) {
         if (e.getMessage().startsWith(message)) {
            return;
         }
      }

      throw new IllegalStateException("should not get here");
   }

   @Test(expected = AssertionError.class)
   public void replayStrictExpectationOnceMoreThanExpected()
   {
      new Expectations()
      {
         {
            Collaborator.doInternal();
            mock.provideSomeService(); minTimes = 1; $ = message; maxTimes = 2;
         }
      };

      Collaborator.doInternal();

      try {
         Collaborator.doInternal();
      }
      catch (AssertionError e) {
         if (e.getMessage().startsWith(message)) {
            return;
         }
      }

      throw new IllegalStateException("should not get here");
   }

   @Test
   public void replayNonStrictExpectationOnceMoreThanExpected()
   {
      new NonStrictExpectations()
      {
         {
            new Collaborator(); times = 1; $ = message;
         }
      };

      new Collaborator();

      try {
         new Collaborator();
      }
      catch (AssertionError e) {
         assertTrue(e.getMessage().startsWith(message));
         return;
      }

      fail();
   }

   @Test(expected = AssertionError.class)
   public void replayWithMissingNonStrictExpectation()
   {
      new NonStrictExpectations()
      {
         {
            new Collaborator(); minTimes = 2; maxTimes = 3; $ = message;
         }
      };

      new Collaborator();

      // The AssertionError will occur after the test method finished execution, so there is no way
      // to directly test it (except by commenting out the "expected" attribute above).
   }

   @Test(expected = AssertionError.class)
   public void replayWithMissingExpectedInvocation()
   {
      new Expectations()
      {
         {
            mock.setValue(123); $ = message;
         }
      };

      // The AssertionError will occur after the test method finished execution, so there is no way
      // to directly test it (except by commenting out the "expected" attribute above).
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToSpecifyErrorMessageWithNoExpectationVerified()
   {
      new Verifications()
      {
         {
            $ = "error";
         }
      };
   }

   @Test
   public void verifyInvocationThatDidNotOccur()
   {
      try {
         new Verifications()
         {
            {
               mock.provideSomeService(); times = 1;
               $ = message;
            }
         };
      }
      catch (AssertionError e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }

   @Test
   public void verifyMissingInvocationAfterOneThatDidOccur()
   {
      Collaborator.doInternal();
      Collaborator.doInternal();

      try {
         new VerificationsInOrder()
         {
            {
               Collaborator.doInternal();
               mock.provideSomeService(); minTimes = 1; $ = message; maxTimes = 2;
            }
         };
      }
      catch (AssertionError e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }

   @Test
   public void verifyInvocationThatOccurredOnceMoreThanExpected()
   {
      new Collaborator();
      new Collaborator();

      try {
         new FullVerifications()
         {
            {
               new Collaborator();
               times = 1;
               $ = message;
            }
         };
      }
      catch (AssertionError e) {
         assertTrue(e.getMessage().startsWith(message));
      }
   }

   @Test
   public void verifyUnexpectedInvocation()
   {
      mock.provideSomeService();
      mock.setValue(123);

      try {
         new FullVerificationsInOrder()
         {
            {
               mock.provideSomeService();
               mock.setValue(anyInt); times = 0; $ = message;
            }
         };
      }
      catch (AssertionError e) {
         if (!e.getMessage().startsWith(message)) {
            throw new IllegalStateException("Missing custom message prefix", e);
         }
      }
   }
}