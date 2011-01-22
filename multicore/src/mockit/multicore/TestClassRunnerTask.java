/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.concurrent.*;

abstract class TestClassRunnerTask implements Callable<Void>
{
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
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      if (!(cl instanceof CopyingClassLoader)) {
         cl = new CopyingClassLoader();
         Thread.currentThread().setContextClassLoader(cl);
      }

      return (CopyingClassLoader) cl;
   }

   abstract void prepareCopyOfTestClassForExecution(Class<?> testClass) throws Exception;
   abstract void executeTestClass();
}
