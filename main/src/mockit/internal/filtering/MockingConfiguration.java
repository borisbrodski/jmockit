/*
 * JMockit
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
package mockit.internal.filtering;

import java.util.*;
import java.util.regex.*;

import mockit.external.asm.*;

public final class MockingConfiguration
{
   private final List<MockFilter> filtersToApply;
   private final boolean desiredFilterResultWhenMatching;

   public MockingConfiguration(String[] filters, boolean desiredFilterResultWhenMatching)
   {
      filtersToApply = parseMockFilters(filters);
      this.desiredFilterResultWhenMatching = desiredFilterResultWhenMatching;
   }

   private List<MockFilter> parseMockFilters(String[] mockFilters)
   {
      List<MockFilter> filters = new ArrayList<MockFilter>(mockFilters.length);

      for (String mockFilter : mockFilters) {
         filters.add(new RegexMockFilter(mockFilter));
      }

      return filters;
   }

   public boolean matchesFilters(String name, String desc)
   {
      for (MockFilter filter : filtersToApply) {
         if (filter.matches(name, desc)) {
            return desiredFilterResultWhenMatching;
         }
      }

      return !desiredFilterResultWhenMatching;
   }

   private static final class RegexMockFilter implements MockFilter
   {
      private static final Pattern CONSTRUCTOR_NAME_REGEX = Pattern.compile("<init>");
      private static final String[] ANY_PARAMS = {};

      private final Pattern nameRegex;
      private final String[] paramTypeNames;

      private RegexMockFilter(String filter)
      {
         int lp = filter.indexOf('(');
         int rp = filter.indexOf(')');

         if (lp < 0 && rp >= 0 || lp >= 0 && lp >= rp) {
            throw new IllegalArgumentException("Invalid filter: " + filter);
         }

         if (lp == 0) {
            nameRegex = CONSTRUCTOR_NAME_REGEX;
         }
         else {
            nameRegex = Pattern.compile(lp < 0 ? filter : filter.substring(0, lp));
         }

         paramTypeNames = parseParameterTypes(filter, lp, rp);
      }

      private String[] parseParameterTypes(String filter, int lp, int rp)
      {
         if (lp < 0) {
            return ANY_PARAMS;
         }
         else if (lp == rp - 1) {
            return null;
         }
         else {
            String[] typeNames = filter.substring(lp + 1, rp).split(",");

            for (int i = 0; i < typeNames.length; i++) {
               typeNames[i] = typeNames[i].trim();
            }

            return typeNames;
         }
      }

      public boolean matches(String name, String desc)
      {
         if (!nameRegex.matcher(name).matches()) {
            return false;
         }

         if (paramTypeNames == ANY_PARAMS) {
            return true;
         }
         else if (paramTypeNames == null) {
            return desc.charAt(1) == ')';
         }

         Type[] argTypes = Type.getArgumentTypes(desc);

         if (argTypes.length != paramTypeNames.length) {
            return false;
         }

         for (int i = 0; i < paramTypeNames.length; i++) {
            Type argType = argTypes[i];
            String paramTypeName = argType.getClassName();

            if (!paramTypeName.endsWith(paramTypeNames[i])) {
               return false;
            }
         }

         return true;
      }
   }
}
