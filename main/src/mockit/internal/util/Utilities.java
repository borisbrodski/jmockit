/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import mockit.internal.state.*;

/**
 * Miscellaneous utility methods.
 */
public final class Utilities
{
   public static final String GENERATED_SUBCLASS_PREFIX = "$Subclass_";
   public static final String GENERATED_IMPLCLASS_PREFIX = "$Impl_";

   static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<Class<?>, Class<?>>();
   public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<Class<?>, Class<?>>();
   static {
      WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
      WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
      WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
      WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
      WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
      WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
      WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
      WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);

      PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
      PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
      PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
      PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
      PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
      PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
      PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
      PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
   }

   private static final Map<String, Class<?>> LOADED_CLASSES = new ConcurrentHashMap<String, Class<?>>();

   public static void registerLoadedClass(Class<?> aClass)
   {
      LOADED_CLASSES.put(aClass.getName(), aClass);
   }

   public static <T> Class<T> loadClassByInternalName(String internalClassName)
   {
      return loadClass(internalClassName.replace('/', '.'));
   }

   public static <T> Class<T> loadClass(String className)
   {
      Class<?> loadedClass = LOADED_CLASSES.get(className);

      try {
         if (loadedClass == null) {
            loadedClass = loadClass(Thread.currentThread().getContextClassLoader(), className);

            if (loadedClass == null) {
               Class<?> testClass = TestRun.getCurrentTestClass();
               loadedClass = testClass == null ? null : loadClass(testClass.getClassLoader(), className);

               if (loadedClass == null) {
                  loadedClass = loadClass(Utilities.class.getClassLoader(), className);

                  if (loadedClass == null) {
                     throw new IllegalArgumentException("No class with name \"" + className + "\" found");
                  }
               }
            }
         }
      }
      catch (LinkageError e) {
         e.printStackTrace();
         throw e;
      }

      //noinspection unchecked
      return (Class<T>) loadedClass;
   }

   private static Class<?> loadClass(ClassLoader loader, String className)
   {
      try { return Class.forName(className, true, loader); } catch (ClassNotFoundException ignore) { return null; }
   }

   public static boolean isGeneratedImplementationClass(Class<?> mockedType)
   {
      return Proxy.isProxyClass(mockedType) || isGeneratedImplementationClass(mockedType.getName());
   }

   public static Class<?> getMockedClassOrInterfaceType(Class<?> aClass)
   {
      if (isGeneratedImplementationClass(aClass)) {
         // Assumes that the proxy class implements a single interface.
         return aClass.getInterfaces()[0];
      }

      return getMockedClassType(aClass);
   }

   public static Class<?> getMockedClassType(Class<?> aClass)
   {
      return isGeneratedSubclass(aClass.getName()) ? aClass.getSuperclass() : aClass;
   }

   public static Class<?> getMockedClass(Object mock)
   {
      return getMockedClassOrInterfaceType(mock.getClass());
   }

   private static boolean isGeneratedSubclass(String className)
   {
      return className.contains(GENERATED_SUBCLASS_PREFIX);
   }

   public static String getNameForGeneratedClass(Class<?> aClass)
   {
      return getNameForGeneratedClass(aClass, aClass.getSimpleName());
   }

   public static String getNameForGeneratedClass(Class<?> aClass, String suffix)
   {
      String prefix = aClass.isInterface() ? GENERATED_IMPLCLASS_PREFIX : GENERATED_SUBCLASS_PREFIX;
      StringBuilder name = new StringBuilder(60).append(prefix).append(suffix);

      if (aClass.getClassLoader() != null) {
         Package targetPackage = aClass.getPackage();

         if (targetPackage != null && !targetPackage.isSealed()) {
            name.insert(0, '.').insert(0, targetPackage.getName());
         }
      }

      return name.toString();
   }

   private static boolean isGeneratedImplementationClass(String className)
   {
      return className.contains(GENERATED_IMPLCLASS_PREFIX);
   }

   public static boolean isGeneratedClass(String className)
   {
      return isGeneratedSubclass(className) || isGeneratedImplementationClass(className);
   }

   static void ensureThatMemberIsAccessible(AccessibleObject classMember)
   {
      if (!classMember.isAccessible()) {
         classMember.setAccessible(true);
      }
   }

   public static boolean isWrapperOfPrimitiveType(Class<?> type)
   {
      return WRAPPER_TO_PRIMITIVE.containsKey(type);
   }

   public static Class<?> getClassType(Type declaredType)
   {
      if (declaredType instanceof ParameterizedType) {
         return (Class<?>) ((ParameterizedType) declaredType).getRawType();
      }

      return (Class<?>) declaredType;
   }

   /**
    * This method was created to work around an issue in the standard {@link Class#isAnonymousClass()} method, which
    * causes a sibling nested class to be loaded when called on a nested class. If that sibling nested class is not in
    * the classpath, a {@code ClassNotFoundException} would result.
    * <p/>
    * This method checks only the given class name, never causing any other classes to be loaded.
    */
   public static boolean isAnonymousClass(Class<?> aClass)
   {
      return isAnonymousClass(aClass.getName());
   }

   public static boolean isAnonymousClass(String className)
   {
      int p = className.lastIndexOf('$');
      return isAllNumeric(className, p);
   }

   public static boolean isAllNumeric(String className, int positionJustBefore)
   {
      if (positionJustBefore <= 0) {
         return false;
      }

      int nextPos = positionJustBefore + 1;
      int n = className.length();

      while (nextPos < n) {
         char c = className.charAt(nextPos);
         if (c < '0' || c > '9') return false;
         nextPos++;
      }

      return true;
   }

   public static String objectIdentity(Object obj)
   {
      return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
   }

   public static Object evaluateObjectOverride(Object obj, String methodNameAndDesc, Object[] args)
   {
      if ("equals(Ljava/lang/Object;)Z".equals(methodNameAndDesc)) {
         return obj == args[0];
      }
      else if ("hashCode()I".equals(methodNameAndDesc)) {
         return System.identityHashCode(obj);
      }
      else if ("toString()Ljava/lang/String;".equals(methodNameAndDesc)) {
         return objectIdentity(obj);
      }
      else if (
         obj instanceof Comparable<?> && args.length == 1 &&
         methodNameAndDesc.startsWith("compareTo(L") && methodNameAndDesc.endsWith(";)I")
      ) {
         Object arg = args[0];

         if (obj == arg) {
            return 0;
         }

         return System.identityHashCode(obj) > System.identityHashCode(arg) ? 1 : -1;
      }

      return null;
   }

   public static  <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotation)
   {
      for (Annotation paramAnnotation : annotations) {
         if (paramAnnotation.annotationType() == annotation) {
            //noinspection unchecked
            return (A) paramAnnotation;
         }
      }

      return null;
   }

   public static boolean containsReference(List<?> references, Object toBeFound)
   {
      for (Object reference : references) {
         if (reference == toBeFound) {
            return true;
         }
      }

      return false;
   }
}
