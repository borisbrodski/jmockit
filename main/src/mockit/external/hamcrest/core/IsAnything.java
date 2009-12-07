/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

/**
 * A matcher that always returns <code>true</code>.
 */
public final class IsAnything<T> extends BaseMatcher<T>
{
   private final String message;

   public IsAnything()
   {
      this("ANYTHING");
   }

   public IsAnything(String message)
   {
      this.message = message;
   }

   public boolean matches(Object o)
   {
      return true;
   }

   public void describeTo(Description description)
   {
      description.appendText(message);
   }
}
