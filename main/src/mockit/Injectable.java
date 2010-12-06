/*
 * JMockit Expectations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 * <p/>
 * Mock fields or parameters with injectable instances can be of any reference type.
 * However, while allowed, the use of this annotation is redundant for interfaces and annotations, since their
 * instances will belong to a dynamically created mock class.
 * It is also redundant for an enum type, since each enum value is unique.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Injectable
{
}
