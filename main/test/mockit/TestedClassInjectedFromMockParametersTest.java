/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class TestedClassInjectedFromMockParametersTest
{
   public static final class TestedClass
   {
      private int i;
      private String s;
      private final boolean b;
      private char[] chars;

      public TestedClass(boolean b) { this.b = b; }

      public TestedClass(int i, String s, boolean b, char... chars)
      {
         this.i = i;
         this.s = s;
         this.b = b;
         this.chars = chars;
      }

      public TestedClass(boolean b1, byte b2, boolean b3)
      {
         b = b1;
         chars = new char[] {(char) b2, b3 ? 'X' : 'x'};
      }
   }

   @Tested TestedClass tested;

   @Test(expected = IllegalArgumentException.class)
   public void attemptToInstantiateTestedClassWithNoInjectables()
   {
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToInstantiateTestedClassWithInjectablePrimitiveHavingNoValue(@Injectable boolean b)
   {
   }

   @Test
   public void exerciseTestedObjectInjectedFromMockParameters(
      @Injectable @Mocked("") String s, @Injectable("123") int mock1, @Injectable("true") boolean mock2,
      @Injectable("A") char c1, @Injectable("bB") char c2)
   {
      assertEquals(s, tested.s);
      assertEquals(mock1, tested.i);
      assertEquals(mock2, tested.b);
      assertEquals(2, tested.chars.length);
      assertEquals(c1, tested.chars[0]);
      assertEquals(c2, tested.chars[1]);
      assertEquals('b', c2);
   }

   @Test
   public void exerciseTestedObjectInjectedFromMockParameters(
      @Injectable("true") boolean b1, @Injectable("true") boolean b3, @Injectable("65") byte b2)
   {
      assertTrue(tested.b);
      assertEquals('A', tested.chars[0]);
      assertEquals('X', tested.chars[1]);
   }
}
