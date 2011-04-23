/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates an instance <em>field</em> or test method <em>parameter</em> whose value will be a mocked instance.
 * Such fields or parameters can be of any type, except for primitive and array types.
 * The declared type of the <em>mock field</em> or <em>mock parameter</em> is the <em>mocked type</em> (unless
 * {@linkplain #realClassName specified otherwise}).
 * For the duration of each test where such a <em>mocked type</em> is in scope, all new instances of that type, as well
 * as those previously existing, will also be mocked.
 * <em>Static methods</em> and <em>constructors</em> belonging to a mocked class type are mocked as well, just like
 * instance methods.
 * Static <em>class initializers</em> (including assignments to static fields) of a mocked class are <em>not</em>
 * stubbed out by default, but they will be if {@linkplain #stubOutClassInitialization specified}.
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
 * (if any, and including assignments to static fields) will be stubbed out.
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
    * Static initialization includes the execution of assignments to static fields of the class and the execution of
    * static initialization blocks, if any.
    * (Note that {@code static final} fields initialized with <em>compile-time</em> constants are not assigned at
    * runtime, remaining unaffected whether the class is stubbed out or not.)
    * <p/>
    * By default, static initialization code in a mocked class is <em>not</em> stubbed out.
    * The JVM will only perform static initialization of a class <em>once</em>, so stubbing out the initialization code
    * can have unexpected consequences.
    * Static initialization will occur the first time the class is instantiated, has a static method called on it, or
    * has a static field whose value is defined at runtime accessed; these are the only events which prompt the JVM to
    * initialize a class.
    * If the original class initialization code was stubbed out, then it will not be there to be executed at the time of
    * static initialization, potentially leaving static fields {@code null} and later causing
    * {@code NullPointerException}'s to occur.
    */
   boolean stubOutClassInitialization() default false;

   /**
    * Specifies the number of new instances to <em>capture</em> during test execution, between those instances which are
    * assignable to the mocked type and are created during the test.
    * If applied to a non-{@code final} mock field, each captured instance will be assigned to the field, up to the
    * number specified in the attribute. Otherwise (ie, when applied to a final field or a mock parameter), instances
    * are still captured but not made directly available to the test.
    * Note that capture can happen at any moment before the first expected invocation is recorded, or during the
    * recording and replay phases.
    * <p/>
    * When applied to a mocked <em>class</em>, capture is not restricted to instances of that class. Instances of any
    * <em>subclass</em> will also be captured. In effect, subclasses will be mocked as well, as long as they have
    * already been loaded by the JVM or get loaded during test execution.
    * The same applies to a mocked <em>interface</em>: instances of any implementing class will get captured.
    * <p/>
    * It is valid to declare two or more mock fields/parameters of the same type with a positive {@code capture} number
    * for each of them, say {@code n1}, {@code n2}, etc.
    * With non-final mock fields, this causes the first {@code n1} new instances to be assigned to the first field, the
    * following {@code n2} new instances to the second, and so on.
    *
    * @see Capturing
    */
   int capture() default 0;

   /**
    * Specify the fully qualified name of the concrete class to be considered as the mocked type when it cannot be used
    * as the declared type, and the instance automatically created by JMockit for a super-type wouldn't be appropriate
    * for the test.
    * <p/>
    * This attribute can be used with fields that are {@code final} or not.
    * In the second case it can be used in conjunction with the {@link #capture} attribute, so that only the specified
    * class is mocked, instead of all classes that implement/extend a given super-type.
    * <p/>
    * Note that this attribute can also be used when the desired concrete class is not accessible to the test (for
    * example, if it's a private inner class inside the code under test).
    *
    * @see Expectations#newInstance(String, Class[], Object...)
    */
   String realClassName() default "";
}
