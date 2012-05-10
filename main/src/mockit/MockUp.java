/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.reflect.*;

import mockit.internal.annotations.*;
import mockit.internal.startup.*;

/**
 * A <em>mock-up</em> for a class or interface, to be used in state-based tests.
 * <pre>
 *
 * // Setup a mockup before exercising tested code:
 * new MockUp&lt;SomeClass>() {
 *    &#64;Mock int someMethod(int i) { assertTrue(i > 0); return 123; }
 *    &#64;Mock(maxInvocations = 2) void anotherMethod(int i, String s) { &#47;* validate arguments *&#47; }
 * };</pre>
 * One or more <em>mock methods</em>, each one annotated {@linkplain Mock as such} and corresponding to a "real" method
 * or constructor of the mocked class/interface, must be defined in the concrete subclass.
 * <p/>
 * This class is particularly useful for the creation on <em>in-line mock classes</em>, defined inside individual test
 * methods as anonymous inner classes.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html#inline">In the Tutorial</a>
 *
 * @param <T> specifies the type (class, interface, etc.) to be mocked; multiple interfaces can be mocked by defining
 * a <em>type variable</em> in the test class or test method, and using it as the type argument;
 * if the type argument itself is a parameterized type, then only its raw type is considered for mocking
 */
public abstract class MockUp<T>
{
   static { Startup.verifyInitialization(); }
   private final T mockInstance;

   /**
    * Applies the {@linkplain Mock mock methods} defined in the concrete subclass to the class or interface specified
    * through the type parameter.
    * <p/>
    * When one or more interfaces are specified to be mocked, a mocked proxy class that implements the interfaces is
    * created, with the proxy instance made available through a call to {@link #getMockInstance()}.
    *
    * @throws IllegalArgumentException if no type to be mocked was specified;
    * or if there is a mock method for which no corresponding real method or constructor is found;
    * or if the real method matching a mock method is {@code abstract}
    *
    * @see #MockUp(Class)
    */
   protected MockUp()
   {
      Type typeToMock = getTypeToMock();

      if (typeToMock instanceof Class<?>) {
         mockInstance = redefineClass((Class<?>) typeToMock);
      }
      else if (typeToMock instanceof ParameterizedType){
         mockInstance = redefineClass((Class<?>) ((ParameterizedType) typeToMock).getRawType());
      }
      else { // a TypeVariable
         mockInstance = createMockInstanceForMultipleInterfaces(typeToMock);
      }
   }

   private Type getTypeToMock()
   {
      Class<?> currentClass = getClass();

      do {
         Type superclass = currentClass.getGenericSuperclass();

         if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments()[0];
         }
         else if (superclass == MockUp.class) {
            throw new IllegalArgumentException("No type to be mocked");
         }

         currentClass = (Class<?>) superclass;
      }
      while (true);
   }

   private T redefineClass(Class<?> classToMock)
   {
      Class<?> realClass = classToMock;
      T proxy = null;

      if (classToMock.isInterface()) {
         //noinspection unchecked
         proxy = (T) Mockit.newEmptyProxy(classToMock);
         realClass = proxy.getClass();
      }

      redefineMethods(realClass);
      return proxy;
   }

   private void redefineMethods(Class<?> realClass)
   {
      new MockClassSetup(realClass, this, getClass()).redefineMethods();
   }

   private T createMockInstanceForMultipleInterfaces(Type typeToMock)
   {
      //noinspection unchecked
      T proxy = (T) Mockit.newEmptyProxy(typeToMock);
      redefineMethods(proxy.getClass());
      return proxy;
   }

   /**
    * Applies the {@linkplain Mock mock methods} defined in the concrete subclass to the given class/interface.
    * <p/>
    * In most cases, the constructor with no parameters can be used. This variation should be used only when the type
    * to be mocked is not accessible or known to the test.
    *
    * @see #MockUp()
    */
   protected MockUp(Class<?> classToMock)
   {
      mockInstance = redefineClass(classToMock);
   }

   /**
    * Returns the mock instance created for the interface(s) to be mocked specified by the type parameter {@code T}, or
    * {@literal null} otherwise (ie, if a class was specified to be mocked).
    */
   public final T getMockInstance() { return mockInstance; }

   /**
    *
    * @param cl the class loader of the class to be mocked
    * @param subclassName the fully qualified name of a class extending/implementing the class/interface specified to be
    *                     mocked
    *
    * @return {@code true} if the class should be mocked as well, {@code false} (the default) otherwise
    */
   protected boolean shouldBeMocked(ClassLoader cl, String subclassName) { return false; }
}
