/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

@SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals"})
public final class ObjectOverridesAndInjectableMocksTest
{
   @Injectable ClassWithObjectOverrides a;
   @Injectable ClassWithObjectOverrides b;

   @Test
   public void verifyStandardBehaviorOfOverriddenEqualsMethodsInMockedClass()
   {
      assertDefaultEqualsBehavior(a, b);
      assertDefaultEqualsBehavior(b, a);
   }

   private void assertDefaultEqualsBehavior(Object a, Object b)
   {
      assert !a.equals(null);
      assert !a.equals("test");
      assert a.equals(a);
      assert !a.equals(b);
   }

   @Test
   public void allowAnyInvocationsOnOverriddenObjectMethodsForStrictMocks()
   {
      new Expectations() {{ a.getIntValue(); result = 58; }};

      assert !a.equals(b);
      assert a.equals(a);
      assert a.getIntValue() == 58;
      assert !b.equals(a);
      assert !a.equals(b);
   }
}
