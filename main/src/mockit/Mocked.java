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
 * Indicates an instance field or parameter whose value will be a mock object.
 * Such fields or parameters can be of any type, except for primitive and array types.
 * For the duration of each test where such a <em>mocked type</em> is in scope, all new instances of
 * that type, as well as those previously existing, will be mocks.
 * Static methods and constructors belonging to a mocked class type will also be mocked.
 * Static initializers (including assignments to static fields) will be stubbed out by default (see
 * {@link #methods} for a way to override this default).
 * <p/>
 * In the case of an instance field, it can be declared in a test class, in a super-class of a test
 * class, or in an {@link Expectations} subclass.
 * In the case of a parameter, it can only be declared in a test method.
 * <p/>
 * Notice that not only a concrete {@code Expectations} subclass used in specific tests can
 * declare mock fields, but also any intermediate super-class in the inheritance hierarchy from
 * {@code Expectations} to the "final" subclass. This can be used to define reusable
 * {@code Expectations} subclasses, which will define one or more mock fields common to a given set
 * of tests.
 * <p/>
 * Note also that for inner {@code Expectations} subclasses the full set of mock fields will consist
 * of the local "inner" fields plus any fields declared in the test class.
 * This can be used to declare mock fields that are common to all tests in the class, even though
 * some of them may define additional mock fields in their specific {@code Expectations} subclasses.
 * The same applies to mock parameters declared for the test method.
 * <p/>
 * In conclusion, there are three possible scopes for mocked types, from larger to smaller: the
 * whole test class, the test method, and the expectation block inside the test method.
 * Some tests will use only one or two of these scopes, while others can take advantage of all
 * three.
 * <p/>
 * Usually, an actual mock object gets created and assigned to a declared mock field automatically,
 * without the test code having to do anything.
 * It is also possible, however, for the test itself to provide this instance, by declaring the
 * field as {@code final} and assigning to it the desired instance.
 * If no mock instance is necessary because only static methods or constructors will be called, then
 * this final field can receive the {@literal null} reference.
 * Mock parameters, on the other hand, will always receive a mock argument whenever the test method
 * is executed by the test runner.
 * <p/>
 * For each mocked type there is at least one <em>target class for mocking</em>, which is derived
 * from the declared type of the mock field or parameter, or specified through an annotation
 * attribute.
 * By default, all methods (including those which are {@code static}, {@code final},
 * {@code private}, {@code abstract}, or {@code native}) and constructors in the target classes will
 * be mocked; in addition, all static class initializers (if any, and including assignments to
 * non-constant static fields) will be stubbed out.
 * The only exceptions are the overrides (if any) for the following {@code java.lang.Object}
 * methods: {@code equals(Object)}, {@code hashCode()}, {@code toString()}, and {@code finalize()}.
 * For reasons of safety and also because mocking such methods isn't typically needed in real tests,
 * they are not mocked by default.
 * However, it is still possible to mock these methods by explicitly including them through one or
 * more {@linkplain #methods mock filters}.
 * <p/>
 * When a method or constructor is mocked, any invocations will not result in the execution of the
 * original code, but only in a call back to JMockit; according to the current phase of execution
 * for the test - <em>record</em>, <em>replay</em>, or <em>verify</em> - JMockit will then take the
 * appropriate actions.
 * <p/>
 * There are three rules that are applied when deriving the target classes from the mocked type:
 * <ol>
 * <li>The type is a concrete or <em>enum</em> class: this class and all its super-classes up to but
 * excluding {@code java.lang.Object} will be the target classes for mocking.</li>
 * <li>The type is an <em>interface</em> or <em>annotation</em>: a
 * {@linkplain java.lang.reflect.Proxy dynamic proxy class} is created and used as the only target.
 * </li>
 * <li>The type is an <em>abstract class</em>: a concrete subclass is generated with mock
 * implementations for the abstract methods in that class and in all of its super-classes (again,
 * excluding {@code Object}), which are also the target for mocking of the non-abstract methods and
 * constructors.</li>
 * </ol>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#declaration">Tutorial</a>
 *
 * @see #methods
 * @see #inverse
 * @see #capture
 * @see #constructorArgsMethod
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
    * Given a target class for mocking, only those methods and constructors which match at least one
    * filter will be mocked.
    * <p/>
    * If no other annotation attribute needs to be specified for a given mocked type, then for
    * convenience the value for this attribute can also be specified through the default
    * {@link #value} attribute, that is, by just supplying the list of mock filters without naming
    * any annotation attribute.
    * <p/>
    * Each mock filter must follow the syntax
    * <strong>{@code [nameRegex][(paramTypeName...)]}</strong>,
    * where {@code nameRegex} is a {@linkplain java.util.regex.Pattern regular expression} for
    * matching method names, and {@code paramTypeName} is the name of a primitive or reference
    * parameter type (actually, any suffix of the type name is enough, like "String" instead of the
    * full class name "java.lang.String").
    * If {@code nameRegex} is omitted the filter matches only constructors.
    * If {@code (paramTypeName...)} is omitted the filter matches methods with any parameters.
    * <p/>
    * If no filters are specified, then all methods and constructors declared in the target class
    * are mocked, and all static initializers are stubbed out.
    * <p/>
    * A filter containing just the empty string matches <em>no</em> methods, no constructors, and no 
    * static initializers of the target class; this can be used to obtain a mock instance where
    * nothing is mocked or stubbed out.
    * <p/>
    * The special filter {@code "<clinit>"} can be used to match the static initializers of the
    * target class. If only this filter is specified then the static initializers are stubbed out
    * and no methods or constructors are mocked; the opposite can be achieved by using the
    * {@link #inverse} attribute.
    * <p/>
    * For constructors, there is special syntax that can be used inside a mock filter to specify
    * which one of the constructors in the super-class is to be called by the mock constructor, when
    * those constructors are filtered out from mocking.
    * By default, one of the available constructors is chosen in an arbitrary way (the first in
    * declaration order, normally).
    * The special syntax actually comes in two forms:
    * <strong>{@code (paramTypeName...): (paramTypeNameForSuper...)}</strong>, where
    * {@code paramTypeNameForSuper} is the fully qualified (optional when the type belongs to
    * {@code java.lang}) type name for a constructor parameter in the super-class; and
    * <strong>{@code (paramTypeName...): n}</strong>, where {@code n} is the number of the
    * desired constructor from the super-class in declaration order, starting at 1 (one) for the
    * first declared constructor.
    */
   String[] methods() default {};

   /**
    * Indicates whether the mock filters are to be inverted or not. If inverted, only the methods
    * and constructors matching them are <strong>not</strong> mocked.
    */
   boolean inverse() default false;

   /**
    * Specifies the name of an instance method which will be used to determine which constructor to
    * call in the immediate super-class of the target class for mocking, and which arguments to pass
    * when instantiating the target class.
    * <p/>
    * For a mock field (defined either inside the test class or inside an expectation block) the
    * method must be defined inside the same class the field is.
    * For a mock parameter of a test method, the "constructor args method" must belong to the test
    * class.
    * <p/>
    * There must be only one method with this name, and its parameters must match those of the
    * desired constructor.
    * The return type of the method must be {@code Object[]}, with each element corresponding to a
    * constructor parameter.
    * <p/>
    * If the target class for mocking is {@code abstract}, then the constructor under consideration
    * will be one of those declared in the target class itself, instead of in its immediate
    * super-class.
    * <p/>
    * If this attribute is not specified, the argument values passed to the constructor in the
    * super-class will be the default values corresponding to its parameter types (if any), that is,
    * {@literal 0} for {@code int}, {@literal false} for {@code boolean}, and so on.
    */
   String constructorArgsMethod() default "";

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
    * It is valid to declare two or more mocks of the same type with a positive {@code capture}
    * number for each of them, say {@code n1}, {@code n2}, etc.
    * In this case, the first {@code n1} new instances will be assigned to the first field, the
    * following {@code n2} new instances to the second, and so on.
    * <p/>
    * Notice that this attribute does not apply to {@code final} mock fields, which cannot be
    * reassigned.
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
