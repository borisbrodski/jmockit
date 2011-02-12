/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates an instance field of the test class or a parameter of the test method whose value will be an <em>injectable
 * mocked instance</em>.
 * Such instances can be said to be proper <em>mock objects</em>, in contrast to the <em>mocked instances</em> of a
 * regular mocked type.
 * The intent is that these mock objects will be passed/injected into the tested object/unit.
 * <p/>
 * The use of this annotation implies that the declared type of the mock field/parameter is a mocked type, just like it
 * would be if annotated with {@linkplain Mocked @Mocked}.
 * The {@code @Mocked} annotation can be applied at the same time, if needed.
 * The default mocking behavior, however, is different in several ways.
 * <p/>
 * For the duration of each test where the mock field/parameter is in scope, <em>only one</em> injectable instance is
 * mocked; other instances of the same mocked type are not affected.
 * For an injectable mocked <em>class</em>, <em>static methods</em> and <em>constructors</em> are <em>not</em> mocked;
 * only instance methods are. The static initializers of said classes are <em>not</em> stubbed out by default.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Injectable
{
}
