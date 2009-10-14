/*
 * JMockit
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
package mockit.integration.junit3;

import mockit.*;

public final class JUnit3DecoratorTest extends BaseJUnit3DecoratorTest
{
   public static class RealClass2
   {
      public String getValue()
      {
         return "REAL2";
      }
   }

   @MockClass(realClass = RealClass2.class)
   public static class MockClass2
   {
      @Mock
      public String getValue()
      {
         return "TEST2";
      }
   }

   public void testSetUpAndUseSomeMocks()
   {
      Mockit.setUpMocks(MockClass2.class);

      assertEquals("TEST2", new RealClass2().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   public void testMockParameter(RealClass2 mock)
   {
      assertEquals("TEST1", new RealClass1().getValue());
      assertNotNull(mock);
      assertNull(mock.getValue());
   }

   // To verify that static methods are not considered as tests.
   public static void testSomething(Object arg1, String arg2)
   {
      System.out.println("arg1=" + arg1 + " arg2=" + arg2);
   }

   @Override
   protected void tearDown()
   {
      assertEquals("REAL2", new RealClass2().getValue());
      super.tearDown();
   }
}
