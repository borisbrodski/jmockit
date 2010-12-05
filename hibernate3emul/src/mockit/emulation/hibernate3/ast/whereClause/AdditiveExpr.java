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

import java.math.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class AdditiveExpr extends Expr
{
   final Expr[] multiplyExprs;
   final BitSet operators;

   AdditiveExpr(List<Expr> multiplyExprs, BitSet operators)
   {
      this.multiplyExprs = multiplyExprs.toArray(new Expr[multiplyExprs.size()]);
      this.operators = operators;
   }

   public static Expr parse(Tokens tokens)
   {
      int pos = tokens.getPosition();
      Expr firstExpr = MultiplyExpr.parse(tokens);

      if (firstExpr == null) {
         tokens.setPosition(pos);
         return null;
      }

      List<Expr> multiplyExprs = new LinkedList<Expr>();
      multiplyExprs.add(firstExpr);

      BitSet operators = new BitSet();

      for (int i = 0; tokens.hasNext(); i++) {
         char operator = tokens.nextChar();

         if (operator == '+' || operator == '-') {
            Expr nextExpr = MultiplyExpr.parse(tokens);

            if (nextExpr == null) {
               throw new QuerySyntaxException(tokens);
            }

            multiplyExprs.add(nextExpr);

            if (operator == '+') {
               operators.set(i);
            }
         }
         else {
            tokens.pushback();
            break;
         }
      }

      return multiplyExprs.size() == 1 ? firstExpr : new AdditiveExpr(multiplyExprs, operators);
   }

   @Override
   public BigDecimal evaluate(QueryEval eval)
   {
      Object firstValue = multiplyExprs[0].evaluate(eval);
      BigDecimal result = new BigDecimal(firstValue.toString());

      for (int i = 1; i < multiplyExprs.length; i++) {
         Object nextValue = multiplyExprs[i].evaluate(eval);
         BigDecimal operand = new BigDecimal(nextValue.toString());
         result = operators.get(i - 1) ? result.add(operand) : result.subtract(operand);
      }

      return result;
   }
}
