/*
 * JMockit Expectations
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

import java.lang.reflect.*;

import org.junit.*;

import mockit.integration.junit4.*;

public final class MockFieldCapturingMaxInstancesTest extends JMockitTest
{
   public interface Service
   {
      int doSomething();
   }

   static final class ServiceImpl implements Service
   {
      public int doSomething() { return 1; }
   }

   @Capturing Service mock1;

   @Test
   public void mockFieldWithUnlimitedCapturing()
   {
      assertNotNull(Proxy.isProxyClass(mock2.getClass()));

      new Expectations()
      {
         {
            mock1.doSomething(); returns(1, 2, 3);
         }
      };

      Service service1 = new ServiceImpl();
      assertSame(service1, mock1);
      assertEquals(1, service1.doSomething());

      Service service2 = new Service() { public int doSomething() { return -1; } };
      assertSame(service2, mock1);
      assertEquals(2, service2.doSomething());

      Service service3 = new ServiceImpl();
      assertSame(service3, mock1);
      assertEquals(3, service3.doSomething());
   }

   static class BaseClass
   {
      final String str;

      BaseClass() { str = ""; }
      BaseClass(String str) { this.str = str; }
   }

   static class DerivedClass extends BaseClass
   {
      DerivedClass() {}
      DerivedClass(String str) { super(str); }
   }

   @Capturing(maxInstances = 1) BaseClass mock2;

   @Test
   public void mockFieldWithCapturingLimitedToOneInstance()
   {
      assertNotNull(Proxy.isProxyClass(mock2.getClass()));

      BaseClass service1 = new DerivedClass("test 1");
      assertNull(service1.str);
      assertSame(service1, mock2);

      BaseClass service2 = new BaseClass("test 2");
      assertNull(service2.str);
      assertSame(service1, mock2);
   }

   @Capturing(maxInstances = 1) BaseClass mock3;

   @Test
   public void secondMockFieldWithCapturingLimitedToOneInstance()
   {
      assertNotNull(Proxy.isProxyClass(mock2.getClass()));

      BaseClass service1 = new DerivedClass("test 1");
      assertNull(service1.str);
      assertSame(service1, mock2);

      assertNotNull(Proxy.isProxyClass(mock3.getClass()));

      BaseClass service2 = new BaseClass("test 2");
      assertNull(service2.str);
      assertSame(service1, mock2);
      assertSame(service2, mock3);

      BaseClass service3 = new DerivedClass("test 3");
      assertNull(service3.str);
      assertSame(service1, mock2);
      assertSame(service2, mock3);
   }
}