/*
 * JMockit
 * Copyright (c) 2009-2010 JMockit Developers
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
 * A context object representing the current invocation to a mocked method or constructor.
 * <p/>
 * When used as the type of the first parameter on a {@link Delegate} method, all invocations to the
 * delegate method will receive an appropriate instance.
 * Similarly, it can be used in the handler method of an instance assigned to the
 * {@linkplain mockit.Invocations#forEachInvocation forEachInvocation} field.
 * <p/>
 * Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/DelegateInvocationTest.java"
 * >DelegateInvocationTest</a>
 */
public class Invocation
{
   private final Object invokedInstance;
   private final int invocationCount;
   private int minInvocations;
   private int maxInvocations;

   /**
    * For internal use only.
    */
   protected Invocation(
      Object invokedInstance, int invocationCount, int minInvocations, int maxInvocations)
   {
      this.invokedInstance = invokedInstance;
      this.invocationCount = invocationCount;
      this.minInvocations = minInvocations;
      this.maxInvocations = maxInvocations;
   }

   /**
    * Returns the target instance on which the current invocation was made, if any (if the method
    * invoked is {@code static} then there is no instance and {@literal null} is returned).
    * <p/>
    * Note that this instance can either be a mock instance (for example, the instance automatically
    * created by JMockit and assigned to a mock field or passed as the argument value for a mock
    * parameter), or a "real" instance created by code under test for a mocked type.
    */
   public final Object getInvokedInstance()
   {
      return invokedInstance;
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
    * This call will return the value specified through the {@link Expectations#times} or
    * {@link Expectations#minTimes} field, if that was the case.
    */
   public final int getMinInvocations()
   {
      return minInvocations;
   }

   /**
    * Sets the minimum invocation count for the current expectation.
    * <p/>
    * This call can be used to override the value set on the {@link Expectations#times} or
    * {@link Expectations#minTimes} field.
    */
   public final void setMinInvocations(int minInvocations)
   {
      this.minInvocations = minInvocations;
   }

   /**
    * Returns the maximum invocation count for the current expectation (-1 indicates unlimited).
    * <p/>
    * This call will return the value specified through the {@link Expectations#times} or
    * {@link Expectations#maxTimes} field, if that was the case.
    */
   public final int getMaxInvocations()
   {
      return maxInvocations;
   }

   /**
    * Sets the maximum invocation count for the current expectation.
    * <p/>
    * This call can be used to override the value set on the {@link Expectations#times} or
    * {@link Expectations#maxTimes} field.
    */
   public final void setMaxInvocations(int maxInvocations)
   {
      this.maxInvocations = maxInvocations;
   }
}
