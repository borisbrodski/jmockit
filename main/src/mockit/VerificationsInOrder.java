/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import mockit.internal.expectations.*;

/**
 * Same as {@link Verifications}, but checking that invocations in the replay phase occurred in the same order as
 * specified in this <em>ordered</em> verification block.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#VerificationInOrder">In the
 * Tutorial</a>
 *
 * @see #VerificationsInOrder()
 * @see #VerificationsInOrder(int)
 * @see #unverifiedInvocations()
 * @see #verifiedInvocations(Verifications)
 */
public abstract class VerificationsInOrder extends Verifications
{
   /**
    * Begins <em>in-order</em> verification on the mocked types/instances invoked during the replay phase of the test.
    */
   protected VerificationsInOrder()
   {
      super(true);
   }

   /**
    * Begins <em>in-order</em> verification on the mocked types/instances invoked during the replay phase of the test.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to duplicating (like in "copy &
    * paste") the whole sequence of invocations in the block.
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations
    * verified inside the block; when not specified, 1 (one) iteration is assumed
    */
   protected VerificationsInOrder(int numberOfIterations)
   {
      super(true);
      verificationPhase.setNumberOfIterations(numberOfIterations);
   }

   /**
    * Accounts for a sequence of non-strict invocations executed in the replay phase that are not explicitly verified
    * in this block.
    * Note that even the invocations that are implicitly verified (due to a minimum invocation count specified in the
    * <em>record</em> phase) will be accounted for (if any), since their replay order cannot be verified otherwise.
    * (Obviously, this doesn't apply to <em>strict</em> invocations, whose replay order is always verified implicitly.)
    * <p/>
    * Which invocations belong or not in this sequence depends on the relative position of the call to this method with
    * respect to the explicitly verified invocations in the same block.
    * <p/>
    * This can be used to verify that one or more consecutive invocations occurred <em>before</em> others, and
    * conversely to verify that one or more consecutive invocations occurred <em>after</em> others.
    * The call to this method marks the position where the unverified invocations are expected to have occurred,
    * relative to the explicitly verified ones.
    * <p/>
    * Each sequence of explicit verifications in the block will correspond to a <em>consecutive</em> sequence of
    * invocations in the replay phase of the test.
    * So, if this method is called more than once from the same verification block, then an arbitrary sequence of
    * consecutive invocations between said calls will be verified.
    * Notice that when this method is not used, the invocations in the replay phase need <em>not</em> be consecutive,
    * but only have the same relative order as the verification calls.
    * <p/>
    * Finally, notice that you can combine an ordered block that verifies the position of some calls relative to others,
    * with a previous unordered block which verifies some or all of those other invocations.
    *
    * @see #verifiedInvocations(Verifications)
    * @see <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#partiallyOrdered">In
    * the Tutorial</a>
    */
   protected final void unverifiedInvocations()
   {
      ((OrderedVerificationPhase) verificationPhase).fixPositionOfUnverifiedExpectations();
   }

   /**
    * Accounts for a sequence of non-strict invocations executed in the replay phase that have already been explicitly
    * verified in a previous verification block.
    *
    * @param alreadyVerified an unordered verification block describing a group of already verified invocations
    *
    * @throws IllegalArgumentException if the given verifications are ordered
    *
    * @see #unverifiedInvocations()
    * @see <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#partiallyOrdered">In
    * the Tutorial</a>
    */
   protected final void verifiedInvocations(Verifications alreadyVerified)
   {
      ((OrderedVerificationPhase) verificationPhase).checkOrderOfVerifiedInvocations(
         alreadyVerified.verificationPhase);
   }
}
