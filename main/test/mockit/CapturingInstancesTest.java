/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CapturingInstancesTest
{
   public interface Service { int doSomething(); }
   static final class ServiceImpl implements Service { public int doSomething() { return 1; } }

   public static final class TestedUnit
   {
      private final Service service1 = new ServiceImpl();
      private final Service service2 = new Service() { public int doSomething() { return 2; } };
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

   @Mocked(capture = 2) Service service;

   @Test
   public void captureServiceInstancesCreatedByTestedConstructor()
   {
      Service initialMockService = service;

      new TestedUnit();

      assertNotSame(initialMockService, service);
      assertFalse(service instanceof ServiceImpl);
   }

   @Test
   public void captureAllInternallyCreatedInstances(@Mocked(capture = 1) final Callable<?> callable) throws Exception
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

      new Verifications() {{ callable.call(); }};
   }
}