/*
 * JMockit Coverage
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
package integrationTests.data;

public class ClassWithFields
{
   private static int static1;
   private static String static2;
   private static long static3;

   // Instance fields:
   // (coverage accounts for each owner instance)
   private boolean instance1;
   private Boolean instance2;
   private double instance3;

   public static int getStatic1()
   {
      return static1;
   }

   public static void setStatic1(int static1)
   {
      ClassWithFields.static1 = static1;
   }

   public static void setStatic2(String static2)
   {
      ClassWithFields.static2 = static2;
   }

   public static long getStatic3()
   {
      return static3;
   }

   public static void setStatic3(long static3)
   {
      ClassWithFields.static3 = static3;
   }

   /**
    * Indicates whether {@link #instance1} is {@code true} or {@code false}.
    */
   public boolean isInstance1()
   {
      return instance1;
   }

   public void setInstance1(boolean instance1)
   {
      this.instance1 = instance1;
   }

   public void setInstance2(Boolean instance2)
   {
      this.instance2 = instance2;
   }

   public double getInstance3()
   {
      return instance3;
   }

   public void setInstance3(double instance3)
   {
      this.instance3 = instance3;
   }
}
