/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

/**
 * A combination of {@link FullVerifications} and {@link VerificationsInOrder}.
 * <p/>
 * Note that the behavior provided by this class is essentially the same obtained through an strict
 * {@link Expectations} recording block, except that the number of expected invocations for each
 * expectation is still "non-strict".
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#FullVerificationInOrder">In the Tutorial</a>
 */
public abstract class FullVerificationsInOrder extends Verifications
{
   /**
    * Begins <em>in-order</em> verification for <em>all</em> invocations on the mocked types/instances that can
    * potentially be invoked during the replay phase.
    */
   protected FullVerificationsInOrder()
   {
      super(true);
      verificationPhase.setAllInvocationsMustBeVerified();
   }

   /**
    * Begins <em>in-order</em> verification for <em>all</em> mocked types/instances invoked during the replay phase of
    * the test, considering that such invocations occurred in a given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to duplicating (like in "copy &
    * paste") the whole sequence of invocations in the block.
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations verified inside the
    * block; when not specified, 1 (one) iteration is assumed
    */
   protected FullVerificationsInOrder(int numberOfIterations)
   {
      super(true);
      verificationPhase.setAllInvocationsMustBeVerified();
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }

   /**
    * Same as {@link #FullVerificationsInOrder()}, but restricting the verification to the specified mocked types and/or
    * mocked instances.
    *
    * @param mockedTypesAndInstancesToVerify one or more of the mocked types (ie, {@code Class} objects) and/or mocked
    * instances that are in scope for the test; for a given mocked <em>instance</em>, all classes up to (but not
    * including) {@code java.lang.Object} are considered
    */
   protected FullVerificationsInOrder(Object... mockedTypesAndInstancesToVerify)
   {
      this();
      verificationPhase.setMockedTypesToFullyVerify(mockedTypesAndInstancesToVerify);
   }

   /**
    * Same as {@link #FullVerificationsInOrder(int)}, but restricting the verification to the specified mocked types
    * and/or mocked instances.
    *
    * @param mockedTypesAndInstancesToVerify one or more of the mocked types (ie, {@code Class} objects) and/or mocked
    * instances that are in scope for the test; for a given mocked <em>instance</em>, all classes up to (but not
    * including) {@code java.lang.Object} are considered
    */
   protected FullVerificationsInOrder(int numberOfIterations, Object... mockedTypesAndInstancesToVerify)
   {
      this(numberOfIterations);
      verificationPhase.setMockedTypesToFullyVerify(mockedTypesAndInstancesToVerify);
   }
}
