/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

/**
 * Tests whether the value is an instance of a class.
 * Classes of basic types will be converted to the relevant "Object" classes
 */
public final class IsInstanceOf extends BaseMatcher<Object>
{
   private final Class<?> expectedClass;
   private final Class<?> matchableClass;

   /**
    * Creates a new instance of IsInstanceOf.
    *
    * @param expectedClass The predicate evaluates to true for instances of this class
    *                      or one of its subclasses.
    */
   public IsInstanceOf(Class<?> expectedClass)
   {
      this.expectedClass = expectedClass;
      matchableClass = matchableClass(expectedClass);
   }

   @SuppressWarnings({"ControlFlowStatementWithoutBraces"})
   private static Class<?> matchableClass(Class<?> expectedClass)
   {
      if (boolean.class.equals(expectedClass)) return Boolean.class;
      if (byte.class.equals(expectedClass)) return Byte.class;
      if (char.class.equals(expectedClass)) return Character.class;
      if (double.class.equals(expectedClass)) return Double.class;
      if (float.class.equals(expectedClass)) return Float.class;
      if (int.class.equals(expectedClass)) return Integer.class;
      if (long.class.equals(expectedClass)) return Long.class;
      if (short.class.equals(expectedClass)) return Short.class;
      return expectedClass;
   }

   public boolean matches(Object item)
   {
      return matches(item, Description.NONE);
   }

   @Override
   public void describeMismatch(Object item, Description mismatchDescription)
   {
      matches(item, mismatchDescription);
   }

   private boolean matches(Object item, Description mismatchDescription)
   {
      if (item == null) {
         mismatchDescription.appendText("null");
         return false;
      }

      if (!matchableClass.isInstance(item)) {
         mismatchDescription.appendValue(item).appendText(" is a " + item.getClass().getName());
         return false;
      }

      return true;
   }

   public void describeTo(Description description)
   {
      description.appendText("an instance of ").appendText(expectedClass.getName());
   }
}
