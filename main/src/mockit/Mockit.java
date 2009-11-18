/*
 * JMockit Core/Annotations
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
package mockit;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Provides static methods for the mocking and stubbing of arbitrary classes from production code,
 * according to specified <em>mock classes</em> defined in test code.
 * Such methods are intended to be called from test code only.
 * <p/>
 * Once mocked, a "real" method defined in a production class will behave (during test execution) as
 * if its implementation was replaced by a call to the corresponding <em>mock method</em> in the
 * mock class.
 * Whatever value this mock method returns will be the value returned by the call to the mocked
 * method.
 * The mock method can also throw an exception or error, which will then be propagated to the caller
 * of the mocked "real" method.
 * Therefore, while mocked the original code in the real method is never executed (actually, there's
 * still a way to execute the real implementation, although not normally used for testing purposes).
 * The same basic rules apply to constructors, which can be mocked by corresponding <em>mock
 * constructors</em> or by special mock methods.
 * <p/>
 * The methods in this class can be divided in the following groups:
 * <ul>
 * <li>
 * <strong>Stubbing API</strong>: {@link #stubOut(Class...)},
 * {@link #stubOutClass(Class, String...)}, and {@link #stubOutClass(Class, boolean, String...)}.
 * </li>
 * <li>
 * <strong>Annotations API</strong> for state-based mocking: {@link MockUp},
 * {@link #setUpMocks(Object...)}, {@link #setUpMock(Class, Class)} and its several overloads,
 * {@link #setUpStartupMocks(Object...)}, {@link #setUpMocksAndStubs(Class...)}, and
 * {@link #tearDownMocks(Class...)} / {@link #tearDownMocks()}.
 * </li>
 * <li>
 * <strong>Core API</strong> for state-based mocking on JDK 1.4, now obsolete: 
 * {@linkplain #redefineMethods(Class, Class)} and its several overloads, and
 * {@link #restoreAllOriginalDefinitions()} / {@link #restoreOriginalDefinition(Class...)}.
 * </li>
 * <li>
 * <strong>Proxy-based utilities</strong>:
 * {@link #newEmptyProxy(ClassLoader, Class)} and its overloads.
 * These are merely convenience methods that create empty implementation classes for one or more
 * interfaces, where all implemented methods do nothing beyond returning a default value according
 * to the return type of each interface method.
 * The created classes can be redefined/mocked through the Core or Annotations API, and its
 * instances passed to code under test.
 * </li>
 * </ul>
 * Tutorial:
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html">Writing state-based tests</a>,
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/ReflectionUtilities.html">Reflection-based utilities</a>
 */
public final class Mockit
{
   static
   {
      Startup.verifyInitialization();
   }

   private Mockit() {}

   /**
    * Same as {@link #redefineMethods(Class, Class)}, except that any mock methods and even mock
    * constructors will be called on the given mock class instance, whenever the (redefined)
    * corresponding real methods or constructors are called.
    *
    * @param allowDefaultConstructor indicates whether the public default constructor in the mock
    * class is a mock for the default constructor in the real class
    */
   public static void redefineMethods(
      Class<?> realClass, Object mock, boolean allowDefaultConstructor)
   {
      redefineMethods(realClass, mock, mock.getClass(), allowDefaultConstructor);
   }

   /**
    * Same as {@link #redefineMethods(Class, Class)}, except for the extra parameter.
    *
    * @param allowDefaultConstructor indicates whether the public default constructor in the mock
    * class is a mock for the default constructor in the real class
    */
   public static void redefineMethods(
      Class<?> realClass, Class<?> mockClass, boolean allowDefaultConstructor)
   {
      redefineMethods(realClass, null, mockClass, allowDefaultConstructor);
   }

   /**
    * Same as {@link #redefineMethods(Class, Class)}, for a given mock class instance.
    * The mock methods will be called on this instance from the modified real methods.
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/orderMngmntWebapp/test/orderMngr/domain/order/OrderRepositoryTest.java">Example</a>
    */
   public static void redefineMethods(Class<?> realClass, Object mock)
   {
      redefineMethods(realClass, mock, mock.getClass(), false);
   }

   /**
    * Redefines methods and/or constructors in the real class with the corresponding methods or
    * constructors in a mock class (in all documentation, wherever we mention a mock/real method,
    * keep in mind that it also applies to constructors).
    * <p/>
    * For each call made during test execution to a mocked real method, the corresponding mock
    * method is called instead. For an instance mock method, the mock class instance on which the
    * call is made is created with the default constructor for the mock class, <strong>every
    * time</strong> the mocked real method is called. If you want to reuse mock instances for all
    * such calls you should pass an instance of the mock class instead of its <code>Class</code>
    * object.
    * <p/>
    * For a <strong>mock method</strong> to be considered as <strong>corresponding</strong> to a
    * given <strong>real method</strong>, it must have the same name, the exact same parameter types
    * in the same order, and also the exact same return type. The throws clauses may differ in any
    * way. Note also that the mock method can be static or not, independently of the real method
    * being static or not.
    * <p/>
    * A constructor in the real class can be mocked by a corresponding <strong>mock
    * constructor</strong> in the mock class, declared with the same signature.
    * However, since a constructor can only be called on a freshly created instance, it is generally
    * recommended to declare a mock method of name <code>$init</code> instead (which can also be
    * <code>static</code>). This method should have <code>void</code> return type and must have the
    * same declared parameters as the mocked constructor. It will be called for each new instance of
    * the real class that is created through a call to that constructor, with whatever arguments are
    * passed to it.
    * <p/>
    * <strong>Class initializers</strong> of the real class (one or more <code>static</code>
    * initialization blocks plus all assignments to <code>static</code> fields) can be mocked by
    * providing a mock method named <code>$clinit</code> in the mock class.
    * This method should return <code>void</code> and have no declared parameters.
    * It will called at most once, at the time the real class is initialized by the JVM (and since
    * all static initializers for that class are mocked, the initialization will have no effect).
    * <p/>
    * In a mock class, each method intended as a mock for some real method must be public.
    * Non-public methods in the mock class are allowed, but are not considered to be mocks.
    * <p/>
    * The mock class must not be private, being either public or defined in the same package as the
    * real class, so that the modified methods in the real class can make valid calls.
    * Note that anonymous inner class instances are accepted as mocks.
    * <p/>
    * Any constructor in the real class can be redefined by a corresponding public mock constructor,
    * except the default no-args constructor. This restriction is only to avoid the inconvenience of
    * having to explicitly define a non-public no-args constructor in the mock class, in the most
    * common case where a mock for the default constructor is not wanted. If you want to mock the
    * default constructor, then use the overload that takes a boolean parameter to indicate that.
    * <p/>
    * Mock methods and even constructors can gain access to the instance of the real class on which
    * the corresponding real method or constructor was called. This requires that the mock class
    * defines an instance field with name <strong>"it"</strong> of the same type as the real class,
    * and accessible from that real class (in general, this means the field will have to be
    * <code>public</code>). Such a field will always be set to the appropriate real class instance,
    * whenever a mock method is called. Note that through this field the mock class will be able to
    * call any accessible instance method on the real class, including the real method corresponding
    * to the current mock method. In this case, however, such calls are not allowed because they
    * lead to infinite recursion, with the mock calling itself indirectly through the redefined real
    * method. If you really need to call the real method from its mock method, then you will have to
    * use the <em>JMockit Annotations</em> API, such as {@link #setUpMocks(Object...)}, using the
    * {@link Mock} annotation with <code>reentrant = true</code>.
    *
    * @param realClass the class from production code to be mocked, which is used by code under test
    * @param mockClass the class containing the mock methods that will replace methods of same
    *                  signature in the real class
    *
    * @throws IllegalArgumentException if the mock class is an inner (non static) class
    * @throws RealMethodNotFoundForMockException if the mock class contains a public method for
    * which no corresponding method is found in the real class
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/tutorial/test/jmockit/tutorial/domain/ServiceA_CoreAPI_Test.java">Example</a>
    */
   public static void redefineMethods(Class<?> realClass, Class<?> mockClass)
   {
      redefineMethods(realClass, null, mockClass, false);
   }

   private static void redefineMethods(
      Class<?> realClass, Object mock, Class<?> mockClass, boolean allowDefaultConstructor)
   {
      new RedefinitionEngine(realClass, mock, mockClass, allowDefaultConstructor).redefineMethods();
   }

   /**
    * Stubs out all methods, constructors, and static initializers in the given classes, so that
    * they do nothing whenever executed.
    * <p/>
    * Note that any stubbed out constructor will still call a constructor in the super-class, which
    * in turn will be executed normally unless also stubbed out.
    * The super-constructor to be called is chosen arbitrarily.
    * The classes are stubbed out in the order they are given, so make sure any super-class comes
    * first.
    * <p/>
    * Methods with non-<code>void</code> return type will return the default value for this type,
    * that is, zero for a number or <code>char</code>, <code>false</code> for a boolean, empty for
    * an array, or <code>null</code> for a reference type.
    * <p/>
    * If a different behavior is desired for any method or constructor, then
    * {@link #setUpMocks(Object...)} and the other similar methods can be used right after the call
    * to this method. They will override any stub previously created with the corresponding mock
    * implementation, if any.
    *
    * @param realClasses one or more regular classes to be stubbed out
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/powermock/test/powermock/examples/suppress/constructor/ExampleWithEvilChild_JMockit_Test.java">Example</a>
    */
   public static void stubOut(Class<?>... realClasses)
   {
      for (Class<?> realClass : realClasses) {
         new RedefinitionEngine(realClass).stubOut();
      }
   }

   /**
    * Same as {@link #stubOut(Class...)} for the given class, except that only the specified class
    * members (if any) are stubbed out, leaving the rest unaffected.
    * Such class members include the methods and constructors defined by the class, plus any static
    * or instance initialization blocks it might have.
    * Note that if <em>no</em> filters are specified the whole class will be stubbed out.
    * <p/>
    * For methods, the filters are {@linkplain java.util.regex.Pattern regular expressions} for
    * method names, optionally followed by parameter type names between parentheses.
    * For constructors, only the parameters are specified.
    * For more details about the syntax for mock filters, see the {@link MockClass#stubs} annotation
    * attribute.
    * <p/>
    * The special filter "&lt;clinit>" will match all static initializers in the given class.
    * <p/>
    * To stub out instance field initializers it is necessary to actually specify all constructors
    * in the class, because such initialization assignments are copied to each and every constructor
    * by the Java compiler.
    *
    * @param realClass a regular class to be stubbed out
    * @param filters one or more filters that specify which class members (methods, constructors,
    * and/or static initialization blocks) to be stubbed out
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/powermock/test/powermock/examples/suppress/method/ExampleWithEvilMethod_JMockit_Test.java">Example</a>
    */
   public static void stubOutClass(Class<?> realClass, String... filters)
   {
      new RedefinitionEngine(realClass, true, filters).stubOut();
   }

   /**
    * The same as {@link #stubOutClass(Class, String...)}, but specifying whether filters are to be
    * inverted or not.
    *
    * @param inverse indicates whether the mock filters are to be inverted or not; if inverted, only
    * the methods and constructors matching them are <strong>not</strong> mocked
    */
   public static void stubOutClass(Class<?> realClass, boolean inverse, String... filters)
   {
      new RedefinitionEngine(realClass, !inverse, filters).stubOut();
   }

   /**
    * Same as {@link #stubOutClass(Class, String...)}, but accepting the (fully qualified) name of
    * the real class. This is useful when said class is not accessible from the test.
    */
   public static void stubOutClass(String realClassName, String... filters)
   {
      Class<?> realClass = Utilities.loadClass(realClassName);
      new RedefinitionEngine(realClass, true, filters).stubOut();
   }

   /**
    * Same as {@link #stubOutClass(Class, boolean, String...)}, but accepting the (fully qualified)
    * name of the real class. This is useful when said class is not accessible from the test.
    */
   public static void stubOutClass(String realClassName, boolean inverse, String... filters)
   {
      Class<?> realClass = Utilities.loadClass(realClassName);
      new RedefinitionEngine(realClass, !inverse, filters).stubOut();
   }

   /**
    * Given a mix of {@linkplain MockClass mock} and real classes,
    * {@linkplain #setUpMock(Class, Class) sets up} each mock class for the associated real class,
    * and {@linkplain #stubOut stubs out} each specified regular class.
    *
    * @param mockAndRealClasses one or more mock classes and/or regular classes to be stubbed out
    */
   public static void setUpMocksAndStubs(Class<?>... mockAndRealClasses)
   {
      for (Class<?> mockOrRealClass : mockAndRealClasses) {
         RedefinitionEngine redefinition = new RedefinitionEngine(mockOrRealClass);

         if (redefinition.isWithMockClass()) {
            redefinition.redefineMethods();
         }
         else {
            redefinition.stubOut();
         }
      }
   }

   /**
    * Sets up the mocks defined in one or more {@linkplain MockClass mock classes}.
    * <p/>
    * After this call, all such mocks are "in effect" until the end of the test method inside which
    * it appears, if this is the case.
    * If the method is a "before"/"setUp" method which executes before all test methods, then the
    * mocks will remain in effect until they are explicitly {@linkplain #tearDownMocks(Class...)
    * torn down} in an "after"/"tearDown" method.
    * <p/>
    * Any invocation count constraints specified on the mocks will be automatically verified after
    * the code under test is executed.
    * <p/>
    * For each given mock class or instance, method redefinition occurs as if
    * {@link #redefineMethods(Class, Class)} or {@link #redefineMethods(Class, Object)} was called,
    * respectively, except that the rules for determining which methods are mocks are different
    * (that is, in this case only methods or constructors annotated as mocks are considered to be
    * so).
    *
    * @param mockClassesOrInstances one or more classes (<code>Class</code> objects) or instances of
    * classes which define arbitrary methods and/or constructors, where the ones annotated as
    * {@linkplain Mock mocks} will be used to redefine corresponding real methods/constructors in a
    * designated {@linkplain MockClass#realClass() real class} (usually, a class on which the code
    * under test depends on)
    *
    * @throws IllegalArgumentException if any mock class fails to specify the corresponding real
    * class using the <code>@MockClass(realClass = ...)</code> annotation
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/orderMngmntWebapp/test/orderMngr/web/submitOrder/OrderEntryPageTest.java">Example</a>
    */
   public static void setUpMocks(Object... mockClassesOrInstances)
   {
      for (Object mockClassOrInstance : mockClassesOrInstances) {
         Class<?> mockClass;
         Object mock;

         if (mockClassOrInstance instanceof Class) {
            mockClass = (Class<?>) mockClassOrInstance;
            mock = null;
         }
         else {
            mockClass = mockClassOrInstance.getClass();
            mock = mockClassOrInstance;
         }

         new RedefinitionEngine(mock, mockClass, false).redefineMethods();
      }
   }

   /**
    * Sets up the <em>startup</em> mocks defined in one or more mock classes, similarly to
    * {@link #setUpMocks(Object...)}. The difference is in the lifetime of the mocks, which will last
    * to the end of the current test suite execution.
    * Consequently, this method should only be called once, before the first test begins execution.
    * One way to achieve this is to put the call in the static initializer of a common base class
    * extended by all test classes in the suite.
    * Another way is by configuring JMockit to apply one or more of the mock classes at startup.
    * <p/>
    * There are two mechanisms that allow specifying mock classes to be loaded at JMockit startup
    * time:
    * <ol>
    * <li>Using the "-javaagent:jmockit.jar=&lt;agentArgs>" JVM argument, where "agentArgs" contains
    * one or more <strong>mock class names</strong> (separated by semicolons if more than one).</li>
    * <li>Using a customized "jmockit.properties" file, which must precede the standard file located
    * inside jmockit.jar (see comments in that file for more details).
    * </li>
    * </ol>
    * Note that if you pass all desired mock classes to JMockit at startup using one of these two
    * mechanisms, this method won't be used. However, it's also possible to pass only one of those
    * mock classes to JMockit and then call this method in the no-args constructor of the chosen
    * mock class, to cause the remaining mock classes to be applied.
    * <p/>
    * Note also that it's possible to package a whole set of mock classes in a jar file containing
    * a jmockit.properties file and then, by simply adding the jar to the classpath <em>before</em>
    * jmockit.jar, to have it loaded and applied automatically for any test suite execution, as
    * soon as JMockit itself is initialized.
    *
    * @param mockClassesOrInstances one or more classes (<code>Class</code> objects) or instances of
    * classes which define arbitrary methods and/or constructors, where the ones annotated as
    * {@linkplain Mock mocks} will be used to redefine corresponding real methods/constructors in a
    * designated {@linkplain MockClass#realClass() real class} (usually, a class on which the code
    * under test depends on)
    *
    * @throws IllegalArgumentException if any mock class fails to specify the corresponding real
    * class using the <code>@MockClass(realClass = ...)</code> annotation
    */
   public static void setUpStartupMocks(Object... mockClassesOrInstances)
   {
      for (Object mockClassOrInstance : mockClassesOrInstances) {
         Class<?> mockClass;
         Object mock;

         if (mockClassOrInstance instanceof Class) {
            mockClass = (Class<?>) mockClassOrInstance;
            mock = null;
         }
         else {
            mockClass = mockClassOrInstance.getClass();
            mock = mockClassOrInstance;
         }

         new RedefinitionEngine(mock, mockClass, false).setUpStartupMock();
      }
   }

   /**
    * Similar to {@link #setUpMocks(Object...)}, but accepting a single mock and its corresponding
    * real class.
    * <p/>
    * Useful when the real class is not known in advance, such as when it is determined at runtime
    * through configuration of by creating a {@link Proxy} for an interface.
    *
    * @param realClass the class to be mocked that is used by code under test
    * @param mock an instance of the class containing the mock methods/constructors for the real
    * class
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/orderMngmntWebapp/test/orderMngr/domain/order/OrderFactoryTest.java">Example</a>
    */
   public static void setUpMock(Class<?> realClass, Object mock)
   {
      Class<?> mockClass = mock.getClass();
      new RedefinitionEngine(realClass, mock, mockClass).redefineMethods();
   }

   /**
    * Same as {@link #setUpMock(Class, Object)}, but accepting the (fully qualified) name of the
    * real class. This is useful when said class is not accessible from the test.
    */
   public static void setUpMock(String realClassName, Object mock)
   {
      Class<?> realClass = Utilities.loadClass(realClassName);
      setUpMock(realClass, mock);
   }

   /**
    * Similar to {@link #setUpMocks(Object...)}, but accepting a single mock class and its
    * corresponding real class.
    * <p/>
    * Can also be useful when the real class is not known in advance, such as when it is determined
    * at runtime through configuration or by creating a {@link Proxy} for an interface.
    *
    * @param realClass the class to be mocked that is used by code under test
    * @param mockClass the class containing the mock methods/constructors for the real class
    *
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/MockAnnotationsTest.java">Example</a>
    */
   public static void setUpMock(Class<?> realClass, Class<?> mockClass)
   {
      new RedefinitionEngine(realClass, null, mockClass).redefineMethods();
   }

   /**
    * Same as {@link #setUpMock(Class, Class)}, but accepting the (fully qualified) name of the
    * real class. This is useful when said class is not accessible from the test.
    */
   public static void setUpMock(String realClassName, Class<?> mockClass)
   {
      Class<?> realClass = Utilities.loadClass(realClassName);
      setUpMock(realClass, mockClass);
   }

   /**
    * Sets up the mocks defined in the given mock class.
    * <p/>
    * If the type {@linkplain MockClass#realClass referred to} by the mock class is actually an
    * interface, then a {@linkplain #newEmptyProxy(ClassLoader, Class) new empty proxy} is created.
    *
    * @param mockClassOrInstance the mock class itself (given by its {@code Class} literal), or an
    * instance of the mock class
    *
    * @return the new proxy instance created for the mocked interface, or null otherwise
    *
    * @throws IllegalArgumentException if the mock class fails to specify an interface or class
    * using the {@code @MockClass(realClass = ...)} annotation
    *
    * @see #setUpMock(Class, Object)
    * @see #setUpMocks(Object...)
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/orderMngmntWebapp/test/orderMngr/domain/order/OrderFactoryTest.java">Example</a>
    */
   public static <T> T setUpMock(Object mockClassOrInstance)
   {
      Class<?> mockClass;
      Object mock;

      if (mockClassOrInstance instanceof Class) {
         mockClass = (Class<?>) mockClassOrInstance;
         mock = null;
      }
      else {
         mockClass = mockClassOrInstance.getClass();
         mock = mockClassOrInstance;
      }

      RedefinitionEngine redefinition = new RedefinitionEngine(mock, mockClass, false);
      Class<?> realClass = redefinition.getRealClass();
      T proxy = null;

      if (realClass.isInterface()) {
         //noinspection unchecked
         proxy = (T) newEmptyProxy(mockClass.getClassLoader(), realClass);
         redefinition.setRealClass(proxy.getClass());
      }

      redefinition.redefineMethods();

      return proxy;
   }

   /**
    * Discards any mocks currently in effect, for all test scopes: the current test method (if any),
    * the current test (which starts with the first "before" method and continues until the last
    * "after" method), the current test class (which includes all code from the first "before class"
    * method to the last "after class" method), and the current test suite.
    * <p/>
    * Notice that a call to this method will tear down <em>all</em> mock classes that were applied
    * through use of the Annotations or Core API that are still in effect, as well as any mock
    * classes or stubs applied to the current test class through {@code @UsingMocksAndStubs}.
    * In other words, it would effectively prevent mocks to be set up at the test class and test
    * suite levels. So, use it only if necessary and if it won't discard mock classes that should
    * remain in effect. Consider using {@link #tearDownMocks(Class...)} instead, which lets you
    * restrict the set of real classes to be restored.
    * <p/>
    * JMockit will automatically restore classes mocked by a test method at the end of that test
    * method's execution, as well as all classes mocked for the test class as a whole (through a
    * "before class" method or an {@code @UsingMocksAndStubs} annotation) before the first test in
    * the next test class is executed.
    * <p/>
    * This is equivalent to {@link #restoreAllOriginalDefinitions()}.
    */
   public static void tearDownMocks()
   {
      restoreAllOriginalDefinitions();
   }

   /**
    * Discards any mocks set up for the specified classes that are currently in effect, for all test
    * scopes: the current test method (if any), the current test (which starts with the first
    * "before" method and continues until the last "after" method), the current test class (which
    * includes all code from the first "before class" method to the last "after class" method), and
    * the current test suite.
    * <p/>
    * Notice that if one of the given real classes has a mock class applied at the level of the test
    * class, calling this method would negate the application of that mock class.
    * JMockit will automatically restore classes mocked by a test method at the end of that test
    * method's execution, as well as all classes mocked for the test class as a whole (through a
    * "before class" method or an {@code @UsingMocksAndStubs} annotation) before the first test in
    * the next test class is executed.
    * <p/>
    * In practice, this method should only be used inside "after" methods ({@code tearDown()} in a
    * JUnit 3.8 test class), since mock classes set up in a "before" or {@code setUp()} method are
    * <em>not</em> automatically discarded.
    * <p/>
    * This is equivalent to {@link #restoreOriginalDefinition(Class...)}.
    */
   public static void tearDownMocks(Class<?>... realClasses)
   {
      restoreOriginalDefinition(realClasses);
   }

   /**
    * Verify that any and all expectations defined for the {@linkplain Mock mocks} which are in
    * effect are satisfied at this moment (which normally is just after the execution of code under
    * test), throwing an {@link AssertionError} for any violation.
    * <p/>
    * After verification the execution status for each mock (such as the invocation count, kept
    * while the code under test is being executed) is reset. Therefore, mocks must be set up again
    * before the next verification. (This in practice only means that a test should set up mocks,
    * execute code under test, verify expectations, tear down mocks, and then be done.)
    * <p/>
    * This assert can safely be called even if no expectations were set for a particular test.
    * <p/>
    * <strong>This method will be called automatically</strong> at the end of each test execution,
    * so it normally does not need to be explicitly called in test code.
    *
    * @see Mock#invocations()
    * @see Mock#minInvocations()
    * @see Mock#maxInvocations()
    */
   public static void assertExpectations()
   {
      TestRun.verifyExpectationsOnAnnotatedMocks();
   }

   /**
    * Restores a given set of classes to their original definitions. This is equivalent to calling
    * <code>redefineMethods(realClass, realClass)</code>.
    * <p/>
    * In practice, this method should only be used inside "after" methods ({@code tearDown()} with
    * JUnit 3.8, {@code @After}-annotated with JUnit 4, and {@code @AfterMethod}-annotated with
    * TestNG). 
    * Otherwise, it is redundant because JMockit will automatically restore all classes mocked by a
    * test method at the end of that test method's execution, as well as all classes mocked for the
    * test class as a whole (through a "before class" method or an {@code @UsingMocksAndStubs}
    * annotation) before the first test in the next test class is executed.
    *
    * @param realClasses one or more real classes from production code, which may have had methods
    * redefined
    */
   public static void restoreOriginalDefinition(Class<?>... realClasses)
   {
      Set<Class<?>> classesToRestore = new HashSet<Class<?>>();
      Collections.addAll(classesToRestore, realClasses);
      TestRun.mockFixture().restoreAndRemoveRedefinedClasses(classesToRestore);
   }

   /**
    * {@linkplain #restoreOriginalDefinition Restores the original definitions} for all the classes
    * which have been redefined, if any. Once this method executes, all "real" classes will be back
    * to the definitions they had at JVM startup.
    * <p/>
    * In practice, this method should only be used if all mocked classes needs to be restored in the
    * middle of some test. Otherwise, it is unnecessary because JMockit will automatically restore
    * all classes mocked by a test at the end of that test, as well as all classes mocked for the
    * test class as a whole (eg, in a <code>@BeforeClass</code> JUnit method) before the first test
    * in the next test class is executed.
    */
   public static void restoreAllOriginalDefinitions()
   {
      MockFixture mockFixture = TestRun.mockFixture();

      Set<Class<?>> redefinedClasses = mockFixture.getRedefinedClasses();
      mockFixture.restoreAndRemoveRedefinedClasses(redefinedClasses);
      assert mockFixture.getRedefinedClassCount() == 0;

      TestRun.getMockClasses().getRegularMocks().discardInstances();
   }

   /**
    * Same as {@link #newEmptyProxy(ClassLoader, Class)}, but with the class loader obtained from
    * the interface to be proxied. Note that this may lead to a NoClassDefFoundError if that
    * interface was loaded by the boot class loader (usually, when it's a JRE class). Therefore, you
    * should only use this method for application-defined interfaces.
    * <p/>
    * This method is just a convenience for some uses of the <em>Core/Annotations</em> API.
    * In <em>JMockit Expectations</em> in particular, mock objects will be automatically created and
    * assigned to any mock fields or parameters.
    */
   public static <E> E newEmptyProxy(Class<E> interfaceToBeProxied)
   {
      return newEmptyProxy(interfaceToBeProxied.getClassLoader(), interfaceToBeProxied);
   }

   /**
    * Creates a {@link Proxy} implementation for a given interface where all methods are empty, with
    * return values for non-void methods being the appropriate default value (0 for int, null for a
    * reference type, and so on).
    * <p/>
    * The <code>equals</code>, <code>hashCode</code>, and <code>toString</code> methods inherited
    * from <code>java.lang.Object</code> are overridden with an appropriate implementation in each
    * case: <code>equals</code> is implemented by comparing the two object references (the proxy
    * instance and the method argument) for equality; <code>hashCode</code> is implemented to return
    * the identity hash code for the proxy instance; and <code>toString</code> returns the standard
    * string representation that <code>Object#toString</code> would have returned.
    * <p/>
    * This is useful for creating stubs which can then be mocked through individual method
    * redefinitions.
    * <p/>
    * This method is just a convenience for some uses of the <em>Core/Annotations</em> API.
    * In <em>JMockit Expectations</em> in particular, mock objects will be automatically created and
    * assigned to any mock fields.
    *
    * @param loader the class loader under which to define the proxy class; usually this would be
    * the application class loader, which can be obtained from any application class
    * @param interfaceToBeProxied a Class object for an interface
    *
    * @return the created proxy instance
    *
    * @see #newEmptyProxy(Class)
    * @see #newEmptyProxy(Type...)
    * @see <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/orderMngmntWebapp/test/orderMngr/domain/order/OrderRepositoryTest.java">Example</a>
    */
   public static <E> E newEmptyProxy(ClassLoader loader, Class<E> interfaceToBeProxied)
   {
      Class<?>[] interfaces =
         loader == null ?
            new Class<?>[] {interfaceToBeProxied} :
            new Class<?>[] {interfaceToBeProxied, EmptyProxy.class};

      //noinspection unchecked
      return (E) Proxy.newProxyInstance(loader, interfaces, MockInvocationHandler.INSTANCE);
   }

   /**
    * Creates a {@link Proxy} implementation for a given set of interface types.
    * In this created class all methods will be empty, with return values for non-void methods being
    * the appropriate default value (0 for int, null for a reference type, and so on).
    * <p/>
    * The <code>equals</code>, <code>hashCode</code>, and <code>toString</code> methods inherited
    * from <code>java.lang.Object</code> are overridden with an appropriate implementation in each
    * case: <code>equals</code> is implemented by comparing the two object references (the proxy
    * instance and the method argument) for equality; <code>hashCode</code> is implemented to return
    * the identity hash code for the proxy instance; and <code>toString</code> returns the standard
    * string representation that <code>Object#toString</code> would have returned.
    * <p/>
    * This is useful for creating stubs which can then be mocked through individual method
    * redefinitions.
    * <p/>
    * This method is just a convenience for some uses of the <em>Core/Annotations</em> API.
    * In <em>JMockit Expectations</em> in particular, mock objects will be automatically created and
    * assigned to any mock fields or parameters.
    *
    * @param interfacesToBeProxied one or more <code>Type</code> objects, each of which can be a
    * <code>Class</code> object for an interface, a {@link ParameterizedType} whose raw type is an
    * interface, or a {@link TypeVariable} whose bounds are interfaces
    *
    * @return the created proxy instance
    */
   public static <E> E newEmptyProxy(Type... interfacesToBeProxied)
   {
      List<Class<?>> interfaces = new ArrayList<Class<?>>();

      for (Type type : interfacesToBeProxied) {
         addInterface(interfaces, type);
      }

      interfaces.add(EmptyProxy.class);

      ClassLoader loader = Mockit.class.getClassLoader();
      Class<?>[] interfacesArray = interfaces.toArray(new Class<?>[interfaces.size()]);

      //noinspection unchecked
      return (E) Proxy.newProxyInstance(loader, interfacesArray, MockInvocationHandler.INSTANCE);
   }

   private static void addInterface(List<Class<?>> interfaces, Type type)
   {
      if (type instanceof Class) {
         interfaces.add((Class<?>) type);
      }
      else if (type instanceof ParameterizedType) {
         ParameterizedType paramType = (ParameterizedType) type;
         interfaces.add((Class<?>) paramType.getRawType());
      }
      else if (type instanceof TypeVariable) {
         TypeVariable<?> typeVar = (TypeVariable<?>) type;
         addBoundInterfaces(interfaces, typeVar.getBounds());
      }
   }

   private static void addBoundInterfaces(List<Class<?>> interfaces, Type[] bounds)
   {
      for (Type bound : bounds) {
         addInterface(interfaces, bound);
      }
   }
}
