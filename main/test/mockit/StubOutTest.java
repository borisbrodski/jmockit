/*
 * JMockit Core
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

public final class StubOutTest
{
   static class RealClass
   {
      private static final boolean staticInitializerExecuted;
      static { staticInitializerExecuted = true; }

      int doSomething() { return 1; }
      private static void tryAndFail(String s) { throw new AssertionError(s); }

      long getLongValue() { return 15L; }
      static float getFloatValue() { return 1.5F; }
      synchronized double getDoubleValue() { return 1.5; }

      int[][] getMatrix() { return null; }
      boolean[] getBooleans() { return null; }
      char[] getChars() { return null; }
      byte[] getBytes() { return null; }
      short[] getShorts() { return null; }
      int[] getInts() { return null; }
      long[] getLongs() { return null; }
      float[] getFloats() { return null; }
      double[] getDoubles() { return null; }
      Object[] getObjects() { return null; }
   }

   @Test
   public void stubOutMethodsAndStaticInitializer()
   {
      Mockit.stubOut(RealClass.class);

      assertFalse(RealClass.staticInitializerExecuted);

      RealClass obj = new RealClass();
      assertEquals(0, obj.doSomething());
      assertEquals(0L, obj.getLongValue());
      assertEquals(0.0F, RealClass.getFloatValue(), 0);
      assertEquals(0.0, obj.getDoubleValue(), 0);
      assertArrayEquals(new int[0][0], obj.getMatrix());
      assertEquals(0, obj.getBooleans().length);
      assertArrayEquals(new char[0], obj.getChars());
      assertArrayEquals(new byte[0], obj.getBytes());
      assertArrayEquals(new short[0], obj.getShorts());
      assertArrayEquals(new int[0], obj.getInts());
      assertArrayEquals(new long[0], obj.getLongs());
      assertArrayEquals(new float[0], obj.getFloats(), 0);
      assertArrayEquals(new double[0], obj.getDoubles(), 0);
      assertArrayEquals(new Object[0], obj.getObjects());

      RealClass.tryAndFail("test");
   }

   static final class AnotherRealClass
   {
      AnotherRealClass() { throw new IllegalStateException("should not happen"); }

      private String getText(boolean b) { return "" + b; }
   }

   @Test
   public void stubOutClass()
   {
      Mockit.stubOutClass(AnotherRealClass.class);

      assertNull(new AnotherRealClass().getText(true));
   }

   @Test
   public void stubOutClassByName()
   {
      Mockit.stubOutClass(AnotherRealClass.class.getName());

      assertNull(new AnotherRealClass().getText(true));
   }

   static final class YetAnotherRealClass
   {
      private final int value;

      YetAnotherRealClass() { value = 123; }
      YetAnotherRealClass(int value) { this.value = value; }

      public String doSomething(RealClass a, AnotherRealClass b)
      {
         a.doSomething();
         return b.getText(false);
      }
   }

   @Test
   public void stubOutClassUsingFilters()
   {
      Mockit.stubOutClass(YetAnotherRealClass.class, "doSomething");

      YetAnotherRealClass obj = new YetAnotherRealClass();
      assertEquals(123, obj.value);
      assertNull(obj.doSomething(null, null));
   }

   @Test
   public void stubOutClassUsingFiltersByName()
   {
      Mockit.stubOutClass(YetAnotherRealClass.class.getName(), "doSomething");

      YetAnotherRealClass obj = new YetAnotherRealClass();
      assertEquals(123, obj.value);
      assertNull(obj.doSomething(null, null));
   }

   @Test
   public void stubOutClassUsingInverseFilters()
   {
      Mockit.stubOutClass(AnotherRealClass.class, true, "getText(boolean)");
      Mockit.stubOutClass(YetAnotherRealClass.class, false, "()", "(int)");

      assertEquals(0, new YetAnotherRealClass(45).value);
      YetAnotherRealClass obj = new YetAnotherRealClass();
      assertEquals(0, obj.value);
      assertEquals("false", obj.doSomething(new RealClass(), new AnotherRealClass()));
   }
   
   @Test
   public void stubOutClassUsingInverseFiltersByName()
   {
      Mockit.stubOutClass(AnotherRealClass.class.getName(), true, "getText(boolean)");
      Mockit.stubOutClass(YetAnotherRealClass.class.getName(), false, "()", "(int)");

      assertEquals(0, new YetAnotherRealClass(45).value);
      YetAnotherRealClass obj = new YetAnotherRealClass();
      assertEquals(0, obj.value);
      assertEquals("false", obj.doSomething(new RealClass(), new AnotherRealClass()));
   }
}
