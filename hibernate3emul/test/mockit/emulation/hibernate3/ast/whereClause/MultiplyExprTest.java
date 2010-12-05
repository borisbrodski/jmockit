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
import static org.junit.Assert.*;
import org.junit.*;

public final class MultiplyExprTest
{
   @Test
   public void parseSingleValue()
   {
      Tokens tokens = new Tokens("68");

      Expr expr = MultiplyExpr.parse(tokens);

      assertTrue(expr instanceof ConstantExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseMultiplication()
   {
      Tokens tokens = new Tokens("2 * 25");

      Expr expr = MultiplyExpr.parse(tokens);

      assertTrue(expr instanceof MultiplyExpr);
      assertFalse(tokens.hasNext());
      MultiplyExpr multExpr = (MultiplyExpr) expr;
      assertEquals(2, multExpr.unaryExprs.length);
      assertTrue(multExpr.unaryExprs[0] instanceof ConstantExpr);
      assertTrue(multExpr.unaryExprs[1] instanceof ConstantExpr);
      assertEquals(1, multExpr.operators.cardinality());
      assertEquals(1, multExpr.operators.length());
   }

   @Test
   public void parseDivision()
   {
      Tokens tokens = new Tokens("12.5 / 5");

      Expr expr = MultiplyExpr.parse(tokens);

      assertTrue(expr instanceof MultiplyExpr);
      assertFalse(tokens.hasNext());
      MultiplyExpr multExpr = (MultiplyExpr) expr;
      assertEquals(2, multExpr.unaryExprs.length);
      assertTrue(multExpr.unaryExprs[0] instanceof ConstantExpr);
      assertTrue(multExpr.unaryExprs[1] instanceof ConstantExpr);
      assertEquals(0, multExpr.operators.cardinality());
   }

   @Test
   public void parseMultiplicationsAndDivisions()
   {
      Tokens tokens = new Tokens("2 * 25 / 3.2 * -5 / -2");

      Expr expr = MultiplyExpr.parse(tokens);

      assertTrue(expr instanceof MultiplyExpr);
      assertFalse(tokens.hasNext());
      MultiplyExpr multExpr = (MultiplyExpr) expr;
      assertEquals(5, multExpr.unaryExprs.length);
      assertEquals(2, multExpr.operators.cardinality());
      assertTrue(multExpr.operators.get(0));
      assertTrue(multExpr.operators.get(2));
   }

   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(")");

      Expr expr = MultiplyExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithSyntaxError()
   {
      Tokens tokens = new Tokens("4*");
      MultiplyExpr.parse(tokens);
   }

   @Test
   public void parseAdditiveExpr()
   {
      Tokens tokens = new Tokens("5 / 2 + 3");

      Expr expr = MultiplyExpr.parse(tokens);

      assertTrue(expr instanceof MultiplyExpr);
      assertEquals(2, tokens.getPosition());
   }

   @Test
   public void evaluate()
   {
      Tokens tokens = new Tokens("2 * 25 / 3.2 * -5 / -2");
      MultiplyExpr expr = (MultiplyExpr) MultiplyExpr.parse(tokens);

      Object result = expr.evaluate(new QueryEval());

      assertEquals(0, new BigDecimal("39.0625").compareTo((BigDecimal) result));
   }
}