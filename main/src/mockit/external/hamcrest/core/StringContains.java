/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest.core;

/**
 * Tests if the argument is a string that contains a substring.
 */
public final class StringContains extends SubstringMatcher
{
   public StringContains(CharSequence substring)
   {
      super(substring);
   }

   @Override
   protected boolean evalSubstringOf(CharSequence s)
   {
      return s.toString().contains(substring);
   }

   @Override
   protected String relationship()
   {
      return "containing";
   }
}