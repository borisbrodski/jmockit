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

import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

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
            MyEnum.values(); result = new MyEnum[] {mock};
            mock.getValue(anyDouble); result = 50.0;
         }
      };

      MyEnum[] values = MyEnum.values();
      assertEquals(1, values.length);

      double value = values[0].getValue(0.5);
      assertEquals(50.0, value, 0.0);
   }

   @Test
   public void mockInstanceMethodOnAnyEnumElement()
   {
      final double f = 2.5;

      new NonStrictExpectations()
      {
         MyEnum mock;

         {
            mock.getValue(f); result = 12.3;
         }
      };

      assertEquals(12.3, MyEnum.First.getValue(f), 0.0);
      assertEquals(12.3, MyEnum.Second.getValue(f), 0.0);
   }

   @Test
   public void mockSpecificEnumElementsByUsingTwoMockInstances()
   {
      new Expectations()
      {
         final MyEnum mock1 = MyEnum.First;
         final MyEnum mock2 = MyEnum.Second;

         {
            mock1.getValue(anyDouble); result = 12.3;
            mock2.getValue(anyDouble); result = -5.01;
         }
      };

      assertEquals(12.3, MyEnum.First.getValue(2.5), 0.0);
      assertEquals(-5.01, MyEnum.Second.getValue(1), 0.0);
   }

   @Test
   public void mockSpecificEnumElementsByUsingASingleMockInstance(@NonStrict MyEnum unused)
   {
      new Expectations()
      {
         {
            onInstance(MyEnum.First).getValue(anyDouble); result = 12.3;
            onInstance(MyEnum.Second).getValue(anyDouble); result = -5.01;
         }
      };

      assertEquals(-5.01, MyEnum.Second.getValue(1), 0.0);
      assertEquals(12.3, MyEnum.First.getValue(2.5), 0.0);
   }

   @Test(expected = AssertionError.class)
   public void mockSpecificEnumElementsByUsingASingleStrictMockInstance()
   {
      new Expectations()
      {
         @Mocked("toString") final MyEnum unused = null;

         {
            onInstance(MyEnum.First).toString();
            onInstance(MyEnum.Second).toString();
         }
      };

      MyEnum.Second.toString();
   }
   
   @Test
   public void mockNonAbstractMethodsInEnumWithAbstractMethod(final TimeUnit tm) throws Exception
   {
      new Expectations()
      {{
         tm.convert(anyLong, TimeUnit.HOURS); result = 1L;
         tm.sleep(anyLong);
      }};

      assertEquals(1, tm.convert(1000, TimeUnit.HOURS));
      tm.sleep(10000);
   }

   public enum EnumWithValueSpecificMethods
   {
      One
      {
         @Override public int getValue() { return 1; }
         @Override public String getDescription() { return "one"; }
      },
      Two
      {
         @Override public int getValue() { return 2; }
         @Override public String getDescription() { return "two"; }
      };

      public abstract int getValue();
      public String getDescription() { return String.valueOf(getValue()); }
   }

   @Test
   public void mockEnumWithValueSpecificMethods(@Capturing EnumWithValueSpecificMethods mockedEnum)
   {
      assertSame(EnumWithValueSpecificMethods.One, mockedEnum);

      new NonStrictExpectations()
      {{
         // TODO: at a minimum, use of "onInstance" should not be needed here
         onInstance(EnumWithValueSpecificMethods.One).getValue(); result = 123;
         EnumWithValueSpecificMethods.Two.getValue(); result = -45;

         onInstance(EnumWithValueSpecificMethods.One).getDescription(); result = "1";
         EnumWithValueSpecificMethods.Two.getDescription(); result = "2";
      }};

      assertEquals(123, EnumWithValueSpecificMethods.One.getValue());
      assertEquals(-45, EnumWithValueSpecificMethods.Two.getValue());
      assertEquals("1", EnumWithValueSpecificMethods.One.getDescription());
      assertEquals("2", EnumWithValueSpecificMethods.Two.getDescription());
   }
}
