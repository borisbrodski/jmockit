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
import static org.junit.Assert.*;

import mockit.*;

public final class JUnit4DecoratorTest extends BaseJUnit4DecoratorTest
{
   public static final class RealClass2
   {
      public String getValue() { return "REAL2"; }
   }

   @MockClass(realClass = RealClass2.class)
   public static final class MockClass2
   {
      @Mock
      public String getValue() { return "TEST2"; }
   }

   @Test
   public void useClassScopedMockDefinedByBaseClass()
   {
      assertEquals("TEST0", new RealClass0().getValue());
   }

   @Test
   public void setUpAndUseSomeMocks()
   {
      assertEquals("TEST1", new RealClass1().getValue());
      assertEquals("REAL2", new RealClass2().getValue());

      Mockit.setUpMocks(MockClass2.class);

      assertEquals("TEST2", new RealClass2().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @Test
   public void setUpAndUseMocksAgain()
   {
      assertEquals("TEST1", new RealClass1().getValue());
      assertEquals("REAL2", new RealClass2().getValue());

      Mockit.setUpMocks(MockClass2.class);

      assertEquals("TEST2", new RealClass2().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @After
   public void afterTest()
   {
      assertEquals("REAL2", new RealClass2().getValue());
   }

   @Test
   public void classMockedInSecondTestClassMustNotBeMockedForThisTestClass()
   {
      assertEquals("REAL3", new SecondJUnit4DecoratorTest.RealClass3().getValue());
   }
}
