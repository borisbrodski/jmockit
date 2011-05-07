/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

/**
 * Indicates an instance field holding either a default return value for a given return type, or a checked exception
 * instance to be thrown for a given checked exception type.
 * If the field is not explicitly assigned a value, then one will be created and assigned automatically, provided the
 * declared type of the field is a class having a public no-args constructor.
 * <p/>
 * This annotation is only relevant inside expectation blocks, and only applies to mocked methods/constructors for which
 * no matching expectation is recorded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Input
{
}
