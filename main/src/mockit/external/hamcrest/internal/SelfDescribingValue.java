/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.internal;

import mockit.external.hamcrest.*;

public final class SelfDescribingValue<T> implements SelfDescribing
{
   private final T value;

   public SelfDescribingValue(T value)
   {
      this.value = value;
   }

   public void describeTo(Description description)
   {
      description.appendValue(value);
   }
}
