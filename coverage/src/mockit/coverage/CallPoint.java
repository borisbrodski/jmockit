/*
 * JMockit Coverage
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mockit.coverage;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public final class CallPoint implements Serializable
{
   private static final long serialVersionUID = 362727169057343840L;
   private static final Map<StackTraceElement, Boolean> steCache =
      new HashMap<StackTraceElement, Boolean>();
   private static final Class<? extends Annotation> testAnnotation;
   private static final boolean checkTestAnnotationOnClass;
   private static final boolean checkIfTestCaseSubclass;

   static
   {
      boolean checkOnClassAlso = true;
      Class<?> annotation;

      try {
         annotation = Class.forName("org.junit.Test");
         checkOnClassAlso = false;
      }
      catch (ClassNotFoundException ignore) {
         annotation = getTestNGAnnotationIfAvailable();
      }

      //noinspection unchecked
      testAnnotation = (Class<? extends Annotation>) annotation;
      checkTestAnnotationOnClass = checkOnClassAlso;
      checkIfTestCaseSubclass = checkForJUnit3Availability();
   }

   private static Class<?> getTestNGAnnotationIfAvailable()
   {
      try {
         return Class.forName("org.testng.annotations.Test");
      }
      catch (ClassNotFoundException ignore) {
         // For older versions of TestNG:
         try {
            return Class.forName("org.testng.Test");
         }
         catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      }
   }

   private static boolean checkForJUnit3Availability()
   {
      try {
         Class.forName("junit.framework.TestCase");
         return true;
      }
      catch (ClassNotFoundException ignore) {
         return false;
      }
   }

   private final StackTraceElement ste;

   public CallPoint(StackTraceElement ste)
   {
      this.ste = ste;
   }

   public StackTraceElement getStackTraceElement()
   {
      return ste;
   }

   static CallPoint create(String file, int line, Throwable newThrowable)
   {
      StackTraceElement[] stackTrace = newThrowable.getStackTrace();
      StackTraceElement ste = stackTrace[1];

      assert file.endsWith(ste.getFileName()) :
         "CallPoint#create: found file " + ste.getFileName() + " instead of " + file;
      assert line == ste.getLineNumber() :
         "CallPoint#create: found line " + ste.getLineNumber() + " instead of " + line;

      for (int i = 2; i < stackTrace.length; i++) {
         ste = stackTrace[i];

         if (isTestMethod(ste)) {
            return new CallPoint(ste);
         }
      }

      return null;
   }

   private static boolean isTestMethod(StackTraceElement ste)
   {
      if (steCache.containsKey(ste)){
         return steCache.get(ste);
      }

      if (ste.getFileName() == null || ste.getLineNumber() < 0) {
         steCache.put(ste, false);
         return false;
      }

      Class<?> aClass = loadClass(ste.getClassName());
      Method method = findMethod(aClass, ste.getMethodName());

      if (method == null) {
         steCache.put(ste, false);
         return false;
      }

      boolean isTestMethod =
         checkTestAnnotationOnClass && aClass.isAnnotationPresent(testAnnotation) ||
         containsATestFrameworkAnnotation(method.getDeclaredAnnotations()) ||
         checkIfTestCaseSubclass && isJUnit3xTestMethod(aClass, method);

      steCache.put(ste, isTestMethod);

      return isTestMethod;
   }

   private static Class<?> loadClass(String className)
   {
      try {
         return Class.forName(className);
      }
      catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   private static Method findMethod(Class<?> aClass, String name)
   {
      for (Method method : aClass.getDeclaredMethods()) {
         if (
            Modifier.isPublic(method.getModifiers()) && method.getReturnType() == void.class &&
            name.equals(method.getName())
         ) {
            return method;
         }
      }

      return null;
   }

   private static boolean containsATestFrameworkAnnotation(Annotation[] methodAnnotations)
   {
      for (Annotation annotation : methodAnnotations) {
         String annotationName = annotation.annotationType().getName();

         if (annotationName.startsWith("org.junit.") || annotationName.startsWith("org.testng.")) {
            return true;
         }
      }

      return false;
   }

   private static boolean isJUnit3xTestMethod(Class<?> aClass, Method method)
   {
      if (!method.getName().startsWith("test")) {
         return false;
      }

      Class<?> superClass = aClass.getSuperclass();

      while (superClass != Object.class) {
         if ("junit.framework.TestCase".equals(superClass.getName())) {
            return true;
         }

         superClass = superClass.getSuperclass();
      }

      return false;
   }
}
