/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

import mockit.*;

public final class ThirdTest
{
   @Test
   public void quickTestUsingTheJMockitExpectationsAPI(@Injectable final A mockedA)
   {
      mockedA.doSomething();

      new Verifications()
      {
         {
            mockedA.doSomething();
         }
      };
   }

   public static class A
   {
      public void doSomething()
      {
         throw new RuntimeException("should not execute");
      }
   }
}
