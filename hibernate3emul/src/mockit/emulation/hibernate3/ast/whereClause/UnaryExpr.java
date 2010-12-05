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

import mockit.emulation.hibernate3.ast.*;

final class UnaryExpr extends Expr
{
   final Expr rhsExpr;

   UnaryExpr(Expr expr)
   {
      rhsExpr = expr;
   }

   // TODO: caseExpr, quantifiedExpr
   public static Expr parse(Tokens tokens)
   {
      if (tokens.hasNext()) {
         String token = tokens.next();
         char sign = token.charAt(0);

         if (sign == '-') {
            tokens.pushback();
            Expr rhsExpr = PrimaryExpr.parse(tokens);
            return rhsExpr instanceof ConstantExpr ? rhsExpr : new UnaryExpr(rhsExpr);
         }
         else if (sign == '+') {
            return PrimaryExpr.parse(tokens);
         }
         else {
            tokens.pushback();
            return PrimaryExpr.parse(tokens);
         }
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      Object value = rhsExpr.evaluate(eval);

      Number numericValue = (Number) value;

      if (numericValue instanceof BigInteger) {
         numericValue = ((BigInteger) numericValue).negate();
      }
      else if (numericValue instanceof BigDecimal) {
         numericValue = ((BigDecimal) numericValue).negate();
      }
      else if (numericValue instanceof Float) {
         numericValue = -numericValue.floatValue();
      }
      else if (numericValue instanceof Double) {
         numericValue = -numericValue.doubleValue();
      }
      else if (numericValue instanceof Integer) {
         numericValue = -numericValue.intValue();
      }
      else if (numericValue instanceof Short) {
         numericValue = -numericValue.shortValue();
      }
      else if (numericValue instanceof Byte) {
         numericValue = -numericValue.byteValue();
      }
      else {
         numericValue = -numericValue.longValue();
      }

      return numericValue;
   }
}
