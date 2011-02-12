/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class TestedClassWithConstructorDITest
{
   public static final class TestedClass
   {
      private final Dependency dependency;

      public TestedClass(Dependency dependency) { this.dependency = dependency; }

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
   @Injectable Dependency mock;

   @Test
   public void exerciseTestedObjectWithDependencyInjectedThroughConstructor()
   {
      new Expectations()
      {
         {
            mock.doSomething(); result = 23;
         }
      };

      assertTrue(tested.doSomeOperation());
   }
}
