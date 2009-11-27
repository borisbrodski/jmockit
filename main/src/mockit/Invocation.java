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
 * This is a context object representing the current invocation. When setup as a
 * first parameter on a {@linkplain Delegate} method, the method receives and
 * able to manipulate the context.
 * <p/>
 * Sample tests: <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/DelegateInvocationTest.java"
 * >DelegateInvocationTest</a>,
 * 
 * @see Expectations
 */
public class Invocation
{
   /**
    * Current invocation count. The first invocation start at 1.
    */
   final protected int invocationCount;

   /**
    * Minimum invocation count
    */
   protected int minInvocations;

   /**
    * Maximum invocation count (-1 indicates unlimited).
    */
   protected int maxInvocations;

   /**
    * Constructor intended for internal use.
    */
   protected Invocation(int invocationCount) {
      this.invocationCount=invocationCount;
   }

   /**
    * Returns invocation count (non-modifiable). The first invocation start at 1.
    */
   final public int getInvocationCount()
   {
      return invocationCount;
   }

   /**
    * Returns minimum invocation count.
    * <p/>
    * This call returns value set
    * with {@link Expectations#repeats(int)}, {@link Expectations#repeats(int, int)}
    * and {@link Expectations#repeatsAtLeast(int)} methods.
    */
   final public int getMinInvocations()
   {
      return minInvocations;
   }

   /**
    * Sets minimum invocation count.
    * This call can be used to override value set
    * with {@link Expectations#repeats(int)}, {@link Expectations#repeats(int, int)}
    * and {@link Expectations#repeatsAtLeast(int)} methods.
    */
   final public void setMinInvocations(int minInvocations)
   {
      this.minInvocations = minInvocations;
   }

   /**
    * Returns maximum invocation count.
    * <p/>
    * This call returns value set
    * with {@link Expectations#repeats(int)}, {@link Expectations#repeats(int, int)}
    * and {@link Expectations#repeatsAtMost(int)} methods.
    */
   final public int getMaxInvocations()
   {
      return maxInvocations;
   }

   /**
    * Sets maximum invocation count.
    * <p/>
    * This call can be used to override value set
    * with {@link Expectations#repeats(int)}, {@link Expectations#repeats(int, int)}
    * and {@link Expectations#repeatsAtMost(int)} methods.
    */
   final public void setMaxInvocations(int maxInvocations)
   {
      this.maxInvocations = maxInvocations;
   }
}
