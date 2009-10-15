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
 * Used inside a <em>mock class</em> to indicate a <em>mock method</em> or <em>mock
 * constructor</em>, with optional <em>expectations</em> on the number of invocations while the mock
 * is in effect.
 * (A mock will be in effect from the time a real method/constructor is mocked to the time it is
 * restored to its original definition.)
 * <p/>
 * This annotation can also be applied to the special methods <strong><code>void
 * $init(...)</code></strong> and <strong><code>void $clinit()</code></strong>, which correspond to
 * constructors and to class initializers, respectively.
 * <p/>
 * In the latter case, this is the only way to mock a static <em>class initialization block</em>.
 * Notice that it makes no difference if the real class contains more than one static initialization
 * block, because the Java compiler will always merge the sequence of static blocks into a single
 * internal "&lt;clinit>" static method in the class file.
 * <p/>
 * Mock methods named <code>$init</code> will apply to the corresponding constructor (by taking into
 * consideration the parameters) in the real class. The advantages of mocking a constructor this way
 * instead of using an actual mock constructor are three: 1) the <code>it</code> field can be used
 * in the <code>$init</code> method, while in a mock constructor such field would not be set before
 * the mock constructor was called; 2) the <code>$init</code> method will be called on the mock
 * instance if one is provided by the test, contrary to a mock constructor which is always called on
 * a new mock instance; and 3) the <code>$init</code> method can be static, which a constructor
 * cannot.
 * <p/>
 * Finally, a note about <em>instance initialization blocks</em>. The Java compiler does not
 * preserve instance initializers as separate elements in the class file, instead inserting any
 * statements in such blocks into each and every constructor, right after the necessary call to the
 * superclass constructor. Therefore, it is not possible to separately mock instance initialization
 * blocks.
 *
 * @see mockit.MockClass
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Mock
{
   /**
    * Number of expected invocations of the mock method/constructor.
    * If 0 (zero), no invocations will be expected.
    * A negative value (the default) means there is no expectation on the number of invocations;
    * that is, the mock can be called any number of times or not at all during any test which uses
    * it.
    * <p/>
    * A non-negative value is equivalent to setting {@linkplain #minInvocations} and {@linkplain
    * #maxInvocations} to that same value.
    */
   int invocations() default -1;

   /**
    * Minimum number of expected invocations of the mock method/constructor, starting from 0
    * (zero, which is the default).
    */
   int minInvocations() default 0;

   /**
    * Maximum number of expected invocations of the mock method/constructor, if positive.
    * If zero the mock is not expected to be called at all.
    * A negative value (the default) means there is no expectation on the maximum number of
    * invocations.
    */
   int maxInvocations() default -1;

   /**
    * Indicates whether or not the mock method implementation is allowed to call the corresponding
    * real method on the <code>it</code> field before it returns from an execution.
    * (The <code>it</code> field is an instance field defined in the mock class with name "it" and
    * of the same type as the mocked class, which will be set to the real class instance for each
    * indirect call to a mock method.)
    * By default, such reentrant calls are not allowed because they lead to infinite recursion, with
    * the mock method calling itself indirectly through the mocked method.
    * <p/>
    * When allowed to make such calls, the mock method effectively behaves as <em>advice</em> to the
    * corresponding real method.
    */
   boolean reentrant() default false;
}
