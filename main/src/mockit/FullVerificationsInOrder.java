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

/**
 * A combination of {@link FullVerifications} and {@link VerificationsInOrder}.
 * <p/>
 * Note that the behavior provided by this class is essentially the same obtained through an strict
 * {@link Expectations} recording block, except that the number of expected invocations for each
 * expectation is still "non-strict".
 */
public class FullVerificationsInOrder extends Verifications
{
   /**
    * Begins <em>in-order</em> verification for <em>all</em> invocations on the mocked types that
    * can potentially be invoked during the replay phase.
    */
   public FullVerificationsInOrder()
   {
      super(true);
      verificationPhase.setAllInvocationsMustBeVerified();
   }

   /**
    * Begins <em>in-order</em> verification for <em>all</em> mocks invoked during the replay phase
    * of the test, considering that such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * duplicating (like in "copy & paste") the whole sequence of invocations in the block.
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * verified inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected FullVerificationsInOrder(int numberOfIterations)
   {
      super(true);
      verificationPhase.setAllInvocationsMustBeVerified();
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }
}
