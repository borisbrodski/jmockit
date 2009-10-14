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

import junit.framework.*;

import mockit.*;

public class BaseJUnit3DecoratorTest extends TestCase
{
   public static class RealClass1
   {
      public String getValue() { return "REAL1"; }
   }

   @MockClass(realClass = RealClass1.class)
   public static class MockClass1
   {
      @Mock
      public String getValue() { return "TEST1"; }
   }

   @Override
   protected void setUp()
   {
      assertEquals("REAL1", new RealClass1().getValue());
      Mockit.setUpMocks(MockClass1.class);
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @Override
   protected void tearDown()
   {
      assertEquals("TEST1", new RealClass1().getValue());
      Mockit.tearDownMocks(RealClass1.class);
   }
}
