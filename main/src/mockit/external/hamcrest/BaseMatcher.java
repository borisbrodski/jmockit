/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest;

/**
 * BaseClass for all Matcher implementations.
 *
 * @see org.hamcrest.Matcher
 */
public abstract class BaseMatcher<T> implements Matcher<T>
{
   public void describeMismatch(Object item, Description description)
   {
      description.appendText("was ").appendValue(item);
   }

   @Override
   public String toString()
   {
      return StringDescription.toString(this);
   }
}
