/*
 * JMockit Expectations
 * Copyright (c) 2009 JMockit Developers
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
 * This is a context object representing the current invocation to a mocked method or constructor.
 * When used as the type of the first parameter on a {@link Delegate} method, all invocations to the
 * delegate method will receive an appropriate instance.
 * <p/>
 * Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/DelegateInvocationTest.java"
 * >DelegateInvocationTest</a>
 */
public class Invocation
{
   private final int invocationCount;
   private int minInvocations;
   private int maxInvocations;

   protected Invocation(int invocationCount, int minInvocations, int maxInvocations)
   {
      this.invocationCount = invocationCount;
      this.minInvocations = minInvocations;
      this.maxInvocations = maxInvocations;
   }

   /**
    * Returns the current invocation count. The first invocation starts at 1 (one).
    */
   public final int getInvocationCount()
   {
      return invocationCount;
   }

   /**
    * Returns the index for the current invocation. The first invocation starts at 0 (zero).
    * Note that this is equivalent to {@link #getInvocationCount()} - 1.
    */
   public final int getInvocationIndex()
   {
      return invocationCount - 1;
   }

   /**
    * Returns the minimum invocation count for the current expectation.
    * <p/>
    * This call will return the value set with {@link Expectations#repeats(int)},
    * {@link Expectations#repeats(int, int)} and {@link Expectations#repeatsAtLeast(int)}, if that
    * was the case.
    */
   public final int getMinInvocations()
   {
      return minInvocations;
   }

   /**
    * Sets the minimum invocation count for the current expectation.
    * <p/>
    * This call can be used to override the value set with {@link Expectations#repeats(int)},
    * {@link Expectations#repeats(int, int)} or {@link Expectations#repeatsAtLeast(int)}.
    */
   public final void setMinInvocations(int minInvocations)
   {
      this.minInvocations = minInvocations;
   }

   /**
    * Returns the maximum invocation count for the current expectation (-1 indicates unlimited).
    * <p/>
    * This call will return the value set with {@link Expectations#repeats(int)},
    * {@link Expectations#repeats(int, int)} or {@link Expectations#repeatsAtMost(int)}, if that was
    * the case.
    */
   public final int getMaxInvocations()
   {
      return maxInvocations;
   }

   /**
    * Sets the maximum invocation count for the current expectation.
    * <p/>
    * This call can be used to override value set with {@link Expectations#repeats(int)},
    * {@link Expectations#repeats(int, int)} or {@link Expectations#repeatsAtMost(int)}.
    */
   public final void setMaxInvocations(int maxInvocations)
   {
      this.maxInvocations = maxInvocations;
   }
}
