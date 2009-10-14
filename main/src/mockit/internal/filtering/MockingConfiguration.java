/*
 * JMockit
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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

import java.util.regex.*;
import java.util.*;

import org.objectweb.asm2.*;
import mockit.internal.util.*;

public final class MockingConfiguration
{
   private final List<MockFilter> filtersToApply;
   private final boolean desiredFilterResultWhenMatching;
   private String superClassName;
   private MockFilter lastFilterMatched;

   public MockingConfiguration(List<MockFilter> filters, boolean desiredFilterResultWhenMatching)
   {
      filtersToApply = filters;
      this.desiredFilterResultWhenMatching = desiredFilterResultWhenMatching;
   }

   public MockingConfiguration(String[] filters, boolean desiredFilterResultWhenMatching)
   {
      filtersToApply = parseMockFilters(filters);
      this.desiredFilterResultWhenMatching = desiredFilterResultWhenMatching;
   }

   private List<MockFilter> parseMockFilters(String[] mockFilters)
   {
      if (mockFilters == null || mockFilters.length == 0) {
         return null;
      }

      List<MockFilter> filters = new ArrayList<MockFilter>(mockFilters.length);

      for (String mockFilter : mockFilters) {
         filters.add(new RegexMockFilter(mockFilter));
      }

      return filters;
   }

   public boolean isEmpty()
   {
      return filtersToApply == null;
   }

   public void setSuperClassName(String superClassName)
   {
      this.superClassName = superClassName;
   }

   public boolean matchesFilters(String name, String desc)
   {
      if (filtersToApply == null) {
         return true;
      }

      for (MockFilter filter : filtersToApply) {
         if (filter.matches(name, desc)) {
            lastFilterMatched = filter;
            return desiredFilterResultWhenMatching;
         }
      }

      lastFilterMatched = null;
      return !desiredFilterResultWhenMatching;
   }

   public Type[] getSuperConstructorParameterTypes()
   {
      RegexMockFilter regexFilterMatched;
      int superConstructorNo;

      if (lastFilterMatched instanceof RegexMockFilter) {
         regexFilterMatched = (RegexMockFilter) lastFilterMatched;
         superConstructorNo = regexFilterMatched.superConstructorNo;
      }
      else {
         regexFilterMatched = null;
         superConstructorNo = 1;
      }

      if (superConstructorNo > 0) {
         String constructorDesc =
            new SuperConstructorCollector(superConstructorNo).findConstructor(superClassName);

         return Type.getArgumentTypes(constructorDesc);
      }

      //noinspection ConstantConditions
      if (regexFilterMatched.paramsForSuperConstructor == null) {
         return null;
      }

      String[] params = regexFilterMatched.paramsForSuperConstructor;
      Type[] types = new Type[params.length];

      for (int i = 0; i < params.length; i++) {
         types[i] = getParameterType(params[i]);
      }

      return types;
   }

   private Type getParameterType(String param)
   {
      if ("boolean".equals(param)) {
         return Type.BOOLEAN_TYPE;
      }
      else if ("char".equals(param)) {
         return Type.CHAR_TYPE;
      }
      else if ("byte".equals(param)) {
         return Type.BYTE_TYPE;
      }
      else if ("short".equals(param)) {
         return Type.SHORT_TYPE;
      }
      else if ("int".equals(param)) {
         return Type.INT_TYPE;
      }
      else if ("long".equals(param)) {
         return Type.LONG_TYPE;
      }
      else if ("float".equals(param)) {
         return Type.FLOAT_TYPE;
      }
      else if ("double".equals(param)) {
         return Type.DOUBLE_TYPE;
      }
      else if (param.endsWith("[]")) {
         // TODO: handle arrays
         return null;
      }
      else {
         return getReferenceParameterType(param);
      }
   }

   private Type getReferenceParameterType(String param)
   {
      String fqParameterName = param.indexOf('.') < 0 ? "java.lang." + param : param;
      String parameterDesc = 'L' + fqParameterName.replace('.', '/') + ';';
      return Type.getType(parameterDesc);
   }

   private static final class RegexMockFilter implements MockFilter
   {
      private static final Pattern CONSTRUCTOR_NAME_REGEX = Pattern.compile("<init>");
      private static final String[] ANY_PARAMS = {};

      private final Pattern nameRegex;
      private final String[] paramTypeNames;
      private String[] paramsForSuperConstructor;
      private int superConstructorNo;

      private RegexMockFilter(String filter)
      {
         int lp = filter.indexOf('(');
         int rp = filter.indexOf(')');

         if (lp < 0 && rp >= 0 || lp >= 0 && lp >= rp) {
            throw new IllegalArgumentException("Invalid filter: " + filter);
         }

         if (lp == 0) {
            nameRegex = CONSTRUCTOR_NAME_REGEX;
            parseSuperConstructorSpecification(filter);
         }
         else {
            nameRegex = Pattern.compile(lp < 0 ? filter : filter.substring(0, lp));
         }

         paramTypeNames = parseParameterTypes(filter, lp, rp);
      }

      private void parseSuperConstructorSpecification(String filter)
      {
         int cp = filter.indexOf(':');

         if (cp < 0) {
            superConstructorNo = 1;
            return;
         }

         String specification = filter.substring(cp + 1).trim();
         int rp = specification.length() - 1;

         if (specification.charAt(0) == '(' && specification.charAt(rp) == ')') {
            paramsForSuperConstructor = parseParameterTypes(specification, 0, rp);
         }
         else {
            try {
               superConstructorNo = Integer.parseInt(specification);
            }
            catch (NumberFormatException e) {
               throw new IllegalArgumentException(
                  "Invalid specification for super constructor in filter: " + filter, e);
            }
         }
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
            String[] paramTypeNames = filter.substring(lp + 1, rp).split(",");

            for (int i = 0; i < paramTypeNames.length; i++) {
               paramTypeNames[i] = paramTypeNames[i].trim();
            }

            return paramTypeNames;
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
