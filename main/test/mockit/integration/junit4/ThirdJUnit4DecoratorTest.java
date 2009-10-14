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

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.*;

@RunWith(JMockit.class)
@UsingMocksAndStubs(ThirdJUnit4DecoratorTest.MockClass4.class)
public final class ThirdJUnit4DecoratorTest extends BaseJUnit4DecoratorTest
{
   public static final class RealClass4
   {
      public String getValue() { return "REAL4"; }
   }

   @MockClass(realClass = RealClass4.class)
   public static final class MockClass4
   {
      @Mock
      public String getValue() { return "TEST4"; }
   }

   @Test
   public void realClassesMockedInBaseClassMustStillBeMockedHere()
   {
      assertEquals("TEST0", new RealClass0().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @Test
   public void realClassesMockedInOtherTestClassesMustNotBeMockedHere()
   {
      assertEquals("REAL2", new JUnit4DecoratorTest.RealClass2().getValue());
      assertEquals("REAL3", new SecondJUnit4DecoratorTest.RealClass3().getValue());
   }

   @Test
   public void useClassScopedMockDefinedForThisClass()
   {
      assertEquals("TEST4", new RealClass4().getValue());
   }
}