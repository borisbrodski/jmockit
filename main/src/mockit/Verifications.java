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

import mockit.internal.state.*;
import mockit.internal.expectations.*;

/**
 * Base class whose subclasses are defined in test code, and whose instances define a set of
 * invocations on mocks to be verified against the actual invocations executed during the replay
 * phase of the test.
 * The order of the invocations is not relevant, and any subset of the potential invocations in the
 * replay phase can be verified (ie, not all of them need to be verified on each use of this class).
 * <p/>
 * Since each subclass will typically take the form of an anonymous class with no methods but only
 * an instance initialization block, we name such constructs <em>verification blocks</em>.
 * In the particular case of using this class directly, we have an <em>unordered</em> verification
 * block.
 * <p/>
 * Such blocks can appear alone in a test or in conjunction with
 * {@linkplain Expectations strict expectation blocks} which record at least one non-strict
 * expectation, and/or {@linkplain NonStrictExpectations non-strict expectation blocks}.
 * (Strict expectations are unconditionally and automatically verified as invocations occur in the
 * replay phase, and at the end of the test to account for any missing invocations.)
 * <p/>
 * Note that while an expectation block can appear only <em>before</em> the replay phase of the
 * test, a verification block can appear only <em>after</em> that phase.
 * <p/>
 * For an invocation inside a verification block to succeed (ie, pass verification), a
 * corresponding invocation must have occurred during the replay phase of the test, at least once.
 * Such an invocation may or may not have been previously recorded in an expectation block.
 * <p/>
 * Just like it is valid to have multiple expectation blocks in a test, it is also valid to have
 * multiple (non-nested) verification blocks. The relative order of the blocks is not relevant.
 * Such blocks can be of different types. (Typically, when using multiple verification blocks there
 * will be a mix of ordered and unordered ones.)
 *
 * @see Expectations#notStrict()
 * @see NonStrict
 * @see #Verifications()
 * @see #Verifications(int)
 */
public class Verifications extends Invocations
{
   final VerificationPhase verificationPhase;

   /**
    * Begins verification on the mocks invoked during the replay phase of the test.
    */
   protected Verifications()
   {
      this(false);
   }

   /**
    * Begins verification on the mocks invoked during the replay phase of the test, considering that
    * such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to
    * multiplying by that number the lower and upper invocation count limits for each invocation
    * inside the verification block (see {@link #repeats(int, int)}).
    * 
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * verified inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected Verifications(int numberOfIterations)
   {
      this(false);
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }

   Verifications(boolean inOrder)
   {
      RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest(true);
      verificationPhase = instance.startVerifications(inOrder);
   }

   @Override
   final VerificationPhase getCurrentPhase()
   {
      return verificationPhase;
   }

   /**
    * Specifies that the <strong>next</strong> invocation to be verified must never have happened
    * during the replay phase.
    * <p/>
    * This is like calling <code>repeats(0)</code> in the recording phase, with the difference that
    * it must be called just before the actual invocation to be verified.
    *
    * @deprecated Call <code>repeats(0)</code> after the invocation to be verified instead. This
    * method will be removed for release 1.0.
    */
   @Deprecated
   protected final void neverHappens()
   {
      verificationPhase.setNextInvocationNeverHappens(true);
   }
}
