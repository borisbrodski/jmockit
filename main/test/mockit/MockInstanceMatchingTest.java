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

import org.junit.*;

import static org.junit.Assert.*;

public final class MockInstanceMatchingTest
{
   static class Collaborator
   {
      private int value;

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   @Mocked Collaborator mock;

   @Test
   public void recordExpectationMatchingOnMockInstance()
   {
      new Expectations()
      {
         {
            onInstance(mock).getValue(); result = 12;
         }
      };

      assertEquals(12, mock.getValue());
   }

   @Test(expected = AssertionError.class)
   public void recordOnMockInstanceButReplayOnDifferentInstance()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations()
      {
         {
            onInstance(mock).getValue(); result = 12;
         }
      };

      assertEquals(12, collaborator.getValue());
   }

   @Test
   public void verifyExpectationMatchingOnMockInstance()
   {
      mock.setValue(12);

      new Verifications()
      {
         {
            onInstance(mock).setValue(12);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyOnMockInstanceButReplayOnDifferentInstance()
   {
      new Collaborator().setValue(12);

      new Verifications()
      {
         {
            onInstance(mock).setValue(12);
         }
      };
   }

   @Test
   public void recordExpectationsMatchingOnMultipleMockInstances(final Collaborator mock2)
   {
      new Expectations()
      {
         {
            onInstance(mock).getValue(); result = 12;
            onInstance(mock2).getValue(); result = 13;
            onInstance(mock).setValue(20);
         }
      };

      assertEquals(12, mock.getValue());
      assertEquals(13, mock2.getValue());
      mock.setValue(20);
   }

   @Test(expected = AssertionError.class)
   public void recordOnSpecificMockInstancesButReplayOnDifferentOnes(final Collaborator mock2)
   {
      new Expectations()
      {
         {
            onInstance(mock).setValue(12);
            onInstance(mock2).setValue(13);
         }
      };

      mock2.setValue(12);
      mock.setValue(13);
   }

   @Test
   public void verifyExpectationsMatchingOnMultipleMockInstances(final Collaborator mock2)
   {
      mock.setValue(12);
      mock2.setValue(13);
      mock.setValue(20);

      new VerificationsInOrder()
      {
         {
            onInstance(mock).setValue(12);
            onInstance(mock2).setValue(13);
            onInstance(mock).setValue(20);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyOnSpecificMockInstancesButReplayOnDifferentOnes(final Collaborator mock2)
   {
      mock2.setValue(12);
      mock.setValue(13);

      new FullVerifications()
      {
         {
            onInstance(mock).setValue(12);
            onInstance(mock2).setValue(13);
         }
      };
   }

   @Test(expected = NullPointerException.class)
   public void recordOnNullMockInstance()
   {
      new Expectations()
      {
         {
            Collaborator mock2 = null;
            onInstance(mock2).getValue();
         }
      };
   }

   @Test(expected = NullPointerException.class)
   public void verifyOnNullMockInstance()
   {
      new Verifications()
      {
         {
            Collaborator mock2 = null;
            onInstance(mock2).getValue();
         }
      };
   }

   @Test
   public void matchOnTwoMockInstancesWithNonStrictExpectations(final Collaborator mock2)
   {
      new NonStrictExpectations()
      {
         {
            onInstance(mock).getValue(); result = 1; times = 1;
            onInstance(mock2).getValue(); result = 2; times = 1;
         }
      };

      assertEquals(1, mock.getValue());
      assertEquals(2, mock2.getValue());
   }

//   @Test
   public void matchOnTwoMockInstancesForOtherwiseIdenticalExpectations(final Collaborator mock2)
   {
      mock.getValue();
      mock2.getValue();

      new Verifications()
      {
         {
            mock.getValue(); times = 1;
            mock2.getValue(); times = 1;
         }
      };
   }

   @Test
   public void recordExpectationMatchingOnInstanceCreatedInsideCodeUnderTest()
   {
      new Expectations()
      {
         {
            onInstance(new Collaborator()).getValue(); result = 1;
         }
      };

      assertEquals(1, new Collaborator().getValue());
   }
}
