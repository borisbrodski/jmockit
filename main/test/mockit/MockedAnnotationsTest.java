/*
 * JMockit Expectations & Verifications
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

import org.junit.*;

import static org.junit.Assert.*;

public final class MockedAnnotationsTest
{
   @interface MyAnnotation
   {
      String value();
      boolean flag() default true;
      String[] values() default {};
   }

   @Test
   public void specifyValuesForAnnotationAttributes(final MyAnnotation a)
   {
      assertSame(MyAnnotation.class, a.annotationType());

      new Expectations()
      {
         {
            a.flag(); result = false;
            a.value(); result = "test";
            a.values(); returns("abc", "dEf");
         }
      };

      assertFalse(a.flag());
      assertEquals("test", a.value());
      assertArrayEquals(new String[] {"abc", "dEf"}, a.values());
   }

   @Test
   public void verifyUsesOfAnnotationAttributes(final MyAnnotation a)
   {
      new NonStrictExpectations()
      {
         {
            a.value(); result = "test";
            a.values(); returns("abc", "dEf");
         }
      };

      // Same rule for regular methods applies (ie, if no return value was recorded, invocations
      // will get the default for the return type).
      assertFalse(a.flag());

      assertEquals("test", a.value());
      assertArrayEquals(new String[] {"abc", "dEf"}, a.values());
      a.value();

      new FullVerifications()
      {
         {
            // Mocked methods called here always return the default value according to return type.
            a.flag();
            a.value(); times = 2;
            a.values();
         }
      };
   }

   @Test
   public void mockASingleAnnotationAttribute(@Mocked("value") final MyAnnotation a)
   {
      new Expectations()
      {
         {
            a.value(); result = "test";
         }
      };

      assertFalse(a.flag());
      assertEquals("test", a.value());
      assertEquals(0, a.values().length);
   }
}