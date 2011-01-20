/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import mockit.*;

public final class TestClassRunnerTaskTest
{
   @Test
   public void runCodeInSeparateThreadForEachCoreUsingCopyingClassLoaderPerThread() throws Exception
   {
      List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

      for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
         tasks.add(new FakeTestClassRunnerTask());
      }

      MultiCoreTestRunner.runUsingAvailableCores(tasks);
   }

   static final class FakeTestClassRunnerTask extends TestClassRunnerTask
   {
      @Override
      String getTestClassName() { return TestClass.class.getName(); }

      @Override
      void prepareCopyOfTestClassForExecution(Class<?> testClass) throws Exception
      {
      }

      @Override
      void executeTestClass()
      {
         Runnable runner = Deencapsulation.newInstance(TestClass.class);
         runner.run();
      }
   }

   public static class TestClass implements Runnable
   {
      public void run()
      {
         assertDefinedWithCustomClassLoader();
         new Dependency().doSomethingElse();
         assert Dependency.counter == 1 : "counter = " + Dependency.counter;
      }

      private void assertDefinedWithCustomClassLoader()
      {
         ClassLoader cl = getClass().getClassLoader();
         System.out.println(getClass() + ": " + System.identityHashCode(getClass()) + ", " + cl);
      }
   }

   public static class Dependency
   {
      public static int counter;

      public void doSomethingElse()
      {
         counter++;
         assertDefinedWithCustomClassLoader();
      }

      private void assertDefinedWithCustomClassLoader()
      {
         ClassLoader cl = getClass().getClassLoader();
         System.out.println(getClass() + ": " + System.identityHashCode(getClass()) + ", " + cl);
      }
   }
}
