/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.jar.*;
import java.util.logging.*;

import mockit.internal.util.*;

public abstract class MockingBridge implements InvocationHandler, Filter
{
   private static final Object[] EMPTY_ARGS = {};
   private static final ReentrantLock LOCK = new ReentrantLock();

   // Keeps a strong reference to the logger so that it never gets GC-ed.
   @SuppressWarnings({"FieldCanBeLocal", "NonConstantLogger"})
   private final Logger logger;

   /**
    * The instance is stored in a place directly accessible through the Java SE API, so that it can
    * be recovered from any class loader.
    */
   protected MockingBridge()
   {
      String loggerName = "" + hashCode();
      logger = Logger.getLogger(loggerName);
      logger.setFilter(this);
   }

   public final boolean isLoggable(LogRecord record) { return false; }

   protected static boolean notToBeMocked(Object mocked, String mockedClassDesc)
   {
      return
         mocked == null && "java/lang/System".equals(mockedClassDesc) && wasCalledDuringClassLoading() ||
         mocked != null && instanceOfClassThatParticipatesInClassLoading(mocked) && wasCalledDuringClassLoading();
   }

   private static boolean instanceOfClassThatParticipatesInClassLoading(Object mocked)
   {
      Class<?> mockedClass = mocked.getClass();
      return
         mockedClass == File.class || mockedClass == URL.class || mockedClass == FileInputStream.class ||
         JarFile.class.isInstance(mocked) || JarEntry.class.isInstance(mocked) || mockedClass == Manifest.class ||
         Vector.class.isInstance(mocked) || Hashtable.class.isInstance(mocked);
   }

   protected static boolean wasCalledDuringClassLoading()
   {
      if (LOCK.isHeldByCurrentThread()) return true;
      LOCK.lock();

      try {
         StackTrace st = new StackTrace(new Throwable());
         int n = st.getDepth();

         for (int i = 3; i < n; i++) {
            StackTraceElement ste = st.getElement(i);

            if ("ClassLoader.java".equals(ste.getFileName()) && "loadClass".equals(ste.getMethodName())) {
               return true;
            }
         }

         return false;
      }
      finally {
         LOCK.unlock();
      }
   }

   protected static Object[] extractMockArguments(Object[] args)
   {
      int i = 7;

      if (args.length > i) {
         Object[] mockArgs = new Object[args.length - i];
         System.arraycopy(args, i, mockArgs, 0, mockArgs.length);
         return mockArgs;
      }

      return EMPTY_ARGS;
   }
}
