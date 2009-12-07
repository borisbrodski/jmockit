/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

/**
 * Is the value the same object as another value?
 */
public final class IsSame<T> extends BaseMatcher<T>
{
   private final T object;

   public IsSame(T object)
   {
      this.object = object;
   }

   public boolean matches(Object arg)
   {
      return arg == object;
   }

   public void describeTo(Description description)
   {
      description.appendText("sameInstance(").appendValue(object).appendText(")");
   }
}
