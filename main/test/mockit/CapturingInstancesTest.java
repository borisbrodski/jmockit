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

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CapturingInstancesTest
{
   public interface Service
   {
      int doSomething();
   }

   static final class ServiceImpl implements Service
   {
      public int doSomething() { return 1; }
   }

   public static final class TestedUnit
   {
      private final Service service1 = new ServiceImpl();
      private final Service service2 = new Service()
      {
         public int doSomething() { return 2; }
      };

      Observable observable;

      public int businessOperation(final boolean b)
      {
         new Callable()
         {
            public Object call() { throw new IllegalStateException(); }
         }.call();

         observable = new Observable()
         {
            {
               if (b) {
                  throw new IllegalArgumentException();
               }
            }
         };

         return service1.doSomething() + service2.doSomething();
      }
   }

   @Mocked(capture = 10)
   private Service service;

   @Test
   public void captureServiceInstancesCreatedByTestedConstructor()
   {
      Service initialMockService = service;

      new TestedUnit();

      assertNotSame(initialMockService, service);
      assertFalse(service instanceof ServiceImpl);
   }

   @Test
   public void captureAllInternallyCreatedInstances(@Mocked(capture = 1) final Callable<?> callable)
      throws Exception
   {
      new NonStrictExpectations()
      {
         @Mocked(capture = 1) Observable observable;

         {
            service.doSomething(); returns(3, 4);
         }
      };

      TestedUnit unit = new TestedUnit();
      int result = unit.businessOperation(true);

      assertNotNull(unit.observable);
      assertEquals(7, result);

      new Verifications()
      {
         {
            callable.call();
         }
      };
   }
}