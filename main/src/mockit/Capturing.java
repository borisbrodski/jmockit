/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates a test class, an instance field declared in a test class, or a test method parameter which will capture all
 * implementations of a given base type (usually an interface or abstract class).
 * <p/>
 * When applied to a test class, each class implementing/extending the explicitly specified
 * {@linkplain #baseType base type} will be stubbed out for the whole test class.
 * <p/>
 * When applied to an instance field or test method parameter, the declared type of the field or parameter will be
 * considered a mocked type, just as it would be if annotated with {@link Mocked @Mocked}.
 * Each class implementing (in case the mocked type is an interface) or extending (in case the mocked type is a class)
 * the mocked type will also be mocked.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/CapturingImplementations.html">Tutorial</a>
 * <br/>Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/integrationTests/SubclassTest.java">SubclassTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/CapturingImplementationsTest.java">CapturingImplementationsTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/TimingFramework/test/org/jdesktop/animation/timing/interpolation/PropertySetterTest.java">PropertySetterTest</a>
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
    * Any classes implementing this base type will be fully mocked or {@linkplain Mockit#stubOut(Class[]) stubbed out}
    * when loaded by the JVM during test execution (they are mocked if the annotation is applied to a field or
    * parameter, and stubbed out if applied to a test class).
    * Any already loaded implementations of the base type will also be mocked or stubbed out.
    * <p/>
    * This attribute is mandatory for test classes, and optional for instance fields and test method parameters.
    * In the case of fields and parameters, the base type will be the declared field or parameter type, if this
    * attribute is left unspecified.
    */
   Class<?> baseType() default Void.class;

   /**
    * When the annotation is applied to an instance field, this attribute specifies the maximum number of new instances
    * to <em>capture</em> (by assigning them to the field) while the test is running, between those instances which are
    * assignable to the mocked type and are created during the test.
    * <p/>
    * If {@code maxInstances} is zero (or negative), no instances created by a test are captured.
    * <p/>
    * If the value for this attribute is positive or unspecified (the default is {@code Integer.MAX_VALUE}), then
    * whenever an assignable instance is created during test execution and the specified number of new instances has not
    * been previously assigned, the (non-{@code final}) mock field will be assigned that new instance.
    * <p/>
    * It is valid to declare two or more fields of the same mocked type with a positive number of {@code maxInstances}
    * for each one of them, say {@code n1}, {@code n2}, etc.
    * In this case, the first {@code n1} new instances will be assigned to the first field, the following {@code n2} new
    * instances to the second, and so on.
    * <p/>
    * Notice that this attribute does not apply to {@code final} mock fields, which cannot be reassigned.
    */
   int maxInstances() default Integer.MAX_VALUE;

   /**
    * One or more implementation class filters.
    * Only classes which implement/extend the base type and match at least one filter will be considered.
    * <p/>
    * Each filter must contain a {@linkplain java.util.regex.Pattern regular expression} for matching fully qualified
    * class names.
    */
   String[] classNames() default {};

   /**
    * Indicates whether the implementation filters are to be inverted or not.
    * If inverted, only the classes matching them are <strong>not</strong> captured.
    */
   boolean inverse() default false;
}
