/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

/**
 * Is the value null?
 */
public final class IsNull<T> extends BaseMatcher<T>
{
   public boolean matches(Object o)
   {
      return o == null;
   }

   public void describeTo(Description description)
   {
      description.appendText("null");
   }
}

