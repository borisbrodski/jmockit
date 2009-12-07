/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.internal;

import java.lang.reflect.Array;
import java.util.Iterator;

public final class ArrayIterator implements Iterator<Object>
{
   private final Object array;
   private int currentIndex;

   public ArrayIterator(Object array)
   {
      if (!array.getClass().isArray()) {
         throw new IllegalArgumentException("not an array");
      }

      this.array = array;
   }

   public boolean hasNext()
   {
      return currentIndex < Array.getLength(array);
   }

   public Object next()
   {
      return Array.get(array, currentIndex++);
   }

   public void remove()
   {
      throw new UnsupportedOperationException("cannot remove items from an array");
   }
}
