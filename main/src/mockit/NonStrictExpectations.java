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
 * An {@link Expectations} subclass where all expectations are automatically
 * {@linkplain Expectations#notStrict() non-strict}.
 * <p/>
 * Such expectations will typically be later verified through a {@link Verifications} block,
 * executed after the replay phase of the test.
 *
 * @see NonStrict
 */
public class NonStrictExpectations extends Expectations
{
   /**
    * Identical to {@linkplain Expectations#Expectations() Expectations()}, except that all
    * expectations recorded will be {@linkplain #notStrict() non-strict} by default.
    */
   protected NonStrictExpectations()
   {
   }

   /**
    * Identical to {@linkplain Expectations#Expectations(Object...) Expectations(Object...)}, except
    * that all expectations recorded will be {@linkplain #notStrict() non-strict} by default.
    */
   protected NonStrictExpectations(Object... classesOrObjectsToBePartiallyMocked)
   {
      super(classesOrObjectsToBePartiallyMocked);
   }

   /**
    * Identical to {@linkplain Expectations#Expectations(Object...) Expectations(Object...)}, except
    * that all expectations recorded will be {@linkplain #notStrict() non-strict} by default.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * multiplying by that number the lower and upper invocation count limits for each invocation
    * inside the expectation block (see {@link #repeats(int, int)}).
    * Note that by default the invocation count range for a non-strict expectation is [0, ∞), that
    * is, a lower limit of 0 (zero) and no upper limit, so the number of iterations will only be
    * meaningful if a positive and finite limit is explicitly specified for the expectation.
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * recorded inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected NonStrictExpectations(
      int numberOfIterations, Object... classesOrObjectsToBePartiallyMocked)
   {
      super(classesOrObjectsToBePartiallyMocked);
      getCurrentPhase().setNumberOfIterations(numberOfIterations);
   }
}
