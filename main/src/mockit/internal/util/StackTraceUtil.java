/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.reflect.*;

/**
 * Provides optimized utility methods to extract stack trace information.
 */
public final class StackTraceUtil
{
   private static final Method getStackTraceDepth;
   private static final Method getStackTraceElement;

   static
   {
      try {
         getStackTraceDepth = Throwable.class.getDeclaredMethod("getStackTraceDepth");
         getStackTraceDepth.setAccessible(true);

         getStackTraceElement = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
         getStackTraceElement.setAccessible(true);
      }
      catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   private StackTraceUtil() {}

   public static int getDepth(Throwable t)
   {
      int depth = 0;

      try {
         depth = (Integer) getStackTraceDepth.invoke(t);
      }
      catch (IllegalAccessException ignore) {}
      catch (InvocationTargetException ignored) {}

      return depth;
   }

   public static StackTraceElement getElement(Throwable t, int index)
   {
      StackTraceElement element = null;

      try {
         element = (StackTraceElement) getStackTraceElement.invoke(t, index);
      }
      catch (IllegalAccessException ignore) {}
      catch (InvocationTargetException ignored) {}

      return element;
   }

   public static void filterStackTrace(Throwable t)
   {
      int n = getDepth(t);
      StackTraceElement[] filteredST = new StackTraceElement[n];
      int j = 0;

      for (int i = 0; i < n; i++) {
         StackTraceElement ste = getElement(t, i);

         if (ste.getFileName() != null) {
            String where = ste.getClassName();

            if (
               (!where.startsWith("sun.") || ste.isNativeMethod()) &&
               !where.startsWith("org.junit.") && !where.startsWith("junit.") && !where.startsWith("org.testng.")
            ) {
               if (!where.startsWith("mockit.") || ste.getFileName().endsWith("Test.java")) {
                  filteredST[j] = ste;
                  j++;
               }
            }
         }
      }

      StackTraceElement[] newStackTrace = new StackTraceElement[j];
      System.arraycopy(filteredST, 0, newStackTrace, 0, j);
      t.setStackTrace(newStackTrace);

      Throwable cause = t.getCause();

      if (cause != null) {
         filterStackTrace(cause);
      }
   }
}
