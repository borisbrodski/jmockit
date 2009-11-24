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
package mockit;

import org.junit.*;

import mockit.CapturingImplementationsTest.*;

import static org.junit.Assert.*;

@Capturing(baseType = ServiceToBeStubbedOut.class)
public final class CapturingImplementationsTest
{
   interface ServiceToBeStubbedOut
   {
      int doSomething();
   }

   static final class ServiceLocator
   {
      @SuppressWarnings({"UnusedDeclaration"})
      static <S> S getInstance(Class<S> serviceInterface)
      {
         ServiceToBeStubbedOut service = new ServiceToBeStubbedOut()
         {
            public int doSomething()
            {
               return 10;
            }
         };

         //noinspection unchecked
         return (S) service;
      }
   }

   @Test
   public void captureImplementationLoadedByServiceLocator()
   {
      ServiceToBeStubbedOut service = ServiceLocator.getInstance(ServiceToBeStubbedOut.class);
      assertEquals(0, service.doSomething());
   }

   interface Service
   {
      int doSomething();
   }

   static final class ServiceImpl implements Service
   {
      public int doSomething() { return 1; }
   }

   @Test
   public void captureImplementationUsingMockField()
   {
      Service service = new ServiceImpl();

      new Expectations()
      {
         @Capturing Service mock;

         {
            mock.doSomething();
            returns(2, 3);
         }
      };

      assertEquals(2, service.doSomething());
      assertEquals(3, new ServiceImpl().doSomething());
   }

   @Test
   public void captureImplementationUsingMockParameter(@Capturing final Service mock)
   {
      ServiceImpl service = new ServiceImpl();

      new Expectations()
      {
         {
            mock.doSomething();
            returns(3, 2);
         }
      };

      assertEquals(3, service.doSomething());
      assertEquals(2, new ServiceImpl().doSomething());
   }

   interface AnotherService
   {
      int doSomethingElse();
   }

   static final class ServiceImpl2 implements AnotherService
   {
      public int doSomethingElse() { return 2; }
   }

   static final class ServiceImpl3 implements AnotherService
   {
      public int doSomethingElse() { return 3; }
   }

   @Test
   public void captureImplementationByClassName(
      @Capturing(classNames = ".+ServiceImpl2") AnotherService mock)
   {
      assertEquals(0, new ServiceImpl2().doSomethingElse());
      assertEquals(3, new ServiceImpl3().doSomethingElse());
   }

   @Test
   public void captureImplementationByClassNameForAlreadyLoadedClass()
   {
      AnotherService service = new ServiceImpl2();
      assertEquals(2, service.doSomethingElse());

      new Expectations()
      {
         @NonStrict @Capturing(classNames = ".+ServiceImpl3", inverse = true)
         AnotherService mock;

         {
            mock.doSomethingElse(); returns(3);
         }
      };

      assertEquals(3, service.doSomethingElse());
   }

   @Test
   public void captureInstancesForMockFieldWithoutUsingTheCapturingAnnotation()
   {
      new Expectations()
      {
         @Mocked(capture = 2) AnotherService mock;

         {
            mock.doSomethingElse(); returns(5, 6);
         }
      };

      assertEquals(5, new ServiceImpl2().doSomethingElse());
      assertEquals(6, new ServiceImpl3().doSomethingElse());
   }

   public abstract static class AbstractService
   {
      protected abstract boolean doSomething();
   }

   static final class DefaultServiceImpl extends AbstractService
   {
      @Override
      protected boolean doSomething() { return true; }
   }

   @Test
   public void captureImplementationOfAbstractClass(@Capturing AbstractService mock)
   {
      assertFalse(new DefaultServiceImpl().doSomething());

      assertFalse(new AbstractService()
      {
         @Override
         protected boolean doSomething()
         {
            throw new RuntimeException();
         }
      }.doSomething());
   }

   @Test
   public void captureGeneratedMockSubclass(@Capturing final AbstractService mock1)
   {
      new NonStrictExpectations()
      {
         AbstractService mock2;

         {
            onInstance(mock1).doSomething(); returns(true);
            onInstance(mock2).doSomething(); returns(true);
            endRecording();

            assertTrue(mock1.doSomething());
            assertTrue(mock2.doSomething());
         }
      };

      assertFalse(new DefaultServiceImpl().doSomething());
   }
}
