/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class TestedClassWithConstructorDI2Test
{
   public static final class TestedClass
   {
      private final Dependency dependency1;
      private final Dependency dependency2;

      public TestedClass(Dependency dependency1, Runnable r, Dependency dependency2)
      {
         this.dependency1 = dependency1;
         this.dependency2 = dependency2;
         r.run();
      }

      public int doSomeOperation()
      {
         return dependency1.doSomething() + dependency2.doSomething();
      }
   }

   static class Dependency
   {
      int doSomething() { return -1; }
   }

   @Tested TestedClass tested;
   @Injectable Runnable task;
   @Injectable Dependency mock1;
   @Injectable Dependency mock2;

   @Test
   public void exerciseTestedObjectWithDependenciesOfSameTypeInjectedThroughConstructor()
   {
      new Expectations() {{
         mock1.doSomething(); result = 23;
         mock2.doSomething(); result = 5;
      }};

      assertEquals(28, tested.doSomeOperation());
   }
}
