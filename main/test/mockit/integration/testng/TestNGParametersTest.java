/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.testng;

import static org.testng.Assert.*;
import org.testng.annotations.*;

public final class TestNGParametersTest
{
   String p1;
   String p2;

   @Parameters({"p1", "p2"})
   public TestNGParametersTest(String p1, String p2)
   {
      this.p1 = p1;
      this.p2 = p2;
   }

   @BeforeClass @Parameters("p1")
   void setUpClass(String param)
   {
      assertEquals(param, "Abc");
      assertEquals(param, p1);
   }

   @BeforeTest @Parameters("param1")
   void setUpTest(@Optional String param)
   {
      assertNull(param);
   }

   @BeforeMethod @Parameters("param2")
   void setUp(String param)
   {
      assertEquals(param, "XYZ5");
   }

   @Test @Parameters({"first", "second"})
   public void testSomething(@Optional("abc") String a, @Optional("123") String b)
   {
      assertEquals(a, "abc");
      assertEquals(b, "123");
      assertEquals(p1, "Abc");
      assertEquals(p2, "XPTO");
   }

   @AfterMethod @Parameters("param3")
   void tearDown(@Optional String param)
   {
      assertNull(param);
   }

   @AfterTest @Parameters("param1")
   void tearDownTest(@Optional("value") String param)
   {
      assertEquals(param, "value");
   }

   @AfterClass @Parameters("p2")
   void tearDownClass(String param)
   {
      assertEquals(param, "XPTO");
      assertEquals(param, p2);
   }
}
