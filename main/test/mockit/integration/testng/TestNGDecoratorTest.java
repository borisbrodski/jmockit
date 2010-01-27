/*
 * JMockit
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.integration.testng;

import javax.security.auth.login.*;

import static org.testng.Assert.*;
import org.testng.annotations.*;

import mockit.*;

public final class TestNGDecoratorTest extends BaseTestNGDecoratorTest
{
   public static class RealClass2
   {
      public String getValue() { return "REAL2"; }
   }

   @MockClass(realClass = RealClass2.class)
   public static class MockClass2
   {
      @Mock
      public String getValue() { return "TEST2"; }
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
   
   @AfterMethod
   public void afterTest()
   {
      assertEquals("REAL2", new RealClass2().getValue());
   }

   @SuppressWarnings({"ClassMayBeInterface"})
   public static class Temp {}
   private static final Temp temp = new Temp();

   @DataProvider(name = "data")
   public Object[][] createData1()
   {
      return new Object[][] { {temp} };
   }

   @Test(dataProvider = "data")
   public void checkNoMockingOfParametersWhenUsingDataProvider(Temp t)
   {
      assertSame(temp, t);
   }

   @Test
   public void checkMockingOfParameterWhenNotUsingDataProvider(Temp mock)
   {
      assertNotSame(temp, mock);
   }

   @Test(expectedExceptions = AssertionError.class)
   public void mockMethodWithViolatedInvocationCountConstraint() throws Exception
   {
      Mockit.setUpMock(LoginContext.class, new Object()
      {
         @Mock(minInvocations = 1)
         void $init(String name) { assert "test".equals(name); }

         @Mock(invocations = 1)
         void login() {}
      });

      LoginContext context = new LoginContext("test");
      context.login();
      context.login();
   }
}
