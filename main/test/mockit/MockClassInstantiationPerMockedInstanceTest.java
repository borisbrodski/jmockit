/*
 * JMockit Annotations
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

import static mockit.Instantiation.*;
import static mockit.Mockit.*;
import mockit.integration.junit4.*;

@UsingMocksAndStubs(MockClassInstantiationPerMockedInstanceTest.MockClass1.class)
public final class MockClassInstantiationPerMockedInstanceTest extends JMockitTest
{
   static final class RealClass1
   {
      static void doSomething()
      {
         throw new RuntimeException();
      }

      int performComputation(int a, boolean b)
      {
         return b ? a : -a;
      }
   }

   static final class RealClass2
   {
      static void doSomething()
      {
         throw new RuntimeException();
      }

      int performComputation(int a, boolean b)
      {
         return b ? a : -a;
      }
   }

   static final class RealClass3
   {
      static void doSomething()
      {
         throw new RuntimeException();
      }

      int performComputation(int a, boolean b)
      {
         return b ? a : -a;
      }
   }

   static final class RealClass4
   {
      static void doSomething()
      {
         throw new RuntimeException();
      }

      int performComputation(int a, boolean b)
      {
         return b ? a : -a;
      }
   }

   @MockClass(realClass = RealClass1.class, instantiation = PerMockedInstance)
   static final class MockClass1
   {
      static Object firstInstance;
      static Object secondInstance;

      MockClass1()
      {
         if (firstInstance == null) {
            firstInstance = this;
         }
         else {
            assertNull(secondInstance);
            secondInstance = this;
         }
      }

      @Mock void doSomething()
      {
         assertSame(firstInstance, this);
         assertNull(secondInstance);
      }

      @Mock int performComputation(int a, boolean b)
      {
         assertNotNull(firstInstance);
         assertNotSame(firstInstance, this);
         assertSame(secondInstance, this);
         assertTrue(a > 0);
         assertTrue(b);
         return 2;
      }
   }

   @MockClass(realClass = RealClass2.class, instantiation = PerMockedInstance)
   static final class MockClass2
   {
      static Object firstInstance;
      static Object secondInstance;

      MockClass2()
      {
         if (firstInstance == null) {
            firstInstance = this;
         }
         else {
            assertNull(secondInstance);
            secondInstance = this;
         }
      }

      @Mock void doSomething()
      {
         assertSame(firstInstance, this);
         assertNull(secondInstance);
      }

      @Mock int performComputation(int a, boolean b)
      {
         assertNotNull(firstInstance);
         assertNotSame(firstInstance, this);
         assertSame(secondInstance, this);
         assertTrue(a > 0);
         assertTrue(b);
         return 2;
      }
   }

   @MockClass(realClass = RealClass3.class, instantiation = PerMockedInstance)
   static final class MockClass3
   {
      static Object firstInstance;
      static Object secondInstance;

      MockClass3()
      {
         if (firstInstance == null) {
            firstInstance = this;
         }
         else {
            assertNull(secondInstance);
            secondInstance = this;
         }
      }

      @Mock void doSomething()
      {
         assertSame(firstInstance, this);
         assertNull(secondInstance);
      }

      @Mock int performComputation(int a, boolean b)
      {
         assertNotNull(firstInstance);
         assertNotSame(firstInstance, this);
         assertSame(secondInstance, this);
         assertTrue(a > 0);
         assertTrue(b);
         return 2;
      }
   }

   @MockClass(realClass = RealClass4.class, instantiation = PerMockedInstance)
   static final class MockClass4
   {
      static Object firstInstance;
      static Object secondInstance;

      MockClass4()
      {
         if (firstInstance == null) {
            firstInstance = this;
         }
         else {
            assertNull(secondInstance);
            secondInstance = this;
         }
      }

      @Mock void doSomething()
      {
         assertSame(firstInstance, this);
         assertNull(secondInstance);
      }

      @Mock int performComputation(int a, boolean b)
      {
         assertNotNull(firstInstance);
         assertNotSame(firstInstance, this);
         assertSame(secondInstance, this);
         assertTrue(a > 0);
         assertTrue(b);
         return 2;
      }
   }

   @BeforeClass
   public static void setUpClassLevelMocks()
   {
      setUpMocksAndStubs(MockClass2.class);
   }

   @Before
   public void setUpMethodLevelMocks()
   {
      setUpMock(RealClass3.class, MockClass3.class);
   }

   @Test
   public void mockInstancePerMockedInstanceInClassAndFixtureScopes()
   {
      assertMockClass1();
      assertMockClass2();
      assertMockClass3();
      assertEquals(1, new RealClass4().performComputation(1, true));
   }

   private void assertMockClass1()
   {
      MockClass1.firstInstance = null;
      MockClass1.secondInstance = null;
      RealClass1.doSomething();
      RealClass1 realClass = new RealClass1();
      assertEquals(2, realClass.performComputation(1, true));
      assertEquals(2, realClass.performComputation(3, true));
   }

   private void assertMockClass2()
   {
      MockClass2.firstInstance = null;
      MockClass2.secondInstance = null;
      RealClass2.doSomething();
      RealClass2 realClass = new RealClass2();
      assertEquals(2, realClass.performComputation(1, true));
      assertEquals(2, realClass.performComputation(3, true));
   }

   private void assertMockClass3()
   {
      MockClass3.firstInstance = null;
      MockClass3.secondInstance = null;
      RealClass3.doSomething();
      RealClass3 realClass = new RealClass3();
      assertEquals(2, realClass.performComputation(1, true));
      assertEquals(2, realClass.performComputation(3, true));
   }

   private void assertMockClass4()
   {
      RealClass4.doSomething();
      RealClass4 realClass = new RealClass4();
      assertEquals(2, realClass.performComputation(1, true));
      assertEquals(2, realClass.performComputation(3, true));
   }

   @Test
   public void mockInstancePerMockedInstanceInAllScopes()
   {
      setUpMocks(MockClass4.class);

      assertMockClass1();
      assertMockClass2();
      assertMockClass3();
      assertMockClass4();
   }
}