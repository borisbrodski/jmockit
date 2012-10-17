/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import mockit.internal.util.*;

/**
 * Provides utility methods that enable access to ("de-encapsulate") otherwise non-accessible fields, methods and
 * constructors.
 * <p/>
 * The {@link Expectations} class (and its subclasses) provide context-specific versions of this same methods.
 * So, when dealing with mocked types through the Expectations API there is usually no need to import this class.
 * If used it will work just as well, though.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/ReflectionUtilities.html">Tutorial</a>
 * <br/>Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/DeencapsulationTest.java">DeencapsulationTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/powermock/test/powermock/examples/bypassencapsulation/ServiceHolder_JMockit_Test.java">ServiceHolder_JMockit_Test</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/AnimatedTransitions/test/org/jdesktop/animation/transitions/ScreenTransitionContainerResizeTest.java">ScreenTransitionContainerResizeTest</a>
 */
public final class Deencapsulation
{
   private Deencapsulation() {}

   /**
    * Gets the value of a non-accessible (eg {@code private}) field from a given object.
    *
    * @param objectWithField the instance from which to get the field value
    * @param fieldName the name of the field to get
    * @param <T> interface or class type to which the returned value should be assignable
    *
    * @throws IllegalArgumentException if the desired field is not found
    *
    * @see #getField(Object, Class)
    * @see #setField(Object, String, Object)
    */
   public static <T> T getField(Object objectWithField, String fieldName)
   {
      return FieldReflection.getField(objectWithField.getClass(), fieldName, objectWithField);
   }

   /**
    * Gets the value of a non-accessible (eg {@code private}) field from a given object, <em>assuming</em> there is only
    * one field declared in the class of the given object whose type can receive values of the specified field type.
    *
    * @param objectWithField the instance from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    *
    * @see #getField(Object, String)
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    */
   public static <T> T getField(Object objectWithField, Class<T> fieldType)
   {
      return FieldReflection.getField(objectWithField.getClass(), fieldType, objectWithField);
   }

   /**
    * Gets the value of a non-accessible static field defined in a given class.
    *
    * @param classWithStaticField the class from which to get the field value
    * @param fieldName the name of the static field to get
    * @param <T> interface or class type to which the returned value should be assignable
    *
    * @throws IllegalArgumentException if the desired field is not found
    *
    * @see #setField(Class, String, Object)
    */
   public static <T> T getField(Class<?> classWithStaticField, String fieldName)
   {
      return FieldReflection.getField(classWithStaticField, fieldName, null);
   }

   /**
    * Gets the value of a non-accessible static field defined in a given class.
    *
    * @param classWithStaticField the class from which to get the field value
    * @param fieldType the declared type of the field, or a sub-type of the declared field type
    * @param <T> interface or class type to which the returned value should be assignable
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    *
    * @see #setField(Class, String, Object)
    */
   public static <T> T getField(Class<?> classWithStaticField, Class<T> fieldType)
   {
      return FieldReflection.getField(classWithStaticField, fieldType, null);
   }

   /**
    * Sets the value of a non-accessible field on a given object.
    *
    * @param objectWithField the instance on which to set the field value
    * @param fieldName the name of the field to set
    * @param fieldValue the value to set the field to
    *
    * @throws IllegalArgumentException if the desired field is not found
    *
    * @see #setField(Class, String, Object)
    */
   public static void setField(Object objectWithField, String fieldName, Object fieldValue)
   {
      FieldReflection.setField(objectWithField.getClass(), objectWithField, fieldName, fieldValue);
   }

   /**
    * Sets the value of a non-accessible field on a given object.
    * The field is looked up by the type of the given field value instead of by name.
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    */
   public static void setField(Object objectWithField, Object fieldValue)
   {
      FieldReflection.setField(objectWithField.getClass(), objectWithField, null, fieldValue);
   }

   /**
    * Sets the value of a non-accessible static field on a given class.
    *
    * @param classWithStaticField the class on which the static field is defined
    * @param fieldName the name of the field to set
    * @param fieldValue the value to set the field to
    *
    * @throws IllegalArgumentException if the desired field is not found
    */
   public static void setField(Class<?> classWithStaticField, String fieldName, Object fieldValue)
   {
      FieldReflection.setField(classWithStaticField, null, fieldName, fieldValue);
   }

   /**
    * Sets the value of a non-accessible static field on a given class.
    * The field is looked up by the type of the given field value instead of by name.
    *
    * @param classWithStaticField the class on which the static field is defined
    * @param fieldValue the value to set the field to
    *
    * @throws IllegalArgumentException if either the desired field is not found, or more than one is
    */
   public static void setField(Class<?> classWithStaticField, Object fieldValue)
   {
      FieldReflection.setField(classWithStaticField, null, null, fieldValue);
   }

   /**
    * Invokes a non-accessible (eg {@code private}) instance method from a given class with the given arguments.
    *
    * @param objectWithMethod the instance on which the invocation is to be done; must not be null
    * @param methodName the name of the method to invoke
    * @param parameterTypes the types of the parameters as declared in the desired method
    * @param methodArgs zero or more parameter values for the invocation
    * @param <T> type to which the returned value should be assignable
    *
    * @return the return value from the invoked method, wrapped if primitive
    *
    * @see #invoke(Class, String, Object...)
    */
   public static <T> T invoke(
      Object objectWithMethod, String methodName, Class<?>[] parameterTypes, Object... methodArgs)
   {
      Class<?> theClass = objectWithMethod.getClass();
      return MethodReflection.invoke(theClass, objectWithMethod, methodName, parameterTypes, methodArgs);
   }

   /**
    * Invokes a non-accessible (eg {@code private}) instance method from a given class with the given arguments.
    *
    * @param objectWithMethod the instance on which the invocation is to be done; must not be null
    * @param methodName the name of the method to invoke
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned value should be assignable
    *
    * @return the return value from the invoked method, wrapped if primitive
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter
    *
    * @see #invoke(Class, String, Object...)
    */
   public static <T> T invoke(Object objectWithMethod, String methodName, Object... nonNullArgs)
   {
      Class<?> theClass = objectWithMethod.getClass();
      return MethodReflection.invoke(theClass, objectWithMethod, methodName, nonNullArgs);
   }

   /**
    * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
    *
    * @param classWithStaticMethod the class on which the invocation is to be done; must not be null
    * @param methodName the name of the static method to invoke
    * @param parameterTypes the types of the parameters as declared in the desired method
    * @param methodArgs zero or more parameter values for the invocation
    * @param <T> type to which the returned value should be assignable
    *
    * @see #invoke(String, String, Object...)
    */
   public static <T> T invoke(
      Class<?> classWithStaticMethod, String methodName, Class<?>[] parameterTypes, Object... methodArgs)
   {
      return MethodReflection.invoke(classWithStaticMethod, null, methodName, parameterTypes, methodArgs);
   }

   /**
    * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
    *
    * @param classWithStaticMethod the class on which the invocation is to be done; must not be null
    * @param methodName the name of the static method to invoke
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned value should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter
    *
    * @see #invoke(String, String, Object...)
    */
   public static <T> T invoke(Class<?> classWithStaticMethod, String methodName, Object... nonNullArgs)
   {
      return MethodReflection.invoke(classWithStaticMethod, null, methodName, nonNullArgs);
   }

   /**
    * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
    *
    * @param classWithStaticMethod the (fully qualified) name of the class on which the invocation is to be done;
    * must not be null
    * @param methodName the name of the static method to invoke
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned value should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter
    *
    * @see #invoke(Class, String, Object...)
    */
   public static <T> T invoke(String classWithStaticMethod, String methodName, Object... nonNullArgs)
   {
      Class<Object> theClass = ClassLoad.loadClass(classWithStaticMethod);
      return MethodReflection.invoke(theClass, null, methodName, nonNullArgs);
   }

   /**
    * Creates a new instance of a given non-accessible class, invoking the constructor which has the specified parameter
    * types.
    *
    * @param className the fully qualified name of the desired class
    * @param parameterTypes the formal parameter types for the desired constructor, possibly empty
    * @param initArgs the invocation arguments for the constructor, which must be consistent with the specified
    * parameter types
    * @param <T> type to which the returned instance should be assignable
    *
    * @return a newly created instance of the specified class, initialized through the specified constructor and
    * arguments
    *
    * @see #newInstance(String, Object...)
    * @see #newInstance(Class, Class[], Object...)
    * @see #newInnerInstance(String, Object, Object...)
    */
   public static <T> T newInstance(String className, Class<?>[] parameterTypes, Object... initArgs)
   {
      return ConstructorReflection.newInstance(className, parameterTypes, initArgs);
   }

   /**
    * Creates a new instance of a given class, invoking the constructor which has the specified parameter types.
    *
    * @param classToInstantiate the class to be instantiated
    * @param parameterTypes the formal parameter types for the desired constructor, possibly empty
    * @param initArgs the invocation arguments for the constructor, which must be consistent with the specified
    * parameter types
    * @param <T> type to which the returned instance should be assignable
    *
    * @return a newly created instance of the specified class, initialized through the specified constructor and
    * arguments
    *
    * @see #newInstance(Class, Object...)
    * @see #newInnerInstance(Class, Object, Object...)
    * @see #newInstance(String, Class[], Object...)
    */
   public static <T> T newInstance(Class<? extends T> classToInstantiate, Class<?>[] parameterTypes, Object... initArgs)
   {
      return ConstructorReflection.newInstance(classToInstantiate, parameterTypes, initArgs);
   }

   /**
    * Creates a new instance of a given non-accessible class, invoking the constructor which has parameters matching the
    * number, order, and types of the given non-null arguments.
    *
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned instance should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter, or if no constructor was found
    * that matched the given argument types
    *
    * @see #newInnerInstance(String, Object, Object...)
    * @see #newInstance(Class, Object...)
    * @see #newInstance(String, Class[], Object...)
    */
   public static <T> T newInstance(String className, Object... nonNullArgs)
   {
      return ConstructorReflection.newInstance(className, nonNullArgs);
   }

   /**
    * Creates a new instance of a given class, invoking the constructor which has parameters matching the number, order,
    * and types of the given non-null arguments.
    *
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned instance should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter, or if no constructor was found
    * that matched the given argument types
    *
    * @see #newInnerInstance(Class, Object, Object...)
    * @see #newInstance(String, Object...)
    * @see #newInstance(String, Class[], Object...)
    */
   public static <T> T newInstance(Class<? extends T> classToInstantiate, Object... nonNullArgs)
   {
      return ConstructorReflection.newInstance(classToInstantiate, nonNullArgs);
   }

   /**
    * The same as {@link #newInstance(String, Class[], Object...)}, but for instantiating an inner non-accessible class
    * of some other class, and where all other (if any) initialization arguments are known to be non-null.
    *
    * @param innerClassSimpleName the inner class simple name, that is, the part after the "$" character in its full
    * name
    * @param outerClassInstance the outer class instance to which the inner class instance will belong
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned instance should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter
    *
    * @see #newInnerInstance(Class, Object, Object...)
    */
   public static <T> T newInnerInstance(String innerClassSimpleName, Object outerClassInstance, Object... nonNullArgs)
   {
      return ConstructorReflection.newInnerInstance(innerClassSimpleName, outerClassInstance, nonNullArgs);
   }

   /**
    * The same as {@link #newInstance(String, Class[], Object...)}, but for instantiating an inner class of some other
    * class, and where all other (if any) initialization arguments are known to be non-null.
    *
    * @param innerClassToInstantiate the inner class to be instantiated
    * @param outerClassInstance the outer class instance to which the inner class instance will belong
    * @param nonNullArgs zero or more non-null parameter values for the invocation; if a null value needs to be passed,
    * the {@code Class} object for the parameter type must be passed instead
    * @param <T> type to which the returned instance should be assignable
    *
    * @throws IllegalArgumentException if a null reference was provided for a parameter
    *
    * @see #newInnerInstance(String, Object, Object...)
    */
   public static <T> T newInnerInstance(
      Class<? extends T> innerClassToInstantiate, Object outerClassInstance, Object... nonNullArgs)
   {
      return ConstructorReflection.newInnerInstance(innerClassToInstantiate, outerClassInstance, nonNullArgs);
   }
}
