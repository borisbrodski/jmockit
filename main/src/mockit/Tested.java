/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates a class to be tested in isolation from selected dependencies.
 * This annotation is only applicable to instance fields of a test class.
 * <p/>
 * If the tested class has a super-class (apart from {@code Object}), it's also considered to be tested,
 * <em>provided</em> both classes have the same {@linkplain java.security.ProtectionDomain protection domain}.
 * This also applies to the super-class of the super-class, up to but not including the first super-class with a
 * different protection domain.
 * (In practice, this rule about protection domains means that super-classes defined by the JRE or by a third-party
 * library will be excluded from consideration, as they should be.)
 * <p/>
 * If the tested field is not {@code final} and its value remains {@code null} at the time a test method is about to be
 * executed, then a suitable instance of the tested class will be created and assigned to the field.
 * The instantiation will only succeed if the tested class has a single public constructor, and all parameters to this
 * constructor (if any) can be satisfied with the instances available in {@linkplain Injectable injectable} mock fields
 * of the test class.
 * <p/>
 * Whenever the tested object is created automatically, <em>field injection</em> is also performed.
 * Only non-{@code final} fields which remain {@code null} at this time are considered, between those declared in the
 * tested class itself or in a tested super-class.
 * For each such field, the mock instance currently available in an {@linkplain Injectable injectable} mock field of the
 * <em>same</em> type will be injected.
 * If there is more than one field of a given type, then the field to be injected is matched to a suitable mock field
 * <em>by name</em>.
 * If no matching injectable mock field exists in the test class, the target field in the tested class will be left
 * with the {@code null} reference.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tested
{
}
