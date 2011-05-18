/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import static org.testng.Assert.*;
import org.testng.annotations.*;

import mockit.*;

public final class TestNGExpectationsTest
{
   public static final class TestedClass
   {
      private final MockedClass dependency;
      public TestedClass(MockedClass dependency) { this.dependency = dependency; }
      public boolean doSomething(int i) { return dependency.doSomething(i); }
   }

   public static class MockedClass
   {
      public String getValue() { return "REAL"; }
      public boolean doSomething(int i) { return i > 0; }
   }

   @Tested TestedClass tested;
   @Injectable MockedClass mock;

   @BeforeMethod
   void setUp()
   {
      new NonStrictExpectations() {{ mock.getValue(); result = "mocked"; }};
   }

   @AfterMethod
   void tearDown()
   {
      new Verifications() {{ mock.doSomething(anyInt); }};
   }

   @Test
   public void testSomething()
   {
      new NonStrictExpectations()
      {{
         mock.doSomething(anyInt); result = true;
      }};

      assertTrue(mock.doSomething(5));
      assertEquals("mocked", mock.getValue());
      assertTrue(tested.doSomething(-5));

      new FullVerifications()
      {
         {
            mock.doSomething(anyInt); times = 2;
            mock.getValue();
         }
      };
   }

   @Test
   public void testSomethingElse()
   {
      assertEquals("mocked", mock.getValue());
      assertFalse(tested.doSomething(41));

      new FullVerificationsInOrder()
      {
         {
            mock.getValue();
            mock.doSomething(anyInt);
         }
      };
   }
}
