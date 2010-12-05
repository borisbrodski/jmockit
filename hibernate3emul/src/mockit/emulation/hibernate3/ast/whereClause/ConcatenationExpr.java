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

final class ConcatenationExpr extends Expr
{
   final Expr[] additiveExprs;

   ConcatenationExpr(List<Expr> additiveExprs)
   {
      this.additiveExprs = additiveExprs.toArray(new Expr[additiveExprs.size()]);
   }

   public static Expr parse(Tokens tokens)
   {
      int pos = tokens.getPosition();
      Expr firstExpr = AdditiveExpr.parse(tokens);

      if (firstExpr == null) {
         tokens.setPosition(pos);
         return null;
      }

      List<Expr> additiveExprs = new LinkedList<Expr>();
      additiveExprs.add(firstExpr);

      while (tokens.hasNext()) {
         if ("||".equals(tokens.next())) {
            Expr nextExpr = AdditiveExpr.parse(tokens);

            if (nextExpr == null) {
               throw new QuerySyntaxException(tokens);
            }

            additiveExprs.add(nextExpr);
         }
         else {
            tokens.pushback();
            break;
         }
      }

      return additiveExprs.size() == 1 ? firstExpr : new ConcatenationExpr(additiveExprs);
   }

   @Override
   public String evaluate(QueryEval eval)
   {
      StringBuilder result = new StringBuilder();

      for (Expr expr : additiveExprs) {
         result.append(expr.evaluate(eval));
      }

      return result.toString();
   }
}