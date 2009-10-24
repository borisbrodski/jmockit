/*
 * JMockit Verifications
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

/**
 * Same as {@link Verifications}, but checking that all invocations in the replay phase are
 * explicitly verified in this verification block.
 * This way, the verification block represents a full set of verifications for the mocked types
 * used in the test.
 * <p/>
 * Any invocation in the replay phase not covered by one of these verifications will cause an
 * assertion error to be thrown.
 * <p/>
 * Note that the behavior provided by this class is basically the same obtained with
 * {@linkplain Expectations strict expectations} (where no unexpected invocations are allowed),
 * except for the ordering and the number of occurrences of invocations, which are both still
 * "non-strict".
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#FullVerification">Tutorial</a>
 */
public class FullVerifications extends Verifications
{
   /**
    * Begins <em>full</em> verification on the mocked types that can potentially be invoked during
    * the replay phase of the test.
    */
   protected FullVerifications()
   {
      verificationPhase.setAllInvocationsMustBeVerified();
   }

   /**
    * Begins <em>full</em> verification on the mocks invoked during the replay phase of the test,
    * considering that such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * multiplying by that number the lower and upper invocation count limits for each invocation
    * inside the verification block (see {@link #repeats(int, int)}).
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * verified inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected FullVerifications(int numberOfIterations)
   {
      super(numberOfIterations);
      verificationPhase.setAllInvocationsMustBeVerified();
   }
}
