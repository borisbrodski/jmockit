/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

@SuppressWarnings({"UnusedParameters", "ClassWithTooManyFields"})
public final class TestedClassWithConstructorDI0Test
{
   public static final class TestedClassWithConstructorHavingPrimitiveParameter
   {
      public TestedClassWithConstructorHavingPrimitiveParameter(int i) {}
   }

   public static final class TestedClassWithConstructorHavingArrayParameter
   {
      public TestedClassWithConstructorHavingArrayParameter(String[] arr) {}
   }

   public static final class TestedClassWithConstructorHavingMultipleLongParameters
   {
      public TestedClassWithConstructorHavingMultipleLongParameters(long l1, long l2) {}
   }

   public static final class TestedClassWithConstructorHavingVarargsParameter
   {
      final String s;

      public TestedClassWithConstructorHavingVarargsParameter(byte b, char c, String s, byte b2, boolean... flags)
      {
         this.s = s;
      }
   }

   @Tested TestedClassWithConstructorHavingPrimitiveParameter tested1;
   @Tested TestedClassWithConstructorHavingArrayParameter tested2;
   @Tested TestedClassWithConstructorHavingMultipleLongParameters tested3;
   @Tested TestedClassWithConstructorHavingVarargsParameter tested4;

   @Injectable int i = 123;
   @Injectable int unused;
   @Injectable long l1 = 1;
   @Injectable final long l2 = 2;
   @Injectable String[] arr = {"abc", "Xyz"};
   @Injectable byte b = 56;
   @Injectable byte b2 = 56;
   @Injectable char c = 'X';
   @Injectable String s = "test"; // String is mocked

   // For varargs parameter:
   @Injectable boolean firstFlag = true;
   @Injectable boolean secondFlag;
   @Injectable boolean thirdFlag = true;

   @Test
   public void verifyInstantiationOfTestedObjectsThroughConstructorsWithNonMockedParameters()
   {
      assertNotNull(tested1);
      assertNotNull(tested2);
      assertNotNull(tested3);
      assertNotNull(tested4);
      assertEquals("test", tested4.s);
   }
}
