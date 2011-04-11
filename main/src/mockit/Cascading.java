/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates a {@linkplain Mocked mocked type} where methods which return a reference type, excluding {@code String},
 * primitive wrappers, and collection types, will be automatically mocked if and when an invocation to the method
 * occurs.
 * Instead of returning the default {@literal null} reference, such methods will return a mocked instance of the return
 * type on which further invocations can be made.
 * This behavior automatically cascades to those mocked return types.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/BehaviorBasedTesting.html#cascading">In the Tutorial</a>
 * <p/>
 * Sample tests:
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/samples/jbossaop/test/jbossaop/testing/bank/BankBusinessTest.java"
 * >BankBusinessTest</a>,
 * <a href="http://code.google.com/p/jmockit/source/browse/trunk/main/test/mockit/CascadingFieldTest.java"
 * >CascadingFieldTest</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Cascading
{
}