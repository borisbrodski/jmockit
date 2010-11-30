/*
 * JMockit Expectations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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

import java.lang.annotation.*;

/**
 * Indicates an instance field or test method parameter whose value will be a mocked instance.
 * Such fields or parameters can be of any type, except for primitive and array types.
 * For the duration of each test where such a <em>mocked type</em> is in scope, all new instances of that type, as well
 * as those previously existing, will also be mocked.
 * <em>Static methods</em> and <em>constructors</em> belonging to a mocked class type are mocked as well, just like
 * instance methods.
 * Static initializers (including assignments to static fields) are stubbed out by default
 * (however, specifying {@code stubOutClassInitialization = false} overrides this default).
 * <p/>
 * In the case of an instance field, it can be declared in a test class, in a super-class of a test class, or in an
 * {@link Expectations} subclass.
 * In the case of a parameter, it can only be declared in a test method.
 * <p/>
 * Notice that not only a concrete {@code Expectations} subclass used in specific tests can declare mock fields, but
 * also any intermediate super-class in the inheritance hierarchy from {@code Expectations} to the "final" subclass.
 * This can be used to define reusable {@code Expectations} subclasses, which will define one or more mock fields common
 * to a given set of tests.
 * <p/>
 * Note also that for inner {@code Expectations} subclasses the full set of mock fields will consist of the local
 * "inner" fields plus any fields declared in the test class.
 * This can be used to declare mock fields that are common to all tests in the class, even though some of these tests
 * may define additional mock fields in specific {@code Expectations} subclasses.
 * The same applies to mock parameters declared for the test method.
 * <p/>
 * Therefore, there are three possible scopes for mocked types, from larger to smaller: the whole test class, the test
 * method, and the expectation block inside the test method. Some tests will use only one or two of these scopes, while
 * others can take advantage of all three.
 * <p/>
 * Usually, a mocked instance gets created and assigned to a declared mock field automatically, without the test code
 * having to do anything.
 * It is also possible, however, for the test itself to provide this instance, by declaring the field as {@code final}
 * and assigning to it the desired instance.
 * If no such instance is necessary because only static methods or constructors will be called, then this final field
 * can receive the {@literal null} reference.
 * Mock parameters, on the other hand, will always receive a mocked argument whenever the test method is executed by the
 * test runner.
 * <p/>
 * For each mocked type there is at least one <em>target class for mocking</em>, which is derived from the declared type
 * of the mock field or parameter, or specified through an annotation attribute.
 * By default, all methods (including those which are {@code static}, {@code final}, {@code private}, {@code abstract},
 * or {@code native}) and constructors in the target classes will be mocked; in addition, all static class initializers
 * (if any, and including assignments to non-constant static fields) will be stubbed out.
 * The only exceptions are the overrides (if any) for the following {@code java.lang.Object} methods:
 * {@code equals(Object)}, {@code hashCode()}, {@code toString()}, and {@code finalize()}.
 * For reasons of safety and also because mocking such methods isn't typically needed in real tests, they are not mocked
 * by default.
 * However, it is still possible to mock these methods by explicitly including them through one or more
 * {@linkplain #methods mock filters}.
 * <p/>
 * When a method or constructor is mocked, any invocations will not result in the execution of the original code, but
 * only in a call back to JMockit; according to the current phase of execution for the test - <em>record</em>,
 * <em>replay</em>, or <em>verify</em> - JMockit will then take the appropriate actions.
 * <p/>
 * The following rules are applied when deriving target classes from the mocked type:
 * <ol>
 * <li>The type is a concrete or <em>enum</em> class: this class and all its super-classes up to but excluding
 * {@code java.lang.Object} will be the target classes for mocking.</li>
 * <li>The type is an <em>interface</em> or <em>annotation</em>: a
 * {@linkplain java.lang.reflect.Proxy dynamic proxy class} is created and used as the only target.
 * </li>
 * <li>The type is an <em>abstract class</em>: a concrete subclass is generated with mock implementations for the
 * abstract methods in that class and in all of its super-classes (again, excluding {@code Object}); these super-classes
 * are also targeted for mocking of non-abstract methods and constructors.</li>
 * </ol>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#declaration">Tutorial</a>
 *
 * @see #methods
 * @see #inverse
 * @see #stubOutClassInitialization
 * @see #capture
 * @see #realClassName
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Mocked
{
   /**
    * Same as specifying only the {@link #methods} attribute.
    */
   String[] value() default {};

   /**
    * One or more <em>mock filters</em>.
    * Given a target class for mocking, only those methods and constructors which match at least one filter will be
    * mocked.
    * <p/>
    * If no other annotation attribute needs to be specified for a given mocked type, then for convenience the value for
    * this attribute can also be specified through the default {@link #value} attribute, that is, by just supplying the
    * list of mock filters without naming any annotation attribute.
    * <p/>
    * Each mock filter must follow the syntax <strong>{@code [nameRegex][(paramTypeName...)]}</strong>, where
    * {@code nameRegex} is a {@linkplain java.util.regex.Pattern regular expression} for matching method names, and
    * {@code paramTypeName} is the name of a primitive or reference parameter type (actually, any suffix of the type
    * name is enough, like "String" instead of the full class name "java.lang.String").
    * If {@code nameRegex} is omitted the filter matches only constructors.
    * If {@code (paramTypeName...)} is omitted the filter matches methods with any parameters.
    * <p/>
    * If no filters are specified, then all methods and constructors declared in the target class are mocked, and all
    * static initializers are stubbed out.
    * <p/>
    * A filter containing just the empty string matches <em>no</em> methods, no constructors, and no static initializers
    * of the target class; this can be used to obtain a mocked instance where no executable code is actually mocked or
    * stubbed out.
    * <p/>
    * The special filter {@code "<clinit>"} can be used to match the static initializers of the target class.
    * If only this filter is specified then the static initializers are stubbed out and no methods or constructors are
    * mocked; the opposite can be achieved by using the {@link #inverse} attribute.
    */
   String[] methods() default {};

   /**
    * Indicates whether the mock filters are to be inverted or not.
    * If inverted, only the methods and constructors matching them are <strong>not</strong> mocked.
    */
   boolean inverse() default false;

   /**
    * Indicates whether <em>static initialization code</em> in the mocked class should be stubbed out or not.
    * Static initialization includes the execution of all assignments to static fields of the class, as well as the
    * execution of all static initialization blocks (if any).
    * <p/>
    * The type of this attribute is an array, but <em>at most one</em> {@code boolean} value should be specified;
    * for example, {@code stubOutClassInitialization = false}. If no value is specified for the attribute, then a global
    * default for the test run is assumed.
    * <p/>
    * By default, static initialization code in a mocked class <em>is</em> stubbed out.
    * This is usually helpful, as it fully isolates the class under test from a class it depends on, but it can
    * have unexpected consequences due to the fact that the JVM will only initialize a class <em>once</em>.
    * So, if the static initialization code in a class is stubbed out <em>before</em> this class is instantiated or has
    * a static method called on it (the events which prompt the JVM to initialize the class), then the original
    * initialization code will not be there to be executed at the time of static initialization.
    * If desired, this default behavior can be turned off globally by defining the
    * {@code jmockit-retainStaticInitializers} system property. This can be done by passing the
    * {@code -Djmockit-retainStaticInitializers} initialization parameter to the JVM, or by using the proper XML
    * configuration element in a Ant/Maven build script (note that no value needs to be assigned to the property -
    * if one is, it will be ignored).
    */
   boolean[] stubOutClassInitialization() default {};

   /**
    * Specifies the number of new instances to <em>capture</em> (by assigning them to the field)
    * while the test is running, between those instances which are assignable to the mock field and
    * are created during the test (note this can happen at any moment before the first expected
    * invocation is recorded, or during the recording and replay phases).
    * <p/>
    * If {@code capture} is zero (the default), a mock field initially receives a single
    * instance and then remains with this same instance for the duration of the test; in this case,
    * no instances created by the test are later captured.
    * <p/>
    * If the value for this attribute is positive, then whenever an assignable instance is created
    * during test execution and the specified number of new instances has not been previously
    * captured, the (non-{@code final}) mock field will be assigned that new instance.
    * <p/>
    * It is valid to declare two or more mock fields of the same type with a positive {@code capture}
    * number for each of them, say {@code n1}, {@code n2}, etc.
    * In this case, the first {@code n1} new instances will be assigned to the first field, the
    * following {@code n2} new instances to the second, and so on.
    * <p/>
    * Notice that this attribute does not apply to {@code final} mock fields, which cannot be reassigned.
    *
    * @see Capturing
    */
   int capture() default 0;

   /**
    * Specify the fully qualified name of the concrete class to be considered as the mocked type
    * when it cannot be used as the declared type, and the instance automatically created by JMockit
    * for a super-type wouldn't be appropriate for the test.
    * <p/>
    * This attribute can be used with fields that are {@code final} or not.
    * In the second case it can be used in conjunction with the {@link #capture} attribute, so that
    * only the specified class is mocked, instead of all classes that implement/extend a given
    * super-type.
    * <p/>
    * Note that this attribute can also be used when the desired concrete class is not accessible to
    * the test (for example, if it's a private inner class inside the code under test).
    *
    * @see Expectations#newInstance(String, Class[], Object...)
    */
   String realClassName() default "";
}
