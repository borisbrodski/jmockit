/*
 * JMockit
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
package mockit.internal.util;

import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;

import java.util.*;

import mockit.*;
import mockit.external.asm.Type;

/**
 * Miscellaneous utility methods which don't fit into any other class, most of them related to the
 * use of Reflection.
 */
@SuppressWarnings({"unchecked", "ClassWithTooManyMethods", "OverlyComplexClass"})
public final class Utilities
{
   public static final String GENERATED_SUBCLASS_PREFIX = "$Subclass_";

   private static final Class<?>[] PRIMITIVE_TYPES = {
      null, boolean.class, char.class, byte.class, short.class, int.class, float.class, long.class,
      double.class
   };
   private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];

   private Utilities() {}

   public static <T> Class<T> loadClass(String className)
   {
      try {
         return (Class<T>) Class.forName(className);
      }
      catch (LinkageError e) {
         e.printStackTrace();
         throw e;
      }
      catch (ClassNotFoundException ignore) {
         //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
         throw new IllegalArgumentException("No class with name \"" + className + "\" found");
      }
   }

   public static <T> T newInstance(Class<T> aClass)
   {
      return newInstance(aClass, NO_PARAMETERS);
   }

   public static <T> T newInstance(Class<T> aClass, Class<?>[] parameterTypes, Object... initArgs)
   {
      Constructor<?> constructor = findSpecifiedConstructor(aClass, parameterTypes);
      return (T) invoke(constructor, initArgs);
   }

   private static Constructor<?> findSpecifiedConstructor(Class<?> theClass, Class<?>[] paramTypes)
   {
      for (Constructor<?> declaredConstructor : theClass.getDeclaredConstructors()) {
         if (matchesParameterTypes(declaredConstructor.getParameterTypes(), paramTypes)) {
            return declaredConstructor;
         }
      }

      String paramTypesDesc = getParameterTypesDescription(paramTypes);

      throw new IllegalArgumentException("Specified constructor not found: " + paramTypesDesc);
   }

   private static String getParameterTypesDescription(Class<?>[] paramTypes)
   {
      String paramTypesDesc = Arrays.asList(paramTypes).toString();
      return paramTypesDesc.replace("class ", "").replace('[', '(').replace(']', ')');
   }

   public static <T> T newInstance(String className, Class<?>[] parameterTypes, Object... initArgs)
   {
      Class<T> theClass = loadClass(className);
      return newInstance(theClass, parameterTypes, initArgs);
   }

   public static <T> T newInstance(String className, Object... nonNullArgs)
   {
      Class<?>[] paramTypes = getParameterTypesFromArguments(nonNullArgs);

      return (T) newInstance(className, paramTypes, nonNullArgs);
   }

   public static Class<?>[] getParameterTypesFromArguments(Object... args)
   {
      Class<?>[] paramTypes = new Class<?>[args.length];

      for (int i = 0; i < args.length; i++) {
         Class<?> argType = getParameterTypeFromArgument(i, args);
         paramTypes[i] = argType;
      }

      return paramTypes;
   }

   private static Class<?> getParameterTypeFromArgument(int i, Object[] args)
   {
      Object arg = args[i];

      if (arg == null) {
         throw new IllegalArgumentException(
            "Invalid null value passed as argument " + i +
            " (instead of null, provide the Class object for the parameter type)");
      }

      Class<?> argType;

      if (arg instanceof Class) {
         argType = (Class<?>) arg;
         args[i] = null;
      }
      else {
         argType = arg.getClass();

         if (Proxy.isProxyClass(argType)) {
            // Assumes that the proxy class implements a single interface.
            argType = argType.getInterfaces()[0];
         }
         else if (argType.getSimpleName().startsWith(GENERATED_SUBCLASS_PREFIX)) {
            argType = argType.getSuperclass();
         }
      }

      return argType;
   }

   public static <T> T newInnerInstance(
      String innerClassName, Object outerInstance, Object... nonNullArgs)
   {
      String className = outerInstance.getClass().getName() + '$' + innerClassName;

      Class<?>[] parameterTypes = new Class<?>[1 + nonNullArgs.length];
      parameterTypes[0] = outerInstance.getClass();

      Object[] initArgs = new Object[1 + nonNullArgs.length];
      initArgs[0] = outerInstance;

      for (int i = 0; i < nonNullArgs.length; i++) {
         parameterTypes[1 + i] = getParameterTypeFromArgument(i, nonNullArgs);
         initArgs[1 + i] = nonNullArgs[i];
      }

      return (T) newInstance(className, parameterTypes, initArgs);
   }

   public static <T> T invoke(Object targetInstance, String methodName, Object... methodArgs)
   {
      return (T) invoke(targetInstance.getClass(), targetInstance, methodName, methodArgs);
   }

   public static <T> T invoke(
      Class<?> theClass, Object targetInstance, String methodName, Object... methodArgs)
   {
      Class<?>[] paramTypes = getParameterTypesFromArguments(methodArgs);
      Method method = findCompatibleMethod(theClass, methodName, paramTypes);

      T result = (T) invoke(targetInstance, method, methodArgs);
      return result;
   }

   public static Method findCompatibleMethod(
      Object targetInstance, String methodName, Object[] args)
   {
      Class<?>[] paramTypes = getParameterTypesFromArguments(args);
      return findCompatibleMethod(targetInstance.getClass(), methodName, paramTypes);
   }

   private static Method findCompatibleMethod(
      Class<?> theClass, String methodName, Class<?>[] argTypes)
   {
      while (true) {
         Method methodFound = findCompatibleMethodInClass(theClass, methodName, argTypes);

         if (methodFound != null) {
            return methodFound;
         }
         
         Class<?> superClass = theClass.getSuperclass();

         if (superClass == null || superClass == Object.class) {
            break;
         }

         //noinspection AssignmentToMethodParameter
         theClass = superClass;
      }

      String argTypesDesc = getParameterTypesDescription(argTypes);

      throw new IllegalArgumentException(
         "No compatible method found: " + methodName + argTypesDesc);
   }

   private static Method findCompatibleMethodInClass(
      Class<?> theClass, String methodName, Class<?>[] argTypes)
   {
      for (Method declaredMethod : theClass.getDeclaredMethods()) {
         if (declaredMethod.getName().equals(methodName)) {
            Class<?>[] declaredParamTypes = declaredMethod.getParameterTypes();

            if (
               matchesParameterTypes(declaredParamTypes, argTypes) ||
               acceptsArgumentTypes(declaredParamTypes, argTypes)
            ) {
               return declaredMethod;
            }
         }
      }

      return null;
   }

   private static boolean acceptsArgumentTypes(Class<?>[] paramTypes, Class<?>[] argTypes)
   {
      int i0 = 0;

      if (paramTypes.length != argTypes.length)
      {
         if (paramTypes.length > 0 && paramTypes[0] == Invocation.class) {
            i0 = 1;
         }
         else {
            return false;
         }
      }

      for (int i = i0; i < paramTypes.length; i++) {
         Class<?> parType = paramTypes[i];
         Class<?> argType = argTypes[i - i0];

         if (isSameTypeIgnoringAutoBoxing(parType, argType) || parType.isAssignableFrom(argType)) {
            // OK, move to next parameter.
         }
         else {
            return false;
         }
      }

      return true;
   }

   public static <T> T invoke(
      Class<?> theClass, Object targetInstance, String methodName, Class<?>[] paramTypes,
      Object... methodArgs)
   {
      Method method = findSpecifiedMethod(theClass, methodName, paramTypes);
      T result = (T) invoke(targetInstance, method, methodArgs);
      return result;
   }

   public static <T> T invoke(Object targetInstance, Method method, Object... methodArgs)
   {
      ensureThatMemberIsAccessible(method);

      try {
         return (T) method.invoke(targetInstance, methodArgs);
      }
      catch (IllegalAccessException e) {
         assert false : "Not expected to happen because the method was made accessible";
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
         Throwable cause = e.getCause();

         if (cause instanceof Error) {
            throw (Error) cause;
         }
         else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
         }
         else {
            throwCheckedException((Exception) cause);
            return null;
         }
      }
   }

   private static void ensureThatMemberIsAccessible(AccessibleObject classMember)
   {
      if (!classMember.isAccessible()) {
         classMember.setAccessible(true);
      }
   }

   private static Method findSpecifiedMethod(
      Class<?> theClass, String methodName, Class<?>[] paramTypes)
   {
      for (Method declaredMethod : theClass.getDeclaredMethods()) {
         if (
            declaredMethod.getName().equals(methodName) &&
            matchesParameterTypes(declaredMethod.getParameterTypes(), paramTypes)
         ) {
            return declaredMethod;
         }
      }

      Class<?> superClass = theClass.getSuperclass();

      if (superClass != null && superClass != Object.class) {
         return findSpecifiedMethod(superClass, methodName, paramTypes);
      }

      String paramTypesDesc = getParameterTypesDescription(paramTypes);

      throw new IllegalArgumentException(
         "Specified method not found: " + methodName + paramTypesDesc);
   }

   private static boolean matchesParameterTypes(Class<?>[] declaredTypes, Class<?>[] specifiedTypes)
   {
      int i0 = 0;

      if (declaredTypes.length != specifiedTypes.length)
      {
         if (declaredTypes.length > 0 && declaredTypes[0] == Invocation.class) {
            i0 = 1;
         }
         else {
            return false;
         }
      }

      for (int i = i0; i < declaredTypes.length; i++) {
         Class<?> declaredType = declaredTypes[i];
         Class<?> specifiedType = specifiedTypes[i - i0];

         if (isSameTypeIgnoringAutoBoxing(declaredType, specifiedType)) {
            // OK, move to next parameter.
         }
         else {
            return false;
         }
      }

      return true;
   }

   private static boolean isSameTypeIgnoringAutoBoxing(Class<?> firstType, Class<?> secondType)
   {
      return
         firstType == secondType ||
         firstType.isPrimitive() && isWrapperOfPrimitiveType(firstType, secondType) ||
         secondType.isPrimitive() && isWrapperOfPrimitiveType(secondType, firstType);
   }

   private static boolean isWrapperOfPrimitiveType(Class<?> primitiveType, Class<?> otherType)
   {
      return
         primitiveType == int.class && otherType == Integer.class ||
         primitiveType == long.class && otherType == Long.class ||
         primitiveType == short.class && otherType == Short.class ||
         primitiveType == byte.class && otherType == Byte.class ||
         primitiveType == double.class && otherType == Double.class ||
         primitiveType == float.class && otherType == Float.class ||
         primitiveType == char.class && otherType == Character.class ||
         primitiveType == boolean.class && otherType == Boolean.class;
   }

   public static Method findPublicVoidMethod(Class<?> aClass, String methodName)
   {
      for (Method method : aClass.getDeclaredMethods()) {
         if (
            isPublic(method.getModifiers()) && method.getReturnType() == void.class &&
            methodName.equals(method.getName())
         ) {
            return method;
         }
      }

      return null;
   }

   private static Object[] getDefaultParameterValues(Constructor<?> constructor)
   {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      int numParameters = parameterTypes.length;
      Object[] defaultArgs = new Object[numParameters];

      for (int i = 0; i < numParameters; i++) {
         Class<?> paramType = parameterTypes[i];
         defaultArgs[i] = DefaultValues.computeForType(paramType);
      }

      return defaultArgs;
   }

   public static Object invoke(Constructor<?> constructor, Object... initArgs)
   {
      ensureThatMemberIsAccessible(constructor);

      Object[] args = initArgs != null? initArgs : getDefaultParameterValues(constructor);

      try {
         return constructor.newInstance(args);
      }
      catch (InstantiationException e) {
         assert false : "Not expected to happen because the class is expected to be concrete";
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
         assert false : "Not expected to happen because the constructor was made accessible";
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
         Throwable cause = e.getCause();

         if (cause instanceof Error) {
            throw (Error) cause;
         }
         else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
         }
         else {
            throwCheckedException((Exception) cause);
            return null;
         }
      }
   }

   public static Object getField(Class<?> theClass, String fieldName, Object targetObject)
   {
      Field field = getDeclaredField(theClass, fieldName);
      return getFieldValue(field, targetObject);
   }

   public static Field getDeclaredField(Class<?> theClass, String fieldName)
   {
      try {
         return theClass.getDeclaredField(fieldName);
      }
      catch (NoSuchFieldException ignore) {
         Class<?> superClass = theClass.getSuperclass();

         if (superClass != null) {
            return getDeclaredField(superClass, fieldName);
         }

         //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
         throw new IllegalArgumentException(
            "No instance field of name \"" + fieldName + "\" found in " + theClass);
      }
   }

   public static Object getField(Class<?> theClass, Class<?> fieldType, Object targetObject)
   {
      Field field = getDeclaredField(theClass, fieldType, targetObject != null, false);
      return getFieldValue(field, targetObject);
   }

   private static Field getDeclaredField(
      Class<?> theClass, Class<?> desiredType, boolean instanceField, boolean forAssignment)
   {
      Field found =
         getDeclaredFieldInSingleClass(theClass, desiredType, instanceField, forAssignment);

      if (found == null) {
         Class<?> superClass = theClass.getSuperclass();

         if (superClass != null && superClass != Object.class) {
            return getDeclaredField(superClass, desiredType, instanceField, forAssignment);
         }
         
         throw new IllegalArgumentException(
            (instanceField ? "Instance" : "Static") + " field of type " + desiredType.getName() +
            " not found in " + theClass);
      }

      return found;
   }

   private static Field getDeclaredFieldInSingleClass(
      Class<?> theClass, Class<?> desiredType, boolean instanceField, boolean forAssignment)
   {
      Field found = null;

      for (Field field : theClass.getDeclaredFields()) {
         Class<?> fieldType = field.getType();

         if (
            instanceField != isStatic(field.getModifiers()) &&
            isCompatibleFieldType(fieldType, desiredType, forAssignment)
         ) {
            if (found != null) {
               String message =
                  errorMessageForMoreThanOneFieldFound(
                     desiredType, instanceField, forAssignment, found, field);

               throw new IllegalArgumentException(message);
            }

            found = field;
         }
      }

      return found;
   }

   private static String errorMessageForMoreThanOneFieldFound(
      Class<?> desiredFieldType, boolean instanceField, boolean forAssignment,
      Field firstField, Field secondField)
   {
      StringBuilder message = new StringBuilder("More than one ");
      message.append(instanceField ? "instance" : "static").append(" field ");

      message.append(forAssignment ? "to which a value of type " : "from which a value of type ");
      message.append(desiredFieldType.getName());
      message.append(forAssignment ? " can be assigned" : " can be read");

      message.append(" exists in ").append(secondField.getDeclaringClass()).append(": ");
      message.append(firstField.getName()).append(", ").append(secondField.getName());

      return message.toString();
   }

   private static boolean isCompatibleFieldType(
      Class<?> fieldType, Class<?> desiredType, boolean forAssignment)
   {
      if (isSameTypeIgnoringAutoBoxing(desiredType, fieldType)) {
         return true;
      }
      else if (forAssignment) {
         return fieldType.isAssignableFrom(desiredType);
      }
      else {
         return desiredType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(desiredType);
      }
   }

   public static Object getFieldValue(Field field, Object targetObject)
   {
      ensureThatMemberIsAccessible(field);

      try {
         return field.get(targetObject);
      }
      catch (IllegalAccessException e) {
         assert false : "Not expected to happen because the field was made accessible";
         throw new RuntimeException(e);
      }
   }

   public static Field setField(
      Class<?> theClass, Object targetObject, String fieldName, Object fieldValue)
   {
      Field field =
         fieldName == null ?
            getDeclaredField(theClass, fieldValue.getClass(), targetObject != null, true) :
            getDeclaredField(theClass, fieldName);
      
      setFieldValue(field, targetObject, fieldValue);
      return field;
   }

   public static void setFieldValue(Field field, Object targetObject, Object value)
   {
      ensureThatMemberIsAccessible(field);

      try {
         field.set(targetObject, value);
      }
      catch (IllegalAccessException e) {
         assert false : "Not expected to happen because the field was made accessible";
         throw new RuntimeException(e);
      }
   }

   public static Class<?>[] getParameterTypes(String mockDesc)
   {
      Type[] paramTypes = Type.getArgumentTypes(mockDesc);
      Class<?>[] paramClasses = new Class<?>[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
         paramClasses[i] = getClassForType(paramTypes[i]);
      }

      return paramClasses;
   }

   public static Class<?> getClassForType(Type type)
   {
      int elementSort = type.getSort();

      if (elementSort < PRIMITIVE_TYPES.length) {
         return PRIMITIVE_TYPES[elementSort];
      }

      String className =
         elementSort == Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName();

      return loadClass(className);
   }

   public static void filterStackTrace(Throwable t)
   {
      StackTraceElement[] originalST = t.getStackTrace();
      List<StackTraceElement> filteredST = new ArrayList<StackTraceElement>(originalST.length);

      for (StackTraceElement ste : originalST) {
         if (ste.getFileName() != null) {
            String where = ste.getClassName();

            if (
               (!where.startsWith("sun.") || ste.isNativeMethod()) &&
               !where.startsWith("org.junit.") && !where.startsWith("junit.") &&
               !where.startsWith("org.testng.")
            ) {
               if (!where.startsWith("mockit.") || ste.getFileName().endsWith("Test.java")) {
                  filteredST.add(ste);
               }
            }
         }
      }

      t.setStackTrace(filteredST.toArray(new StackTraceElement[filteredST.size()]));
   }

   public static void throwCheckedException(Exception exceptionToThrow)
   {
      synchronized (ThrowOfCheckedException.class) {
         ThrowOfCheckedException.exceptionToThrow = exceptionToThrow;

         try {
            //noinspection ClassNewInstance
            ThrowOfCheckedException.class.newInstance();
         }
         catch (InstantiationException ignore) {}
         catch (IllegalAccessException ignored) {}
      }
   }

   private static final class ThrowOfCheckedException
   {
      static Exception exceptionToThrow;

      ThrowOfCheckedException() throws Exception { throw exceptionToThrow; }
   }

   /**
    * This method was created to work around an issue in the standard
    * {@link Class#isAnonymousClass()} method, which causes a sibling nested class to be loaded
    * when called on a nested class. If that sibling nested class is not in the classpath, a
    * <code>ClassNotFoundException</code> would result.
    * <p/>
    * This method checks only the given class name, never causing any other classes to be loaded.
    */
   public static boolean isAnonymousClass(Class<?> aClass)
   {
      String className = aClass.getName();
      int p = className.lastIndexOf('$');

      if (p > 0 && p + 1 < className.length()) {
         char c = className.charAt(p + 1);
         return isPositiveDigit(c);
      }

      return false;
   }

   public static boolean isPositiveDigit(char c)
   {
      return c >= '1' && c <= '9';
   }

   public static String objectIdentity(Object obj)
   {
      return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
   }
}
