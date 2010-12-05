/*
 * JMockit Hibernate 3 Emulation
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
package mockit.emulation.hibernate3.ast;

import java.util.*;

final class SelectClause
{
   final boolean distinct;
   final String constructorName;
   final List<PathAndAlias> selectedProperties;

   private SelectClause(
      boolean distinct, String constructorName, List<PathAndAlias> selectedProperties)
   {
      this.distinct = distinct;
      this.selectedProperties = selectedProperties;
      this.constructorName = constructorName;
   }

   static SelectClause parse(Tokens tokens)
   {
      if (!"select".equalsIgnoreCase(tokens.next())) {
         tokens.pushback();
         return null;
      }

      String token = tokens.next();
      boolean distinct = "distinct".equalsIgnoreCase(token);

      if (!distinct) {
         tokens.pushback();
      }

      token = tokens.next();
      String constructorName;
      List<PathAndAlias> seletedProperties;

      if ("new".equalsIgnoreCase(token)) {
         constructorName = PathAndAlias.parsePath(tokens);
         tokens.next("(");
         seletedProperties = parseSelectedPropertiesList(tokens);
         tokens.next(")");
      }
      else {
         tokens.pushback();
         constructorName = null;
         seletedProperties = parseSelectedPropertiesList(tokens);
      }

      return new SelectClause(distinct, constructorName, seletedProperties);
   }

   private static List<PathAndAlias> parseSelectedPropertiesList(Tokens tokens)
   {
      PathAndAlias pathAndAlias = PathAndAlias.parse(tokens);
      if (pathAndAlias == null) return null;

      List<PathAndAlias> seletedProperties = new LinkedList<PathAndAlias>();
      seletedProperties.add(pathAndAlias);

      while (true) {
         char c = tokens.nextChar();

         if (c != ',') {
            tokens.pushback();
            break;
         }

         pathAndAlias = PathAndAlias.parse(tokens);
         seletedProperties.add(pathAndAlias);
      }

      return seletedProperties;
   }
}
