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
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class EqualityExpr extends Expr
{
   final Expr lhsExpr;
   final Expr rhsExpr;
   final String operator;

   EqualityExpr(Expr lhsExpr, Expr rhsExpr, String operator)
   {
      this.lhsExpr = lhsExpr;
      this.rhsExpr = rhsExpr;
      this.operator = operator;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr lhsExpr = RelationalExpr.parse(tokens);

      if (!tokens.hasNext()) {
         return lhsExpr;
      }

      String operator = tokens.next().toLowerCase();
      boolean equalityOperator;

      if ("is".equals(operator)) {
         equalityOperator = true;
         String nextToken = tokens.next().toLowerCase();

         if ("not".equals(nextToken)) {
            operator += nextToken;
         }
         else {
            tokens.pushback();
         }
      }
      else {
         equalityOperator = "=".equals(operator) || "!=".equals(operator) || "<>".equals(operator);
      }

      if (equalityOperator) {
         Expr rhsExpr = RelationalExpr.parse(tokens);

         if (rhsExpr == null) {
            throw new QuerySyntaxException(tokens);
         }

         return new EqualityExpr(lhsExpr, rhsExpr, operator);
      }
      else {
         tokens.pushback();
         return lhsExpr;
      }
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      Object value1 = lhsExpr.evaluate(eval);
      Object value2 = rhsExpr.evaluate(eval);

      if ("=".equals(operator)) {
         return value1.equals(value2);
      }
      else if ("!=".equals(operator) || "<>".equals(operator)) {
         return !value1.equals(value2);
      }
      else if ("is".equals(operator)) {
         if (value2 == null ) {
            return value1 == null;
         }
         else if (value2 == Collections.EMPTY_SET) {
            return ((Collection<?>) value1).isEmpty();
         }
         else {
            throw new IllegalStateException("IS operator not applied to null nor empty");
         }
      }
      else {
         assert "isnot".equals(operator);
         if (value2 == null) {
            return value1 != null;
         }
         else if (value2 == Collections.EMPTY_SET) {
            return !((Collection<?>) value1).isEmpty();
         }
         else {
            throw new IllegalStateException("IS NOT operator not applied to null nor empty");
         }
      }
   }
}
