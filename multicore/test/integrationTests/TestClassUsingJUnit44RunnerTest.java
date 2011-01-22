/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;
import org.junit.internal.runners.*;
import org.junit.runner.*;

@SuppressWarnings({"deprecation"})
@RunWith(JUnit4ClassRunner.class)
public final class TestClassUsingJUnit44RunnerTest
{
   @BeforeClass
   public static void verifyClassLoader()
   {
      assert TestClassUsingJUnit44RunnerTest.class.getClassLoader() != ClassLoader.getSystemClassLoader();
   }

   @Test
   public void test1()
   {
      A.doSomething();
   }

   @Test
   public void test2()
   {
      new B().toString();
   }

   public static class A
   {
      public static void doSomething() { new B(); }
   }

   public static class B
   {
      @Override
      public String toString() { return "B"; }
   }
}
