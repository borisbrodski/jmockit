/*
 * JMockit
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

import java.util.*;

final class Subclass extends BaseClass
{
   final int INITIAL_VALUE = new Random().nextInt();

   private static StringBuilder buffer;
   private static char static1;
   private static char static2;

   static StringBuilder getBuffer() { return buffer; }
   static void setBuffer(StringBuilder buffer) { Subclass.buffer = buffer; }

   private String stringField;
   private int intField;
   private int intField2;
   private List<String> listField;

   Subclass() { intField = -1; }
   Subclass(int a, String b) { intField = a; stringField = b; }
   Subclass(String... args) { listField = Arrays.asList(args); }

   private static Boolean anStaticMethod() { return true; }
   private static void staticMethod(short s, String str, Boolean b) {}

   private long aMethod() { return 567L; }
   private void instanceMethod(short s, String str, Boolean b) {}

   int getIntField() { return intField; }
   void setIntField(int intField) { this.intField = intField; }

   int getIntField2() { return intField2; }
   void setIntField2(int intField2) { this.intField2 = intField2; }

   String getStringField() { return stringField; }
   void setStringField(String stringField) { this.stringField = stringField; }

   List<String> getListField() { return listField; }
   void setListField(List<String> listField) { this.listField = listField; }

   private final class InnerClass
   {
      private InnerClass() {}
      private InnerClass(boolean b, Long l, String s) {}
   }
}
