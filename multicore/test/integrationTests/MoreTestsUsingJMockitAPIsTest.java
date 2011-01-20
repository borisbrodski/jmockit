/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

import mockit.*;

public final class MoreTestsUsingJMockitAPIsTest
{
   public static class A
   {
      public void doSomething() { throw new RuntimeException("should not execute"); }
      int getValue() { return -1; }
   }

   @NonStrict A mock;

   @Test
   public void usingTheExpectationsAPI()
   {
      new Expectations()
      {
         {
            mock.getValue(); result = 123;
         }
      };

      assert mock.getValue() == 123;
      mock.doSomething();
   }

   @Test
   public void usingTheVerificationsAPI()
   {
      mock.doSomething();

      new FullVerifications()
      {
         {
            mock.doSomething(); times = 1;
         }
      };
   }
}
