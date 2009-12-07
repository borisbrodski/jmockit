/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest;

import mockit.external.hamcrest.internal.ReflectiveTypeFinder;

/**
 * Convenient base class for Matchers that require a non-null value of a specific type.
 * This simply implements the null check, checks the type and then casts.
 *
 * @author Joe Walnes
 * @author Steve Freeman
 * @author Nat Pryce
 */
public abstract class TypeSafeMatcher<T> extends BaseMatcher<T>
{
   private static final ReflectiveTypeFinder TYPE_FINDER = new ReflectiveTypeFinder();

   private final Class<?> expectedType;

   /**
    * The default constructor for simple sub types.
    */
   protected TypeSafeMatcher()
   {
      expectedType = TYPE_FINDER.findExpectedType(getClass());
   }

   /**
    * Subclasses should implement this. The item will already have been checked for
    * the specific type and will never be null.
    */
   protected abstract boolean matchesSafely(T item);

   /**
    * Subclasses should override this. The item will already have been checked for
    * the specific type and will never be null.
    */
   protected void describeMismatchSafely(T item, Description mismatchDescription)
   {
      super.describeMismatch(item, mismatchDescription);
   }

   /**
    * Methods made final to prevent accidental override.
    * If you need to override this, there's no point on extending TypeSafeMatcher.
    * Instead, extend the {@link BaseMatcher}.
    */
   @SuppressWarnings({"unchecked"})
   public final boolean matches(Object item)
   {
      return item != null && expectedType.isInstance(item) && matchesSafely((T) item);
   }

   @Override
   @SuppressWarnings("unchecked")
   public final void describeMismatch(Object item, Description description)
   {
      if (item == null) {
         super.describeMismatch(item, description);
      }
      else if (!expectedType.isInstance(item)) {
         description.appendText("was a ").appendText(item.getClass().getName())
            .appendText(" (").appendValue(item).appendText(")");
      }
      else {
         describeMismatchSafely((T) item, description);
      }
   }
}
