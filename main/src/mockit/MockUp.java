/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.reflect.*;

import mockit.internal.*;

/**
 * A <em>mock-up</em> for a class or interface, to be used in state-based tests.
 * <p/>
 * One or more <em>mock methods</em>, each one annotated {@linkplain Mock as such} and corresponding to a "real" method
 * or constructor of the mocked class/interface, must be defined in a concrete subclass.
 * <p/>
 * This class is particularly useful for the creation on <em>in-line mock classes</em>, defined inside individual test
 * methods as anonymous inner classes.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html#inline">In the Tutorial</a>
 *
 * @param <T> specifies the class or interface(s) to be mocked
 */
public abstract class MockUp<T>
{
   private final T mockInstance;

   /**
    * Applies the mock methods defined in the concrete subclass to the class or interface specified through the type
    * parameter.
    * <p/>
    * When one or more interfaces are specified to be mocked, a mocked proxy class that implements the interfaces is
    * created, with the proxy instance made available through a call to {@link #getMockInstance()}.
    *
    * @throws IllegalArgumentException if no type to be mocked was specified;
    * or if there is a mock method for which no corresponding real method or constructor is found
    *
    * @see #MockUp(Class)
    */
   protected MockUp()
   {
      Type typeToMock = getTypeToMock();

      if (typeToMock instanceof Class<?>) {
         mockInstance = setUpMockForNonGenericType((Class<?>) typeToMock);
      }
      else if (typeToMock instanceof ParameterizedType){
         mockInstance = setUpMockForParameterizedType((ParameterizedType) typeToMock);
      }
      else { // a TypeVariable
         mockInstance = createMockInstanceForMultipleInterfaces(typeToMock);
      }
   }

   private Type getTypeToMock()
   {
      Type genericSuperclass = getClass().getGenericSuperclass();

      if (!(genericSuperclass instanceof ParameterizedType)) {
         throw new IllegalArgumentException("No type to be mocked");
      }

      return ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
   }

   private T setUpMockForNonGenericType(Class<?> classToMock)
   {
      //noinspection unchecked
      T proxy = classToMock.isInterface() ? (T) Mockit.newEmptyProxy(classToMock) : null;
      return redefineMethods(classToMock, proxy);
   }

   private T redefineMethods(Class<?> classToMock, T proxy)
   {
      Class<?> realClass = proxy == null ? classToMock : proxy.getClass();
      new RedefinitionEngine(realClass, this, getClass()).redefineMethods();
      return proxy;
   }

   private T setUpMockForParameterizedType(ParameterizedType typeToMock)
   {
      Class<?> classToMock = (Class<?>) typeToMock.getRawType();
      //noinspection unchecked
      T proxy = classToMock.isInterface() ? (T) Mockit.newEmptyProxy(typeToMock) : null;
      return redefineMethods(classToMock, proxy);
   }

   private T createMockInstanceForMultipleInterfaces(Type typeToMock)
   {
      T proxy = Mockit.newEmptyProxy(typeToMock);
      return redefineMethods(null, proxy);
   }

   /**
    * Applies the mock methods defined in the concrete subclass to the given class.
    * <p/>
    * In most cases, the constructor with no parameters can be used. This variation should be used only when the real
    * class to be mocked is not accessible or known to the test.
    *
    * @see #MockUp()
    */
   protected MockUp(Class<?> classToMock)
   {
      mockInstance = null;
      Mockit.setUpMock(classToMock, this);
   }

   /**
    * Returns the mock instance created for the interface(s) to be mocked specified by the type parameter {@code T}, or
    * {@literal null} otherwise (ie, if a class was specified to be mocked).
    */
   public final T getMockInstance()
   {
      return mockInstance;
   }
}
