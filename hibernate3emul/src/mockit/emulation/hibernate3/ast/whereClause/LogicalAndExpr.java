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

final class LogicalAndExpr extends Expr
{
   final List<Expr> negatedExprs;

   LogicalAndExpr(List<Expr> negatedExprs)
   {
      this.negatedExprs = negatedExprs;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr negatedExpr = NegatedExpr.parse(tokens);
      List<Expr> negatedExprs = new LinkedList<Expr>();
      negatedExprs.add(negatedExpr);

      while (tokens.hasNext()) {
         if (!"and".equalsIgnoreCase(tokens.next())) {
            tokens.pushback();
            break;
         }

         negatedExpr = NegatedExpr.parse(tokens);

         if (negatedExpr == null) {
            throw new QuerySyntaxException(tokens);
         }

         negatedExprs.add(negatedExpr);
      }
      
      return negatedExprs.size() == 1 ? negatedExpr : new LogicalAndExpr(negatedExprs);
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      for (Expr expr : negatedExprs) {
         Boolean value = (Boolean) expr.evaluate(eval);

         if (value != null && !value) {
            return false;
         }
      }

      return true;
   }
}