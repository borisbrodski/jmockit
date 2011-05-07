/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;

import mockit.*;

/**
 * Miscellaneous utility methods which don't fit into any other class, most of them related to the
 * use of Reflection.
 */
@SuppressWarnings({"unchecked", "ClassWithTooManyMethods", "OverlyComplexClass"})
public final class Utilities
{
   public static final String GENERATED_SUBCLASS_PREFIX = "$Subclass_";
   public static final String GENERATED_IMPLCLASS_PREFIX = "$Impl_";
   public static final Object[] NO_ARGS = {};

   private static final Class<?>[] PRIMITIVE_TYPES = {
      null, boolean.class, char.class, byte.class, short.class, int.class, float.class, long.class, double.class
   };
   private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<Class<?>, Class<?>>()
   {{
      put(Boolean.class, boolean.class);
      put(Character.class, char.class);
      put(Byte.class, byte.class);
      put(Short.class, short.class);
      put(Integer.class, int.class);
      put(Float.class, float.class);
      put(Long.class, long.class);
      put(Double.class, double.class);
   }};
   private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];

   private Utilities() {}

   public static <T> Class<T> loadClass(String className)
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      try {
         return (Class<T>) Class.forName(className, true, loader);
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

      throw new IllegalArgumentException(
         "Specified constructor not found: " + theClass.getSimpleName() + paramTypesDesc);
   }

   private static String getParameterTypesDescription(Type[] paramTypes)
   {
      String paramTypesDesc = Arrays.asList(paramTypes).toString();
      return paramTypesDesc.replace("class ", "").replace('[', '(').replace(']', ')');
   }

   public static <T> T newInstance(String className, Class<?>[] parameterTypes, Object... initArgs)
   {
      Class<T> theClass = loadClass(className);
      return newInstance(theClass, parameterTypes, initArgs);
   }

   public static <T> T newInstance(Class<? extends T> aClass, Object... nonNullArgs)
   {
      Class<?>[] argTypes = getArgumentTypesFromArgumentValues(nonNullArgs);
      Constructor constructor = findCompatibleConstructor(aClass, argTypes);
      return (T) invoke(constructor, nonNullArgs);
   }

   private static Constructor findCompatibleConstructor(Class<?> theClass, Class<?>[] argTypes)
   {
      for (Constructor declaredConstructor : theClass.getDeclaredConstructors()) {
         Class<?>[] declaredParamTypes = declaredConstructor.getParameterTypes();

         if (
            matchesParameterTypes(declaredParamTypes, argTypes) ||
            acceptsArgumentTypes(declaredParamTypes, argTypes)
         ) {
            return declaredConstructor;
         }
      }

      String argTypesDesc = getParameterTypesDescription(argTypes);

      throw new IllegalArgumentException("No compatible constructor found: " + theClass.getSimpleName() + argTypesDesc);
   }

   public static <T> T newInstance(String className, Object... nonNullArgs)
   {
      Class<?>[] argTypes = getArgumentTypesFromArgumentValues(nonNullArgs);
      return (T) newInstance(className, argTypes, nonNullArgs);
   }

   private static Class<?>[] getArgumentTypesFromArgumentValues(Object... args)
   {
      if (args.length == 0) {
         return NO_PARAMETERS;
      }

      Class<?>[] argTypes = new Class<?>[args.length];

      for (int i = 0; i < args.length; i++) {
         argTypes[i] = getArgumentTypeFromArgumentValue(i, args);
      }

      return argTypes;
   }

   private static Class<?> getArgumentTypeFromArgumentValue(int i, Object[] args)
   {
      Object arg = args[i];

      if (arg == null) {
         throw new IllegalArgumentException(
            "Invalid null value passed as argument " + i +
            " (instead of null, provide the Class object for the parameter type)");
      }

      Class<?> argType;

      if (arg instanceof Class<?>) {
         argType = (Class<?>) arg;
         args[i] = null;
      }
      else {
         Class<?> argClass = arg.getClass();

         if (isGeneratedImplementationClass(argClass)) {
            // Assumes that the proxy class implements a single interface.
            argType = argClass.getInterfaces()[0];
         }
         else {
            argType = getMockedClass(argClass);
         }
      }

      return argType;
   }

   public static boolean isGeneratedImplementationClass(Class<?> mockedType)
   {
      return Proxy.isProxyClass(mockedType) || isGeneratedImplementationClass(mockedType.getName());
   }

   public static Class<?> getMockedClass(Class<?> aClass)
   {
      return isGeneratedSubclass(aClass.getName()) ? aClass.getSuperclass() : aClass;
   }

   public static Class<?> getMockedClass(Object mock)
   {
      return getMockedClass(mock.getClass());
   }

   private static boolean isGeneratedSubclass(String className)
   {
      return className.contains(GENERATED_SUBCLASS_PREFIX);
   }

   private static boolean isGeneratedImplementationClass(String className)
   {
      return className.contains(GENERATED_IMPLCLASS_PREFIX);
   }

   public static boolean isGeneratedClass(String className)
   {
      return isGeneratedSubclass(className) || isGeneratedImplementationClass(className);
   }

   public static <T> T newInnerInstance(Class<? extends T> innerClass, Object outerInstance, Object... nonNullArgs)
   {
      Object[] initArgs = new Object[1 + nonNullArgs.length];
      initArgs[0] = outerInstance;
      System.arraycopy(nonNullArgs, 0, initArgs, 1, nonNullArgs.length);

      return newInstance(innerClass, initArgs);
   }

   public static <T> T newInnerInstance(String innerClassName, Object outerInstance, Object... nonNullArgs)
   {
      String className = outerInstance.getClass().getName() + '$' + innerClassName;
      Class<T> innerClass = loadClass(className);

      return newInnerInstance(innerClass, outerInstance, nonNullArgs);
   }

   public static <T> T invoke(Class<?> theClass, Object targetInstance, String methodName, Object... methodArgs)
   {
      Class<?>[] argTypes = getArgumentTypesFromArgumentValues(methodArgs);
      Method method = findCompatibleMethod(theClass, methodName, argTypes);

      if (targetInstance == null && !isStatic(method.getModifiers())) {
         throw new IllegalArgumentException(
            "Attempted to invoke non-static method without an instance to invoke it on");
      }

      T result = (T) invoke(targetInstance, method, methodArgs);
      return result;
   }

   public static Method findCompatibleMethod(Object targetInstance, String methodName, Object[] args)
   {
      Class<?>[] argTypes = getArgumentTypesFromArgumentValues(args);
      return findCompatibleMethod(targetInstance.getClass(), methodName, argTypes);
   }

   private static Method findCompatibleMethod(Class<?> theClass, String methodName, Class<?>[] argTypes)
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

      throw new IllegalArgumentException("No compatible method found: " + methodName + argTypesDesc);
   }

   private static Method findCompatibleMethodInClass(Class<?> theClass, String methodName, Class<?>[] argTypes)
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

   public static Method findNonPrivateHandlerMethod(Object handler)
   {
      Method[] declaredMethods = handler.getClass().getDeclaredMethods();
      Method nonPrivateMethod = null;

      for (Method declaredMethod : declaredMethods) {
         if (!Modifier.isPrivate(declaredMethod.getModifiers())) {
            if (nonPrivateMethod != null) {
               throw new IllegalArgumentException("More than one non-private invocation handler method found");
            }

            nonPrivateMethod = declaredMethod;
         }
      }

      if (nonPrivateMethod == null) {
         throw new IllegalArgumentException("No non-private invocation handler method found");
      }

      return nonPrivateMethod;
   }

   public static <T> T invoke(
      Class<?> theClass, Object targetInstance, String methodName, Class<?>[] paramTypes, Object... methodArgs)
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
      catch (IllegalArgumentException e) {
         filterStackTrace(e);
         throw new IllegalArgumentException("Failure to invoke method: " + method, e);
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

   private static Method findSpecifiedMethod(Class<?> theClass, String methodName, Class<?>[] paramTypes)
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

      throw new IllegalArgumentException("Specified method not found: " + methodName + paramTypesDesc);
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
      return primitiveType == WRAPPER_TO_PRIMITIVE.get(otherType);
   }

   public static boolean isWrapperOfPrimitiveType(Class<?> type)
   {
      return WRAPPER_TO_PRIMITIVE.containsKey(type);
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
         throw new IllegalArgumentException("No instance field of name \"" + fieldName + "\" found in " + theClass);
      }
   }

   public static <T> T getField(Class<?> theClass, Class<T> fieldType, Object targetObject)
   {
      Field field = getDeclaredField(theClass, fieldType, targetObject != null, false);
      return (T) getFieldValue(field, targetObject);
   }

   public static <T> T getField(Class<?> theClass, Type fieldType, Object targetObject)
   {
      Field field = getDeclaredField(theClass, fieldType, targetObject != null, false);
      return (T) getFieldValue(field, targetObject);
   }

   private static Field getDeclaredField(
      Class<?> theClass, Type desiredType, boolean instanceField, boolean forAssignment)
   {
      Field found = getDeclaredFieldInSingleClass(theClass, desiredType, instanceField, forAssignment);

      if (found == null) {
         Class<?> superClass = theClass.getSuperclass();

         if (superClass != null && superClass != Object.class) {
            return getDeclaredField(superClass, desiredType, instanceField, forAssignment);
         }
         
         throw new IllegalArgumentException(
            (instanceField ? "Instance" : "Static") + " field of " + desiredType + " not found in " + theClass);
      }

      return found;
   }

   private static Field getDeclaredFieldInSingleClass(
      Class<?> theClass, Type desiredType, boolean instanceField, boolean forAssignment)
   {
      Field found = null;

      for (Field field : theClass.getDeclaredFields()) {
         if (!field.isSynthetic()) {
            Type fieldType = field.getGenericType();

            if (
               instanceField != isStatic(field.getModifiers()) &&
               isCompatibleFieldType(fieldType, desiredType, forAssignment)
            ) {
               if (found != null) {
                  String message =
                     errorMessageForMoreThanOneFieldFound(desiredType, instanceField, forAssignment, found, field);

                  throw new IllegalArgumentException(message);
               }

               found = field;
            }
         }
      }

      return found;
   }

   private static String errorMessageForMoreThanOneFieldFound(
      Type desiredFieldType, boolean instanceField, boolean forAssignment, Field firstField, Field secondField)
   {
      StringBuilder message = new StringBuilder("More than one ");
      message.append(instanceField ? "instance" : "static").append(" field ");

      message.append(forAssignment ? "to which a value of " : "from which a value of ");
      message.append(desiredFieldType);
      message.append(forAssignment ? " can be assigned" : " can be read");

      message.append(" exists in ").append(secondField.getDeclaringClass()).append(": ");
      message.append(firstField.getName()).append(", ").append(secondField.getName());

      return message.toString();
   }

   private static boolean isCompatibleFieldType(Type fieldType, Type desiredType, boolean forAssignment)
   {
      Class<?> fieldClass = getClassType(fieldType);
      Class<?> desiredClass = getClassType(desiredType);

      if (isSameTypeIgnoringAutoBoxing(desiredClass, fieldClass)) {
         return true;
      }
      else if (forAssignment) {
         return fieldClass.isAssignableFrom(desiredClass);
      }

      return desiredClass.isAssignableFrom(fieldClass) || fieldClass.isAssignableFrom(desiredClass);
   }

   public static Class<?> getClassType(Type declaredType)
   {
      if (declaredType instanceof ParameterizedType) {
         return (Class<?>) ((ParameterizedType) declaredType).getRawType();
      }

      return (Class<?>) declaredType;
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

   public static Field setField(Class<?> theClass, Object targetObject, String fieldName, Object fieldValue)
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
      mockit.external.asm.Type[] paramTypes = mockit.external.asm.Type.getArgumentTypes(mockDesc);

      if (paramTypes.length == 0) {
         return NO_PARAMETERS;
      }

      Class<?>[] paramClasses = new Class<?>[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
         paramClasses[i] = getClassForType(paramTypes[i]);
      }

      return paramClasses;
   }

   public static Class<?> getClassForType(mockit.external.asm.Type type)
   {
      int elementSort = type.getSort();

      if (elementSort < PRIMITIVE_TYPES.length) {
         return PRIMITIVE_TYPES[elementSort];
      }

      String className =
         elementSort == mockit.external.asm.Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName();

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

      Throwable cause = t.getCause();

      if (cause != null) {
         filterStackTrace(cause);
      }
   }

   public static void throwCheckedException(Exception exceptionToThrow)
   {
      synchronized (ThrowOfCheckedException.class) {
         ThrowOfCheckedException.exceptionToThrow = exceptionToThrow;
         newInstanceUsingDefaultConstructor(ThrowOfCheckedException.class);
      }
   }

   public static Object newInstanceUsingDefaultConstructor(Class<?> aClass)
   {
      try {
         //noinspection ClassNewInstance
         return aClass.newInstance();
      }
      catch (InstantiationException ignore) { return null; }
      catch (IllegalAccessException ignored) { return null; }
   }

   private static final class ThrowOfCheckedException
   {
      static Exception exceptionToThrow;

      ThrowOfCheckedException() throws Exception { throw exceptionToThrow; }
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
      String className = aClass.getName();
      int p = className.lastIndexOf('$');
      return hasPositiveDigit(className, p);
   }

   public static boolean hasPositiveDigit(String className, int positionJustBefore)
   {
      if (positionJustBefore > 0) {
         int nextPos = positionJustBefore + 1;

         if (nextPos < className.length()) {
            char c = className.charAt(nextPos);
            return c >= '1' && c <= '9';
         }
      }

      return false;
   }

   public static String objectIdentity(Object obj)
   {
      return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
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
}
