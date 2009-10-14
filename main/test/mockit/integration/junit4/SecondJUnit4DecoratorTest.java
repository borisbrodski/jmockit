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
package mockit.integration.junit4;

import org.junit.*;

import mockit.*;

@UsingMocksAndStubs(SecondJUnit4DecoratorTest.MockClass3.class)
public final class SecondJUnit4DecoratorTest extends JMockitTest
{
   public static final class RealClass3
   {
      public String getValue() { return "REAL3"; }
   }

   @MockClass(realClass = RealClass3.class)
   public static final class MockClass3
   {
      @Mock
      public String getValue() { return "TEST3"; }
   }

   @Test
   public void realClassesMockedInPreviousTestClassMustNoLongerBeMocked()
   {
      assertEquals("REAL0", new BaseJUnit4DecoratorTest.RealClass0().getValue());
      assertEquals("REAL1", new BaseJUnit4DecoratorTest.RealClass1().getValue());
      assertEquals("REAL2", new JUnit4DecoratorTest.RealClass2().getValue());
   }

   @Test
   public void useClassScopedMockDefinedForThisClass()
   {
      assertEquals("TEST3", new RealClass3().getValue());
   }
}