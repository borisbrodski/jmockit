/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

import mockit.external.hamcrest.*;

abstract class SubstringMatcher extends TypeSafeMatcher<CharSequence>
{
   final CharSequence substring;

   SubstringMatcher(CharSequence substring)
   {
      this.substring = substring;
   }

   @Override
   public final boolean matchesSafely(CharSequence item)
   {
      return evalSubstringOf(item);
   }

   @Override
   public final void describeMismatchSafely(CharSequence item, Description mismatchDescription)
   {
      mismatchDescription.appendText("was \"").appendText(item).appendText("\"");
   }

   public final void describeTo(Description description)
   {
      description.appendText("a string ").appendText(relationship()).appendText(" ")
         .appendValue(substring);
   }

   abstract boolean evalSubstringOf(CharSequence string);

   abstract String relationship();
}