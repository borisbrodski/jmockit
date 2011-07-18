/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

/**
 * A context object representing the current invocation to a mocked method/constructor, to be passed as the
 * <em>first</em> parameter of the corresponding <em>mock method</em> implementations.
 * <p/>
 * With the <em>Expectations & Verifications</em> API, this parameter can appear in mock methods implemented in
 * {@link Delegate} classes or in validation objects assigned to the
 * {@link Invocations#forEachInvocation forEachInvocation} field.
 * With the <em>Mockups</em> API, it can appear in {@link Mock} methods.
 * <p/>
 * Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/DelegateInvocationTest.java"
 * >DelegateInvocationTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/MockInvocationTest.java"
 * >MockInvocationTest</a>
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
    * Returns the instance on which the current invocation was made, or {@code null} for a {@code static} method
    * invocation.
    */
   public final <T> T getInvokedInstance()
   {
      //noinspection unchecked
      return (T) invokedInstance;
   }

   /**
    * Returns the current invocation count. The first invocation starts at 1 (one).
    */
   public final int getInvocationCount() { return invocationCount; }

   /**
    * Returns the index for the current invocation. The first invocation starts at 0 (zero).
    * Note that this is equivalent to {@link #getInvocationCount()} - 1.
    */
   public final int getInvocationIndex() { return invocationCount - 1; }

   /**
    * Returns the current value of the minimum invocation count associated with the matching expectation or mock method.
    * <p/>
    * For an expectation, this call will return the value specified through the
    * {@linkplain Invocations#times times} or {@linkplain Invocations#minTimes minTimes} field, if that was the case;
    * if not, the value will be {@code 0} for a non-strict expectation and {@code 1} for a strict expectation.
    * For a {@code @Mock} method, it will return the value specified for the {@linkplain Mock#invocations invocations}
    * or {@linkplain Mock#minInvocations minInvocations} attribute, or {@code 0} if none.
    */
   public final int getMinInvocations() { return minInvocations; }

   /**
    * Sets the current value of the minimum invocation count for the matching expectation or mock method.
    * <p/>
    * For an expectation, this call can be used to override the value set on the {@linkplain Invocations#times times} or
    * {@linkplain Invocations#minTimes minTimes} field.
    * For a {@code @Mock} method, it would override the value specified in the
    * {@linkplain Mock#invocations invocations} or {@linkplain Mock#minInvocations minInvocations} attribute.
    */
   public final void setMinInvocations(int minInvocations)
   {
      this.minInvocations = minInvocations;
      onChange();
   }

   /**
    * Returns the current value of the maximum invocation count for the matching expectation or mock method ({@code -1}
    * indicates that it's unlimited).
    * <p/>
    * For an expectation, this call will return the value specified through the
    * {@linkplain Invocations#times times} or {@linkplain Invocations#maxTimes maxTimes} field, if that was the case;
    * if not, the value will be {@code -1} for a non-strict expectation and {@code 1} for a strict expectation.
    * For a {@code @Mock} method, it will return the value specified for the {@linkplain Mock#invocations invocations}
    * or {@linkplain Mock#maxInvocations maxInvocations} attribute, or {@code -1} if none.
    */
   public final int getMaxInvocations() { return maxInvocations; }

   /**
    * Sets the current value of the maximum invocation count for the matching expectation or mock method.
    * The value of {@code -1} implies no upper limit.
    * <p/>
    * For an expectation, this call can be used to override the value set on the {@linkplain Invocations#times times} or
    * {@linkplain Invocations#maxTimes maxTimes} field.
    * For a {@code @Mock} method, it would override the value specified in the
    * {@linkplain Mock#invocations invocations} or {@linkplain Mock#maxInvocations maxInvocations} attribute.
    */
   public final void setMaxInvocations(int maxInvocations)
   {
      this.maxInvocations = maxInvocations;
      onChange();
   }

   /**
    * For internal use only.
    */
   protected void onChange() {}
}
