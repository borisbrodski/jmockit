/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.internal;

import java.util.Iterator;

import mockit.external.hamcrest.SelfDescribing;

public final class SelfDescribingValueIterator<T> implements Iterator<SelfDescribing>
{
   private final Iterator<T> values;

   public SelfDescribingValueIterator(Iterator<T> values)
   {
      this.values = values;
   }

   public boolean hasNext()
   {
      return values.hasNext();
   }

   public SelfDescribing next()
   {
      return new SelfDescribingValue<T>(values.next());
   }

   public void remove()
   {
      values.remove();
   }
}
