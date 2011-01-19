/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class SecondTest
{
   @Test
   public void anotherSlowTest1() throws Exception
   {
      Thread.sleep(500);
      A.doSomething();
   }

   @Test
   public void anotherSlowTest2() throws Exception
   {
      Thread.sleep(1500);
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

      public void doSomethingElse()
      {
         counter++;
      }
   }
}
