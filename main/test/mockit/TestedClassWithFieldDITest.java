/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class TestedClassWithFieldDITest
{
   public static class TestedClass
   {
      // Suppose this is injected by some DI framework or Java EE container:
      @SuppressWarnings({"UnusedDeclaration"}) private Dependency dependency;

      public boolean doSomeOperation()
      {
         return dependency.doSomething() > 0;
      }
   }

   static class Dependency
   {
      int doSomething() { return -1; }
   }

   @Tested TestedClass tested;
   @Injectable Dependency dependency;

   @Test
   public void exerciseTestedObjectWithFieldInjectedByType()
   {
      new NonStrictExpectations()
      {
         {
            dependency.doSomething(); result = 23; times = 1;
         }
      };

      assertTrue(tested.doSomeOperation());
   }

   public static final class AnotherTestedClass extends TestedClass
   {
      Runnable runnable;
      Dependency dependency3;
      Dependency dependency2;

      @Override
      public boolean doSomeOperation()
      {
         boolean b = dependency2.doSomething() > 0;
         return super.doSomeOperation() && b;
      }
   }

   @Tested AnotherTestedClass tested2;
   @Injectable Runnable mock2;
   @Injectable Dependency dependency2;

   @Test
   public void exerciseTestedSubclassObjectWithFieldsInjectedByTypeAndName()
   {
      assertSame(mock2, tested2.runnable);
      assertSame(dependency2, tested2.dependency2);
      assertNull(tested2.dependency3);
      assertFalse(tested2.doSomeOperation());

      new Verifications()
      {
         {
            mock2.run(); times = 0;
            dependency.doSomething(); times = 1;
            dependency2.doSomething();
         }
      };
   }
}
