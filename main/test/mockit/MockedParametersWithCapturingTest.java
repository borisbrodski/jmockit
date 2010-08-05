/*
 * JMockit Expectations & Verifications
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
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class MockedParametersWithCapturingTest
{
   public interface Service
   {
      int doSomething();
      void doSomethingElse(int i);
   }

   static final class ServiceImpl implements Service
   {
      final String str;

      ServiceImpl() { str = ""; }
      ServiceImpl(String str) { this.str = str; }

      public int doSomething() { return 1; }
      public void doSomethingElse(int i) { throw new IllegalMonitorStateException(); }

      private boolean privateMethod() { return true; }
      static boolean staticMethod() { return true; }
   }

   public static final class TestedUnit
   {
      final Service service1 = new ServiceImpl("test");

      final Service service2 = new Service()
      {
         public int doSomething() { return 2; }
         public void doSomethingElse(int i) {}
      };

      public int businessOperation()
      {
         return service1.doSomething() + service2.doSomething();
      }
   }

   @Test
   public void captureInstancesWithoutMockingAnyMethods(
      @Mocked(capture = 2, methods = "") Service service)
   {
      assertEquals(0, service.doSomething());

      TestedUnit unit = new TestedUnit();
      assertEquals(3, unit.businessOperation());

      assertTrue(ServiceImpl.staticMethod());

      ServiceImpl service1 = (ServiceImpl) unit.service1;
      assertTrue(service1.privateMethod());
      assertEquals("test", service1.str);
   }

   @Test(expected = IllegalMonitorStateException.class)
   public void mockOnlySpecifiedMethod(@Capturing @Mocked("doSomething") final Service service)
   {
      new Expectations()
      {
         {
            service.doSomething(); returns(3, 4);
         }
      };

      assertEquals(7, new TestedUnit().businessOperation());

      // Not mocked, so it will throw an exception:
      new ServiceImpl().doSomethingElse(1);
   }

   @Test
   public void mockAllMethodsExceptTheOneSpecified(
      @Mocked(methods = "doSomething()", inverse = true) @Capturing final Service service)
   {
      ServiceImpl impl = new ServiceImpl();
      impl.doSomethingElse(5);
      impl.doSomethingElse(-1);

      assertEquals(1, impl.doSomething());
      assertEquals(1, new ServiceImpl().doSomething());
      assertEquals(3, new TestedUnit().businessOperation());

      new Verifications()
      {
         {
            service.doSomethingElse(anyInt); times = 2;
         }
      };

      new VerificationsInOrder()
      {
         {
            service.doSomethingElse(5);
            service.doSomethingElse(-1);
         }
      };
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

   @Test
   public void useSpecifiedConstructorToCallSuperUsingLocalMockField()
   {
      new Expectations()
      {
         @Mocked(methods = "()", constructorArgsMethod = "valueForSuper") DerivedClass mock;

         {
            assertEquals("mock", mock.str);
         }

         @SuppressWarnings({"UnusedDeclaration"})
         Object[] valueForSuper(String s)
         {
            return new Object[] {"mock"};
         }
      };

      assertEquals("mock", new DerivedClass().str);
   }

   @Test
   public void useSpecifiedConstructorToCallSuper(
      @Mocked(methods = {"()"}, constructorArgsMethod = "valueForSuper") DerivedClass mock)
   {
      assertEquals("mock", mock.str);
      assertEquals("mock", new DerivedClass().str);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   Object[] valueForSuper(String s)
   {
      return new Object[] {"mock"};
   }

   @Test
   public void captureDerivedClass(@Capturing BaseClass service)
   {
      assertNull(new DerivedClass("test").str);
   }

   @Test
   public void captureDerivedClassButWithoutMockingAnything(
      @Mocked(methods = "", capture = 1) BaseClass mock)
   {
      assertEquals("", mock.str);
      assertEquals("test", new DerivedClass("test").str);
   }
}