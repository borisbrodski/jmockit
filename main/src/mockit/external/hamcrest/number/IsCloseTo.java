/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.number;

import mockit.external.hamcrest.*;

/**
 * Decides if the value of a number is equal to a value within some range of acceptable error.
 */
public final class IsCloseTo extends TypeSafeMatcher<Number>
{
   private final double delta;
   private final double value;

   public IsCloseTo(double value, double error)
   {
      delta = error;
      this.value = value;
   }

   @Override
   public boolean matchesSafely(Number item)
   {
      return actualDelta(item) <= 0.0;
   }

   @Override
   public void describeMismatchSafely(Number item, Description description)
   {
      description.appendValue(item).appendText(" differed by ").appendValue(actualDelta(item));
   }

   public void describeTo(Description description)
   {
      description.appendText("a numeric value within ").appendValue(delta).appendText(" of ")
         .appendValue(value);
   }

   private double actualDelta(Number item)
   {
      return Math.abs(item.doubleValue() - value) - delta;
   }
}
