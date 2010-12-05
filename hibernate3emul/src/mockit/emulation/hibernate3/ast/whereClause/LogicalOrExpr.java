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

final class LogicalOrExpr extends Expr
{
   final List<Expr> andExprs;

   LogicalOrExpr(List<Expr> andExprs)
   {
      this.andExprs = andExprs;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr andExpr = LogicalAndExpr.parse(tokens);
      List<Expr> andExprs = new LinkedList<Expr>();
      andExprs.add(andExpr);

      while (tokens.hasNext()) {
         if (!"or".equals(tokens.next())) {
            tokens.pushback();
            break;
         }

         andExpr = LogicalAndExpr.parse(tokens);

         if (andExpr == null) {
            throw new QuerySyntaxException(tokens);
         }

         andExprs.add(andExpr);
      }

      return andExprs.size() == 1 ? andExpr : new LogicalOrExpr(andExprs);
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      for (Expr andExpr : andExprs) {
         if ((Boolean) andExpr.evaluate(eval)) {
            return true;
         }
      }

      return false;
   }
}
