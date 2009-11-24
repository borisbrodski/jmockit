/*
 * JMockit Core
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
package integrationTests;

import org.junit.*;

import mockit.*;

import static mockit.Mockit.*;
import static org.junit.Assert.*;

public final class SubclassTest
{
   private static boolean superClassConstructorCalled;
   private static boolean subClassConstructorCalled;
   private static boolean mockConstructorCalled;

   public static class SuperClass
   {
      final String name;

      public SuperClass(int x, String name)
      {
         this.name = name + x;
         superClassConstructorCalled = true;
      }
   }

   public static class SubClass extends SuperClass
   {
      public SubClass(String name)
      {
         super(name.length(), name);
         subClassConstructorCalled = true;
      }
   }

   @Before
   public void setUp()
   {
      superClassConstructorCalled = false;
      subClassConstructorCalled = false;
      mockConstructorCalled = false;
   }

   @Test
   public void captureSubclassThroughClassfileTransformer()
   {
      new NonStrictExpectations()
      {
         @Capturing
         SuperClass captured;
      };

      new SubClass("capture");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void captureSubclassThroughRedefinitionOfPreviouslyLoadedClasses()
   {
      new SubClass("");
      assertTrue(superClassConstructorCalled);
      assertTrue(subClassConstructorCalled);
      superClassConstructorCalled = false;
      subClassConstructorCalled = false;

      new NonStrictExpectations()
      {
         @Capturing
         SuperClass captured;
      };

      new SubClass("capture");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void mockSubclassUsingRedefineMethods()
   {
      redefineMethods(SubClass.class, SubClassMock.class);

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
      assertTrue(mockConstructorCalled);
   }

   public static final class SubClassMock
   {
      public SubClassMock(String name)
      {
         assertNotNull(name);
         mockConstructorCalled = true;
      }
   }

   @Test
   public void mockSubclassUsingSetUpMocks()
   {
      setUpMocks(SubClassMockWithAnnotations.class);

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
      assertTrue(mockConstructorCalled);
   }

   @MockClass(realClass = SubClass.class)
   public static final class SubClassMockWithAnnotations
   {
      @Mock(invocations = 1)
      public SubClassMockWithAnnotations(String name)
      {
         assertNotNull(name);
         mockConstructorCalled = true;
      }
   }

   @Test
   public void mockSubclassUsingExpectationsWithFirstSuperConstructor()
   {
      new Expectations()
      {
         final SubClass mock = null;

         {
            new SubClass("test");
         }
      };

      new SubClass("test");

      assertFalse(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test
   public void mockSubclassUsingExpectationsWithSuperConstructorChosenByItsNumber()
   {
      new Expectations()
      {
         @Mocked("(String): 1")
         final SubClass mock = null;

         {
            new SubClass("test");
         }
      };

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }

   @Test(expected = IllegalArgumentException.class)
   public void mockSubclassUsingExpectationsWithNonExistentSuperConstructorNumber()
   {
      new Expectations()
      {
         @Mocked("(String): 2")
         final SubClass mock = null;
      };
   }

   @Test
   public void mockSubclassUsingExpectationsWithSuperConstructorChosenByItsSignature()
   {
      new Expectations()
      {
         @Mocked("(String): (int, java.lang.String)")
         final SubClass mock = null;

         {
            new SubClass("test");
         }
      };

      new SubClass("test");

      assertTrue(superClassConstructorCalled);
      assertFalse(subClassConstructorCalled);
   }
}
