/*
 * JMockit Annotations
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

import java.lang.annotation.*;

/**
 * Indicates a mock class containing one or more mock methods/constructors, each one of them
 * properly indicated as {@linkplain Mock such}.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html#MockClass">Tutorial</a>
 *
 * @see #realClass
 * @see #stubs
 * @see #inverse
 * @see #instantiation
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MockClass
{
   /**
    * The real class whose methods/constructors will be redefined with the corresponding
    * methods/constructors in the mock class.
    * <p/>
    * Alternatively, the {@code Class} object provided can point to an interface.
    * In that case, a proxy implementation class will be created and then used as the target class
    * for mocking.
    * The only way for a test to use such a mocked class will be through the proxy instance returned
    * by {@link Mockit#setUpMock(Object)}, so it only makes sense to use an interface for this
    * attribute when the mock class is set up through that method.
    */
   Class<?> realClass();

   /**
    * One or more stubbing filters which specify the set of methods and constructors in the real
    * class that are to be stubbed out with empty implementations.
    * <p/>
    * Each filter must follow the syntax <strong>{@code [nameRegex][(paramTypeName...)]}</strong>,
    * where {@code nameRegex} is a {@linkplain java.util.regex.Pattern regular expression} for
    * matching method names, and {@code paramTypeName} is the name of a primitive or reference
    * parameter type. Actually, any <em>suffix</em> of the type name is enough, like "String"
    * instead of the full class name "java.lang.String".
    * If {@code nameRegex} is omitted the filter matches only constructors.
    * If {@code (paramTypeName...)} is omitted the filter matches methods with any parameters.
    * <p/>
    * Note that an empty filter ({@code stubs = ""}) will match <em>no</em> methods or constructors
    * in the real class, or <em>all</em> methods and constructors if used with
    * {@code inverse = true}.
    * <p/>
    * To specify the static initializers of the class, inform the filter "&lt;clinit>".
    */
   String[] stubs() default {};

   /**
    * Indicates whether the stubbing filters are to be inverted or not. If inverted, only the
    * methods and constructors matching them are <strong>not</strong> stubbed out.
    */
   boolean inverse() default false;

   /**
    * Specifies when instances of the mock class should be created:
    * {@linkplain Instantiation#PerMockSetup every time the mock class is set up},
    * {@linkplain Instantiation#PerMockInvocation every time an instance mock method is called}, or
    * {@linkplain Instantiation#PerMockedInstance for each mocked instance of the real class}.
    * <p/>
    * If not specified, a mock instance will be created
    * {@linkplain Instantiation#PerMockInvocation for each mock invocation}.
    */
   Instantiation instantiation() default Instantiation.PerMockInvocation;
}
