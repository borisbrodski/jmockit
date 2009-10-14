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
 * Indicates a test class, an instance field declared in a test class, or a test method parameter
 * which will capture all implementations of a given base type (usually an interface or abstract
 * class).
 * <p/>
 * When applied to a test class, each class implementing/extending the explicitly specified
 * {@linkplain #baseType base type} will be stubbed out for the whole test class.
 * <p/>
 * When applied to an instance field or test method parameter, the declared type of the field or
 * parameter will be considered a mocked type, just as it would be if annotated with
 * {@link Mocked @Mocked}.
 * Each class implementing (in case the mocked type is an interface) or extending (in case the
 * mocked type is a class) the mocked type will also be mocked.
 * <p/>
 * Examples:
 * <a href="https://jmockit.dev.java.net/source/browse/jmockit/main/test/integrationTests/SubclassTest.java?view=markup">SubclassTest</a>
 * <a href="https://jmockit.dev.java.net/source/browse/jmockit/main/test/mockit/CapturingImplementationsTest.java?view=markup">CapturingImplementationsTest</a>
 * <a href="https://jmockit.dev.java.net/source/browse/jmockit/samples/TimingFramework/test/org/jdesktop/animation/timing/interpolation/PropertySetterTest.java?view=markup">PropertySetterTest</a>
 *
 * @see #classNames
 * @see #inverse
 * @see #maxInstances
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Capturing
{
   /**
    * Specifies the base interface/class type whose implementations to capture at runtime.
    * <p/>
    * Any classes implementing this base type will be fully {@linkplain Mockit#stubOut(Class[])
    * stubbed out} or mocked when loaded by the JVM during test execution.
    * Any already loaded implementations of the base type will also be stubbed out or mocked.
    * <p/>
    * This attribute is mandatory for test classes, and optional for instance fields and test method
    * parameters. In the case of fields and parameters, the base type will be the declared field or
    * parameter type, if this attribute is left unspecified.
    */
   Class<?> baseType() default Void.class;

   /**
    * When the annotation is applied to an instance field, this attribute specifies the maximum
    * number of new instances to <em>capture</em> (by assigning them to the field) while the test is
    * running, between those instances which are assignable to the mocked type and are created
    * during the test.
    * <p/>
    * If <code>capture</code> is zero (or negative), no instances created by a test are captured.
    * <p/>
    * If the value for this attribute is positive, then whenever an assignable instance is created
    * during test execution and the specified number of new instances has not been previously
    * assigned, the (non-<code>final</code>) mock field will be assigned that new instance.
    * <p/>
    * It is valid to declare two or more mocks of the same type with a positive <code>capture</code>
    * number for each of them, say <code>n1</code>, <code>n2</code>, etc.
    * In this case, the first <code>n1</code> new instances will be assigned to the first field,
    * the following <code>n2</code> new instances to the second, and so on.
    * <p/>
    * Notice that this attribute does not apply to <code>final</code> mock fields, which cannot be
    * reassigned.
    */
   int maxInstances() default Integer.MAX_VALUE;
   // TODO: implement this attribute

   /**
    * One or more implementation class filters.
    * Only classes which implement/extend the base type and match at least one filter will be
    * considered.
    * <p/>
    * Each filter must contain a {@linkplain java.util.regex.Pattern regular expression} for
    * matching fully qualified class names.
    */
   String[] classNames() default {};

   /**
    * Indicates whether the implementation filters are to be inverted or not.
    * If inverted, only the classes matching them are <strong>not</strong> captured.
    */
   boolean inverse() default false;
}
