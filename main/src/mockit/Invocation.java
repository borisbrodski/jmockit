/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

/**
 * A context object representing the current invocation to a mocked method or constructor.
 * <p/>
 * When used as the type of the first parameter on a {@link Delegate} method, all invocations to the delegate method
 * will receive an appropriate instance.
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
   protected Invocation(Object invokedInstance, int invocationCount, int minInvocations, int maxInvocations)
   {
      this.invokedInstance = invokedInstance;
      this.invocationCount = invocationCount;
      this.minInvocations = minInvocations;
      this.maxInvocations = maxInvocations;
   }

   /**
    * Returns the target instance on which the current invocation was made, if any (if the method invoked is
    * {@code static} then there is no instance and {@literal null} is returned).
    * <p/>
    * Note that this instance can either be the mocked instance originally created by JMockit and assigned to a mock
    * field or passed as a mock parameter, or an instance created by the code under test.
    */
   public final <T> T getInvokedInstance()
   {
      //noinspection unchecked
      return (T) invokedInstance;
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
    * This call will return the value specified through the {@link Expectations#times} or {@link Expectations#minTimes}
    * field, if that was the case.
    */
   public final int getMinInvocations()
   {
      return minInvocations;
   }

   /**
    * Sets the minimum invocation count for the current expectation.
    * <p/>
    * This call can be used to override the value set on the {@link Expectations#times} or {@link Expectations#minTimes}
    * field.
    */
   public final void setMinInvocations(int minInvocations)
   {
      this.minInvocations = minInvocations;
   }

   /**
    * Returns the maximum invocation count for the current expectation (-1 indicates unlimited).
    * <p/>
    * This call will return the value specified through the {@link Expectations#times} or {@link Expectations#maxTimes}
    * field, if that was the case.
    */
   public final int getMaxInvocations()
   {
      return maxInvocations;
   }

   /**
    * Sets the maximum invocation count for the current expectation.
    * <p/>
    * This call can be used to override the value set on the {@link Expectations#times} or {@link Expectations#maxTimes}
    * field.
    */
   public final void setMaxInvocations(int maxInvocations)
   {
      this.maxInvocations = maxInvocations;
   }
}
