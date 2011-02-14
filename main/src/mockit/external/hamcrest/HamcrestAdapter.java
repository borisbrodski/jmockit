/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.external.hamcrest;

import java.lang.reflect.*;

import mockit.internal.util.*;

import static mockit.internal.util.Utilities.*;

/**
 * Adapts the {@code org.hamcrest.Matcher} interface to {@link mockit.external.hamcrest.Matcher}.
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public final class HamcrestAdapter<T> extends BaseMatcher<T>
{
   private final org.hamcrest.Matcher<T> hamcrestMatcher;

   public static <T> HamcrestAdapter<T> create(final Object matcher)
   {
      org.hamcrest.Matcher<T> hamcrestMatcher;

      if (matcher instanceof org.hamcrest.Matcher<?>) {
         //noinspection unchecked
         hamcrestMatcher = (org.hamcrest.Matcher<T>) matcher;
      }
      else {
         hamcrestMatcher = new org.hamcrest.BaseMatcher<T>()
         {
            Method handler;

            public boolean matches(Object value)
            {
               if (handler == null) {
                  handler = Utilities.findNonPrivateHandlerMethod(matcher);
               }

               Boolean result = Utilities.invoke(matcher, handler, value);

               return result == null || result;
            }

            public void describeTo(org.hamcrest.Description description)
            {
            }
         };
      }

      return new HamcrestAdapter<T>(hamcrestMatcher);
   }

   private HamcrestAdapter(org.hamcrest.Matcher<T> matcher)
   {
      hamcrestMatcher = matcher;
   }

   public boolean matches(Object item)
   {
      return hamcrestMatcher.matches(item);
   }

   public void describeTo(Description description)
   {
      org.hamcrest.Description strDescription = new org.hamcrest.StringDescription();
      hamcrestMatcher.describeTo(strDescription);
      description.appendText(strDescription.toString());
   }

   public Object getInnerValue()
   {
      Object innermostMatcher = getInnermostMatcher();

      return getArgumentValueFromMatcherIfAvailable(innermostMatcher);
   }

   private Object getInnermostMatcher()
   {
      org.hamcrest.Matcher<T> innerMatcher = hamcrestMatcher;

      while (
         innerMatcher instanceof org.hamcrest.core.Is ||
         innerMatcher instanceof org.hamcrest.core.IsNot
      ) {
         //noinspection unchecked
         innerMatcher = getField(innerMatcher.getClass(), org.hamcrest.Matcher.class, innerMatcher);
      }

      return innerMatcher;
   }

   private Object getArgumentValueFromMatcherIfAvailable(Object argMatcher)
   {
      if (
         argMatcher instanceof org.hamcrest.core.IsEqual ||
         argMatcher instanceof org.hamcrest.core.IsSame ||
         "org.hamcrest.number.OrderingComparison".equals(argMatcher.getClass().getName())
      ) {
         return getField(argMatcher.getClass(), Object.class, argMatcher);
      }

      return null;
   }
}
