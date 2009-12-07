/*  Copyright (c) 2000-2009 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

/**
 * Calculates the logical negation of a matcher.
 */
public final class IsNot<T> extends BaseMatcher<T>
{
   private final Matcher<T> matcher;

   public IsNot(Matcher<T> matcher)
   {
      this.matcher = matcher;
   }

   public boolean matches(Object arg)
   {
      return !matcher.matches(arg);
   }

   public void describeTo(Description description)
   {
      description.appendText("not ").appendDescriptionOf(matcher);
   }
}
