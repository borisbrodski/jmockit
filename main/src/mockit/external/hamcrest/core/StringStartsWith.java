/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

/**
 * Tests if the argument is a string that contains a substring.
 */
public final class StringStartsWith extends SubstringMatcher
{
   public StringStartsWith(CharSequence substring)
   {
      super(substring);
   }

   @Override
   protected boolean evalSubstringOf(CharSequence s)
   {
      return s.toString().startsWith(substring.toString());
   }

   @Override
   protected String relationship()
   {
      return "starting with";
   }
}