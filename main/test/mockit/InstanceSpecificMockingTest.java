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

import java.nio.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

public final class InstanceSpecificMockingTest
{
   static class Collaborator
   {
      protected final int value;

      Collaborator() { value = -1; }
      Collaborator(int value) { this.value = value; }

      int getValue() { return value; }

      @SuppressWarnings({"UnusedDeclaration"})
      final boolean simpleOperation(int a, String b, Date c) { return true; }

      @SuppressWarnings({"UnusedDeclaration"})
      static void doSomething(boolean b, String s) { throw new IllegalStateException(); }
   }

   final Collaborator previousInstance = new Collaborator();
   @Injectable Collaborator mock;

   @Test
   public void exerciseInjectedInstanceDuringReplayOnly()
   {
      assertThatPreviouslyCreatedInstanceIsNotMocked();

      assertEquals(0, mock.value);
      assertEquals(0, mock.getValue());
      assertFalse(mock.simpleOperation(1, "test", null));

      assertThatNewlyCreatedInstanceIsNotMocked();
   }

   private void assertThatPreviouslyCreatedInstanceIsNotMocked()
   {
      assertEquals(-1, previousInstance.value);
      assertEquals(-1, previousInstance.getValue());
      assertTrue(previousInstance.simpleOperation(1, "test", null));
   }

   private void assertThatNewlyCreatedInstanceIsNotMocked()
   {
      Collaborator newInstance = new Collaborator();
      assertEquals(-1, newInstance.value);
      assertEquals(-1, newInstance.getValue());
      assertTrue(newInstance.simpleOperation(1, "test", null));
   }

   @Test
   public void mockSingleInstanceStrictly()
   {
      assertThatPreviouslyCreatedInstanceIsNotMocked();

      new Expectations()
      {
         { mock.getValue(); result = 123; }
      };

      assertEquals(123, mock.getValue());
      assertThatNewlyCreatedInstanceIsNotMocked();
   }

   @Test
   public void mockSpecificInstanceWithNonStrictExpectations()
   {
      new NonStrictExpectations()
      {{
         mock.simpleOperation(1, "", null); result = false;
         mock.getValue(); result = 123;
      }};

      assertFalse(mock.simpleOperation(1, "", null));
      assertEquals(123, mock.getValue());
      assertThatPreviouslyCreatedInstanceIsNotMocked();
      assertThatNewlyCreatedInstanceIsNotMocked();

      try {
         Collaborator.doSomething(false, null);
         fail();
      }
      catch (IllegalStateException ignore) {}

      new Verifications()
      {{
         mock.getValue(); times = 1;
      }};
   }

   @Test
   public void useASecondMockInstanceOfTheSameType(@Injectable final Collaborator mock2)
   {
      assertThatPreviouslyCreatedInstanceIsNotMocked();

      new NonStrictExpectations()
      {{
         mock2.getValue(); result = 2;
         mock.getValue(); returns(1, 3);
      }};

      assertEquals(1, mock.getValue());
      assertEquals(2, mock2.getValue());
      assertEquals(3, mock.getValue());
      assertEquals(2, mock2.getValue());
      assertEquals(3, mock.getValue());

      new FullVerifications()
      {{
         mock.getValue(); times = 3;
         mock2.getValue(); times = 2;
      }};
      
      assertThatPreviouslyCreatedInstanceIsNotMocked();
      assertThatNewlyCreatedInstanceIsNotMocked();
   }

   @Injectable Hashtable<?, ?> dummy;

   @Test
   public void allowInjectableMockOfInterfaceType(@Injectable final Runnable mock)
   {
      new NonStrictExpectations() {{ mock.run(); minTimes = 1; }};
      
      mock.run();
      mock.run();

      new Verifications() {{ mock.run(); maxTimes = 2; }};
   }

   @Test
   public void allowInjectableMockOfAnnotationType(@Injectable final RunWith mock)
   {
      new Expectations() {{ mock.value(); result = BlockJUnit4ClassRunner.class; }};
      
      assertSame(BlockJUnit4ClassRunner.class, mock.value());
   }

   @Test // TODO: enums with abstract methods (like TimeUnit) are not fully mocked; add tests to MockedEnumsTest
   public void allowInjectableMockOfEnumType(@Injectable final Thread.State mock)
   {
      new Expectations() {{ mock.name(); result = "Test"; }};
      
      assertEquals("Test", mock.name());
   }

   @Test
   public void mockByteBuffer(@Injectable final ByteBuffer buf)
   {
      new NonStrictExpectations()
      {
         {
            buf.isDirect(); result = true;
            
            // Calling "getBytes()" here indirectly creates a new ByteBuffer, requiring use of @Injectable.
            // TODO: use of withEqual is needed because arrays don't get special treatment by default; however, Hamcrest
            // matchers (used internally by withEqual and other such methods) do so by default; add special treatment.
            buf.put(withEqual("Test".getBytes())); times = 1;
         }
      };

      assertTrue(buf.isDirect());
      buf.put("Test".getBytes());
   }
}