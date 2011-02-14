/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

/**
 * An {@link Expectations} subclass where all expectations are automatically
 * {@linkplain Expectations#notStrict() non-strict}.
 * <p/>
 * Such expectations will typically be later verified through a {@link Verifications} block, when executed after the
 * replay phase of the test.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#strictness">In the Tutorial</a>
 *
 * @see NonStrict
 */
public abstract class NonStrictExpectations extends Expectations
{
   /**
    * Identical to the corresponding super-constructor {@link Expectations#Expectations()}, except that all expectations
    * recorded will be {@linkplain #notStrict() non-strict} by default.
    *
    * @see #NonStrictExpectations(Object...)
    * @see #NonStrictExpectations(int, Object...)
    */
   protected NonStrictExpectations()
   {
   }

   /**
    * Identical to the corresponding super-constructor {@link Expectations#Expectations(Object...)}, except that all
    * expectations recorded will be {@linkplain #notStrict() non-strict} by default.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#dynamicPartial">In the Tutorial</a>
    *
    * @see #NonStrictExpectations()
    * @see #NonStrictExpectations(int, Object...)
    */
   protected NonStrictExpectations(Object... classesOrObjectsToBePartiallyMocked)
   {
      super(classesOrObjectsToBePartiallyMocked);
   }

   /**
    * Identical to the corresponding super-constructor {@link Expectations#Expectations(int, Object...)}, except that
    * all expectations recorded will be {@linkplain #notStrict() non-strict} by default.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to multiplying by that number
    * the lower and upper invocation count limits for each invocation inside the expectation block.
    * Note that by default the invocation count range for a non-strict expectation is [0, ∞), that is, a lower limit of
    * 0 (zero) and no upper limit, so the number of iterations will only be meaningful if a positive and finite limit is
    * explicitly specified for the expectation.
    * <p/>
    * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#iteratedExpectations">In the Tutorial</a>
    *
    * @param numberOfIterations the positive number of iterations for the whole set of invocations recorded inside the
    * block; when not specified, 1 (one) iteration is assumed
    *
    * @see #NonStrictExpectations()
    * @see #NonStrictExpectations(Object...)
    */
   protected NonStrictExpectations(int numberOfIterations, Object... classesOrObjectsToBePartiallyMocked)
   {
      super(classesOrObjectsToBePartiallyMocked);
      getCurrentPhase().setNumberOfIterations(numberOfIterations);
   }
}
