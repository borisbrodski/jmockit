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
package mockit.emulation.hibernate3.ast.fromClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class FromClassOrOuterQueryPath
{
   final boolean isFQName;
   final String entityClassName;
   final String alias;
   final List<Object> result;
   FromJoin join;

   FromClassOrOuterQueryPath(String path, String alias)
   {
      isFQName = path.contains(".");
      entityClassName = path;
      this.alias = alias;
      result = new ArrayList<Object>();
   }

   static FromClassOrOuterQueryPath parse(Tokens tokens)
   {
      if (tokens.hasNext()) {
         PathAndAlias path = new PathAndAlias(tokens);
         return new FromClassOrOuterQueryPath(path.path, path.alias);
      }

      throw new QuerySyntaxException(tokens);
   }

   int depth()
   {
      return 1 + (join == null ? 0 : join.depth());
   }

   int tupleCount()
   {
      if (join == null) {
         return result.size();
      }
      else {
         int count = 0;

         for (Object entity : result) {
            count += join.tupleCount(entity);
         }

         return count;
      }
   }

   void matches(Collection<?> entities)
   {
      for (Object entity : entities) {
         Class<?> entityClass = entity.getClass();
         String className = isFQName ? entityClass.getName() : entityClass.getSimpleName();

         if (className.equals(entityClassName) && (join == null || join.matches(entity, alias))) {
            result.add(entity);
         }
      }
   }

   public int columnIndex(String alias)
   {
      if (this.alias.equals(alias)) {
         return 0;
      }
      else if (join != null) {
         return join.columnIndex(alias);
      }
      else {
         return -1;
      }
   }

   void getAliases(Map<String, Object> aliasToValue)
   {
      aliasToValue.put(alias, null);

      FromJoin join = this.join;

      while (join != null) {
         aliasToValue.put(join.pathAndAlias.alias, null);
         join = join.nextJoin;
      }
   }
}