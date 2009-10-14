/*
 * JMockit: a Java class library for developer testing with "mock methods"
 * Copyright (c) 2006 Rogerio Liesenfeld
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
package integrationTests;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.*;

public class C
{
   private String someValue;
   static String printedText;

   static {
      printedText = "";
   }

   public C()
   {
   }

   C(String someValue)
   {
      this.someValue = someValue;
   }

   public static boolean b()
   {
      return true;
   }

   public int i()
   {
      return 1;
   }

   void noReturn()
   {
      someValue = "C";
   }

   public String getSomeValue()
   {
      return someValue;
   }

   protected void setSomeValue(String someValue)
   {
      this.someValue = someValue;
   }

   @Override
   public String toString()
   {
      return someValue;
   }

   public final Date createOtherObject(boolean b, Date d, int a)
   {
      return b ? new Date(d.getTime() + a) : null;
   }

   static void printText()
   {
      doPrintText();
   }

   static void printText(String text)
   {
      printedText = text;
      System.out.println(text);
   }

   private static void doPrintText()
   {
      printedText = "text printed";
      System.out.println(printedText);
   }

   int count(Collection<?> items)
   {
      return items.size();
   }

   <E extends Comparable<E>> List<E> orderBy(Collection<E> items, final boolean asc)
   {
      List<E> sorted = new ArrayList<E>(items);

      Collections.sort(sorted, new Comparator<E>()
      {
         public int compare(E o1, E o2)
         {
            return asc ? o1.compareTo(o2) : o2.compareTo(o1);
         }
      });

      return sorted;
   }

   void loadFile(String name) throws FileNotFoundException
   {
      new FileReader(name);
   }

   void printArgs(Object... args)
   {
      printedText = "";

      for (Object arg : args) {
         printedText += arg;
         System.out.println(arg);
      }
   }

   public List<C2> findC2()
   {
      List<C2> found = Arrays.asList(new C2(1, "one"), new C2(2, "two"));
      return found;
   }

   public static void validateValues(long v1, int v2)
   {
   }

   public double sumValues(byte v1, short v2, int v3, long v4, float v5, double v6)
   {
      return v1 + v2 + v3 + v4 + v5 + v6;
   }
}

final class C2
{
   private int num;
   private String name;

   C2(int num, String name)
   {
      this.num = num;
      this.name = name;
   }

   public int getNum()
   {
      return num;
   }

   public String getName()
   {
      return name;
   }

   String getCode()
   {
      return num + "-" + name;
   }
}