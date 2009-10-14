/*
 * JMockit
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

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public final class MockedEnumsTest
{
   enum MyEnum
   {
      First(true, 10, "First"),
      Second(false, 6, "Second");

      private final boolean flag;
      private final int num;
      private final String desc;

      MyEnum(boolean flag, int num, String desc)
      {
         this.flag = flag;
         this.num = num;
         this.desc = desc;
      }

      public double getValue(double f) { return f * num; }

      @Override
      public String toString() { return num + desc + flag; }
   }

   @Test
   public void mockEnumValues()
   {
      MyEnum.First.toString();

      new Expectations()
      {
         final MyEnum mock = MyEnum.First;

         {
            MyEnum.values(); returns(new MyEnum[] {mock});
            mock.getValue(withAny(0.0)); returns(50.0);
         }
      };

      MyEnum[] values = MyEnum.values();
      assertEquals(1, values.length);

      double value = values[0].getValue(0.5);
      assertEquals(50.0, value, 0.0);
   }

   @Test
   public void mockSpecificEnumElement()
   {
      final double f = 2.5;

      new NonStrictExpectations()
      {
         final MyEnum mock = MyEnum.Second;

         {
            mock.getValue(f); returns(12.3);
         }
      };

      double surfaceWeight = MyEnum.Second.getValue(f);
      assertEquals(12.3, surfaceWeight, 0.0);
   }
}
