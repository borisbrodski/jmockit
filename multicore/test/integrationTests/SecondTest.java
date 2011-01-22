/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import junit.framework.*;

public final class SecondTest extends TestCase
{
   @Override
   public void setUp()
   {
      assert getClass().getClassLoader() != ClassLoader.getSystemClassLoader();
   }

   public void testAnotherSlowTest1() throws Exception
   {
      Thread.sleep(400);
      A.doSomething();
   }

   public void testAnotherSlowTest2() throws Exception
   {
      Thread.sleep(600);
      new B().doSomethingElse();
      assert B.counter == 2;
   }

   public static class A
   {
      public static void doSomething()
      {
         new B().doSomethingElse();
         assert B.counter == 1 : "counter = " + B.counter;
      }
   }

   public static class B
   {
      static int counter;
      public void doSomethingElse() { counter++; }
   }
}
