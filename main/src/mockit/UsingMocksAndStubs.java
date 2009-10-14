/*
 * JMockit Annotations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
 * Indicates one or more {@linkplain #value() classes} to be mocked or stubbed out.
 *
 * @see MockClass
 * @see Mockit#setUpMocks
 * @see Mockit#stubOut
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsingMocksAndStubs
{
   /**
    * A mix of mock and real classes from which the set of methods and constructors to be mocked or
    * stubbed out is obtained.
    * <p/>
    * A mock class is one {@linkplain MockClass annotated as such}.
    * It can define mocks and/or stubs for individual methods/constructors in a real class.
    * <p/>
    * A "real class" is any class not annotated with {@link MockClass} which will have all of its
    * methods and constructors, as well static initializers, stubbed out.
    */
   Class<?>[] value();
}
