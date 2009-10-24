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

public class BaseJUnit4DecoratorTest
{
   public static final class RealClass0
   {
      public String getValue() { return "REAL0"; }
   }

   @MockClass(realClass = RealClass0.class)
   public static final class MockClass0
   {
      @Mock
      public String getValue() { return "TEST0"; }
   }

   @BeforeClass
   public static void beforeClass()
   {
      assertEquals("REAL0", new RealClass0().getValue());
      Mockit.setUpMocks(MockClass0.class);
      assertEquals("TEST0", new RealClass0().getValue());
   }

   public static final class RealClass1
   {
      public String getValue() { return "REAL1"; }
   }

   @MockClass(realClass = RealClass1.class)
   public static final class MockClass1
   {
      @Mock
      public String getValue() { return "TEST1"; }
   }

   @Before
   public final void beforeBase()
   {
      assertEquals("REAL1", new RealClass1().getValue());
      Mockit.setUpMocks(MockClass1.class);
      assertEquals("TEST1", new RealClass1().getValue());
   }

   @After
   public final void afterBase()
   {
      assertEquals("TEST0", new RealClass0().getValue());
      assertEquals("TEST1", new RealClass1().getValue());
      Mockit.tearDownMocks(RealClass1.class);
   }

   @AfterClass
   public static void afterClass()
   {
      assertEquals("REAL0", new RealClass0().getValue());
      assertEquals("REAL1", new RealClass1().getValue());
   }
}
