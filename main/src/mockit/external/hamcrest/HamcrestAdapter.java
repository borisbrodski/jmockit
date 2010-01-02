/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
