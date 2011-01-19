/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.concurrent.*;

abstract class TestClassRunnerTask implements Callable<Void>
{
   private static final ThreadLocal<CopyingClassLoader> TASK_CL = new ThreadLocal<CopyingClassLoader>();

   @Override
   public final Void call()
   {
      Class<?> testClass = getTestClassFromRunner();

      if (testClass != null) {
         try {
            Class<?> copyOfTestClass = createCopyOfTestClass(testClass);
            executeCopyOfTestClass(copyOfTestClass);
            return null;
         }
         catch (Throwable e) {
            e.printStackTrace();
         }
      }

      executeOriginalTestClass();
      return null;
   }

   abstract Class<?> getTestClassFromRunner();

   private Class<?> createCopyOfTestClass(Class<?> testClass)
   {
      CopyingClassLoader cl = getClassLoaderForThisThread();
      return cl.getCopy(testClass);
   }

   protected final CopyingClassLoader getClassLoaderForThisThread()
   {
      CopyingClassLoader cl = TASK_CL.get();

      if (cl == null) {
         cl = new CopyingClassLoader();
         TASK_CL.set(cl);
      }

      return cl;
   }

   abstract void executeCopyOfTestClass(Class<?> testClass) throws Exception;
   abstract void executeOriginalTestClass();
}
