/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.concurrent.*;

abstract class TestClassRunnerTask implements Callable<Void>
{
   private static final ThreadLocal<CopyingClassLoader> TASK_CL = new ThreadLocal<CopyingClassLoader>();

   public final Void call()
   {
      String testClassName = getTestClassName();

      if (testClassName != null) {
         Class<?> copyOfTestClass = createCopyOfTestClass(testClassName);
         
         try {
            prepareCopyOfTestClassForExecution(copyOfTestClass);
         }
         catch (Throwable e) {
            e.printStackTrace();
         }
      }

      executeTestClass();
      return null;
   }

   abstract String getTestClassName();

   private Class<?> createCopyOfTestClass(String testClassName)
   {
      CopyingClassLoader cl = getClassLoaderForThisThread();
      return cl.getCopy(testClassName);
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

   abstract void prepareCopyOfTestClassForExecution(Class<?> testClass) throws Exception;
   abstract void executeTestClass();
}
