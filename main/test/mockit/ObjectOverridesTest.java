/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import mockit.internal.util.*;

@SuppressWarnings({
   "ObjectEqualsNull", "EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals", "FinalizeCalledExplicitly"})
public final class ObjectOverridesTest
{
   @Test
   public void verifyStandardBehaviorOfOverridableObjectMethodsInMockedInterface(Runnable a, Runnable b)
   {
      assertDefaultEqualsBehavior(a, b);
      assertDefaultEqualsBehavior(b, a);

      assertDefaultHashCodeBehavior(a);
      assertDefaultHashCodeBehavior(b);

      assertDefaultToStringBehavior(a);
      assertDefaultToStringBehavior(b);
   }

   private void assertDefaultEqualsBehavior(Object a, Object b)
   {
      assert !a.equals(null);
      assert !a.equals("test");
      assert a.equals(a);
      assert !a.equals(b);
   }

   private void assertDefaultHashCodeBehavior(Object a)
   {
      assert a.hashCode() == System.identityHashCode(a);
   }

   private void assertDefaultToStringBehavior(Object a)
   {
      assert a.toString().equals(Utilities.objectIdentity(a));
   }

   public static final class ClassWithObjectOverrides implements Cloneable
   {
      private final StringBuilder text;

      public ClassWithObjectOverrides(String text) { this.text = new StringBuilder(text); }

      @Override
      public boolean equals(Object o)
      {
         return o instanceof ClassWithObjectOverrides && text.equals(((ClassWithObjectOverrides) o).text);
      }

      @Override
      public int hashCode() { return text.hashCode(); }

      @Override
      public String toString() { return text.toString(); }

      @SuppressWarnings({"FinalizeDeclaration"})
      @Override
      protected void finalize()
      {
         text.setLength(0);
      }

      @Override
      public ClassWithObjectOverrides clone()
      {
         ClassWithObjectOverrides theClone = null;
         try { theClone = (ClassWithObjectOverrides) super.clone(); } catch (CloneNotSupportedException ignore) {}
         return theClone;
      }
   }

   @Mocked ClassWithObjectOverrides a;
   @Mocked ClassWithObjectOverrides b;

   @Test
   public void verifyStandardBehaviorOfOverriddenObjectMethodsInMockedClass()
   {
      assertDefaultEqualsBehavior(a, b);
      assertDefaultEqualsBehavior(b, a);

      assertDefaultHashCodeBehavior(a);
      assertDefaultHashCodeBehavior(b);

      assertDefaultToStringBehavior(a);
      assertDefaultToStringBehavior(b);

      a.finalize();
      b.finalize();
   }

   @Test
   public void mockOverrideOfEqualsMethod()
   {
      new Expectations()
      {
         {
            a.equals(null); result = true;
            a.equals(anyString); result = true;
         }
      };

      new NonStrictExpectations()
      {
         {
            b.equals(a); result = true;
         }
      };

      assert a.equals(null);
      assert a.equals("test");
      assert b.equals(a);
   }

   @Test
   public void mockOverrideOfHashCodeMethod()
   {
      assert a.hashCode() != b.hashCode();

      new NonStrictExpectations()
      {
         {
            a.hashCode(); result = 123;
            b.hashCode(); result = 45; times = 1;
         }
      };

      assert a.hashCode() == 123;
      assert b.hashCode() == 45;
   }

   @Test
   public void mockOverrideOfToStringMethod()
   {
      assert !a.toString().equals(b.toString());

      new NonStrictExpectations()
      {
         {
            a.toString(); result = "mocked";
         }
      };

      assert "mocked".equals(a.toString());

      new Verifications()
      {
         {
            a.toString();
            b.toString(); times = 1;
         }
      };
   }

   @Test
   public void mockOverrideOfCloneMethod()
   {
      new Expectations()
      {
         {
            a.clone(); result = b;
         }
      };

      assert a.clone() == b;
   }
}
