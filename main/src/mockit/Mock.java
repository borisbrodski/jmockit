/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Used inside a <em>mock class</em> to indicate a <em>mock method</em>, with optional <em>expectations</em> on the
 * number of invocations while the mock is in effect.
 * (A mock will be in effect from the time a real method/constructor is mocked to the time it is restored to its
 * original definition.)
 * <p/>
 * This annotation can also be applied to the special methods <strong>{@code void $init(...)}</strong> and
 * <strong>{@code void $clinit()}</strong>, which correspond to constructors and to class initializers, respectively.
 * In the latter case, this is the only way to mock a static <em>class initialization block</em>.
 * (Notice that it makes no difference if the real class contains more than one static initialization block, because the
 * Java compiler will always merge the sequence of static blocks into a single internal "&lt;clinit>" static method in
 * the class file.)
 * Mock methods named {@code $init} will apply to the corresponding constructor in the real class, by matching the
 * declared parameters.
 * <p/>
 * Finally, a note about <em>instance initialization blocks</em>. The Java compiler does not preserve instance
 * initializers as separate elements in the class file, instead inserting any statements in such blocks into each and
 * every constructor, right after the necessary call to the super-class constructor.
 * Therefore, it is not possible to separately mock instance initialization blocks.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html#mocks">Tutorial</a>
 *
 * @see #invocations
 * @see #minInvocations
 * @see #maxInvocations
 * @see #reentrant
 * @see MockClass
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mock
{
   /**
    * Number of expected invocations of the mock method.
    * If 0 (zero), no invocations will be expected.
    * A negative value (the default) means there is no expectation on the number of invocations;
    * that is, the mock can be called any number of times or not at all during any test which uses it.
    * <p/>
    * A non-negative value is equivalent to setting {@link #minInvocations} and  {@link #maxInvocations} to that same
    * value.
    */
   int invocations() default -1;

   /**
    * Minimum number of expected invocations of the mock method, starting from 0 (zero, which is the default).
    * 
    * @see #invocations
    * @see #maxInvocations
    */
   int minInvocations() default 0;

   /**
    * Maximum number of expected invocations of the mock method, if positive.
    * If zero the mock is not expected to be called at all.
    * A negative value (the default) means there is no expectation on the maximum number of invocations.
    * 
    * @see #invocations
    * @see #minInvocations
    */
   int maxInvocations() default -1;

   /**
    * Indicates whether or not the mock method implementation is allowed to call the corresponding real method on the
    * {@code it} field before it returns from an execution.
    * (The {@code it} field is an instance field defined in the mock class with name "it" and of the same type as the
    * mocked class, which will be set to the real class instance for each indirect call to a mock method.)
    * By default, such reentrant calls are not allowed because they lead to infinite recursion, with the mock method
    * calling itself indirectly through the mocked method.
    * <p/>
    * When allowed to make such calls, the mock method effectively behaves as <em>advice</em> to the corresponding real
    * method.
    */
   boolean reentrant() default false;
}
