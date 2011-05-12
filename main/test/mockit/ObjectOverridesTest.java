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

   @Test
   public void allowAnyInvocationsOnOverriddenObjectMethodsForStrictMocks()
   {
      new Expectations()
      {
         {
            a.getIntValue(); result = 58;
            b.doSomething();
         }
      };

      assert !a.equals(b);
      assert a.hashCode() != b.hashCode();
      assert a.getIntValue() == 58;
      assert a.equals(a);
      String bStr = b.toString();
      b.doSomething();
      assert !b.equals(a);
      String aStr = a.toString();
      assert !aStr.equals(bStr);

      new Verifications()
      {
         {
            a.equals(b);
            b.hashCode(); times = 1;
            a.toString();
            b.equals(null); times = 0;
         }
      };

      new VerificationsInOrder()
      {
         {
            a.hashCode();
            b.equals(a);
         }
      };
   }

   @Test
   public void recordExpectationsOnOverriddenObjectMethodAsNonStrictEvenInsideStrictExpectationBlock()
   {
      new Expectations()
      {
         {
            a.doSomething();
            a.hashCode(); result = 1;
            a.equals(any);
            a.toString();
         }
      };

      a.doSomething();
   }
}
