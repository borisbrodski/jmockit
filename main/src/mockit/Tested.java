/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates a class to be tested in isolation from selected dependencies, with optional automatic instantiation and/or
 * automatic injection of dependencies.
 * This annotation is only applicable to instance fields of a test class.
 * <p/>
 * If the tested class has a super-class (apart from {@code Object}), it's also considered to be tested,
 * <em>provided</em> both classes have the same {@linkplain java.security.ProtectionDomain protection domain}.
 * This also applies to the super-class of the super-class, up to but not including the first super-class with a
 * different protection domain.
 * (In practice, this rule about protection domains means that super-classes defined by the JRE or by a third-party
 * library will be excluded from consideration.)
 * <p/>
 * If the tested field is not {@code final} and its value remains {@code null} at the time a test method is about to be
 * executed, then a suitable instance of the tested class is created and assigned to the field.
 * The instantiation will only be attempted if the tested class has a constructor with the same accessibility as the
 * class: i.e. either a <em>public</em> constructor in a public class, or a <em>non-private</em> constructor in a
 * <em>non-public</em> class.
 * Constructor injection will take place, provided all of the constructor parameters (if any) can be satisfied with the
 * values of matching {@linkplain Injectable injectable} fields and/or parameters.
 * This process of injection works even with multiple constructor parameters of the same type, provided there is a
 * separate injectable field or test method parameter for each parameter of the constructor, declared in the same
 * relative order.
 * <p/>
 * Whenever the tested object is created automatically, <em>field injection</em> is also performed.
 * Only non-<code>final</code> fields which remain {@code null} at this time are considered, between those declared in
 * the tested class itself or in one of its super-classes.
 * For each such field, the mock instance currently available in an {@linkplain Injectable injectable} mock field or
 * mock parameter of the <em>same</em> type will be injected.
 * If there is more than one field of a given type, then the field to be injected is matched to a suitable mock field
 * <em>by name</em>, provided there is any in the test class.
 * If there is no matching injectable mock field or parameter, the target field in the tested class will be left with
 * the {@code null} reference.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tested
{
}
