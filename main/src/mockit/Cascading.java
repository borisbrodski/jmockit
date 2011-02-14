/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates a {@linkplain Mocked mocked type} where the return types of non-{@code void} methods,
 * excluding primitives, {@code String}, and collection types, will be automatically mocked if and
 * when a invocation to the method occurs.
 * Instead of returning the default {@literal null} reference, such methods will return a mock
 * instance on which further invocations can be made.
 * This behavior automatically cascades to those mocked return types.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#cascading">In the Tutorial</a>
 * <p/>
 * Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/jbossaop/test/jbossaop/testing/bank/BankBusinessTest.java"
 * >BankBusinessTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/CascadingTest.java"
 * >CascadingTest</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Cascading
{
}