/*
 * JMockit Expectations
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
 * For tests using {@link mockit.Expectations}, indicates a <em>non-strict</em> mocked type for an
 * instance field or test method parameter.
 * <p/>
 * While in the replay phase, invocations on the non-strict mocked type can be made in any number
 * and in any order.
 * For each of these invocations, the result (return value or thrown error/exception) will be either
 * a "no-op" (doing nothing for constructors and {@code void} methods, or returning the default
 * value appropriate to the return type) or whatever was specified through a matching invocation
 * executed in the record phase (matching on the parameter values, optionally using Hamcrest
 * matchers).
 * Note that multiple invocations to the same method or constructor can be recorded, provided
 * different arguments are used.
 * <p/>
 * Each recorded non-strict invocation still specifies an expectation, but without any invocation
 * count constraint to be satisfied. Therefore, a corresponding invocation in the replay phase will
 * not be required for the test to pass.
 * By default, any number of such corresponding invocations can occur in the replay phase for the
 * recorded invocation, in any order.
 * If a lower/upper limit or an exact number of invocations is expected, then the appropriate
 * constraint method should be used after recording the invocation.
 * <p/>
 * Invocations to non-strict mocks or mocked types that should occur in the replay phase can be
 * explicitly verified at the end of the test using a {@linkplain mockit.Verifications verification
 * block}.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#strictness">Tutorial</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface NonStrict
{
}
