/*
 * JMockit Expectations
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
 * @deprecated Use {@link Mocked} instead.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Deprecated
public @interface MockField
{
   /**
    * @deprecated Use {@link Mocked#value} instead.
    */
   @Deprecated String[] value() default {};

   /**
    * @deprecated Use {@link Mocked#methods} instead.
    */
   @Deprecated String[] methods() default {};

   /**
    * @deprecated Use {@link Mocked#inverse} instead.
    */
   @Deprecated boolean inverse() default false;

   /**
    * @deprecated Use {@link Mocked#constructorArgsMethod} instead.
    */
   @Deprecated String constructorArgsMethod() default "";

   /**
    * @deprecated Use {@link Mocked#capture} instead.
    */
   @Deprecated int capture() default 0;

   /**
    * @deprecated Use {@link Mocked#realClassName} instead.
    */
   @Deprecated String realClassName() default "";
}
