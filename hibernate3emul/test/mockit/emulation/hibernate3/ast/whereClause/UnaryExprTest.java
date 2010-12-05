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

public final class UnaryExprTest
{
   @Test
   public void parseWithoutMinusOrPlusSign()
   {
      Tokens tokens = new Tokens("o.code");

      Expr expr = UnaryExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithPlusSign()
   {
      Tokens tokens = new Tokens("+40");

      Expr expr = UnaryExpr.parse(tokens);

      assertTrue(expr instanceof ConstantExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithMinusSign()
   {
      Tokens tokens = new Tokens("-3.56");

      Expr expr = UnaryExpr.parse(tokens);

      assertTrue(expr instanceof ConstantExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(")");

      Expr expr = UnaryExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test
   public void parseEmpty()
   {
      Tokens tokens = new Tokens("");

      Expr expr = UnaryExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test
   public void evaluateMinusInteger()
   {
      int value = 602;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, result);
   }

   private Object evaluateUnaryExpr(Object value)
   {
      UnaryExpr expr = new UnaryExpr(new ConstantExpr(value));
      return expr.evaluate(new QueryEval());
   }

   @Test
   public void evaluateMinusNegativeInteger()
   {
      Expr expr = UnaryExpr.parse(new Tokens("-602"));
      Object result = expr.evaluate(new QueryEval());
      assertEquals(-602, result);
   }

   @Test
   public void evaluateMinusLong()
   {
      long value = 602L;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, result);
   }

   @Test
   public void evaluateMinusShort()
   {
      short value = 602;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, result);
   }

   @Test
   public void evaluateMinusByte()
   {
      byte value = 102;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, result);
   }

   @Test
   public void evaluateMinusFloat()
   {
      float value = 602.0F;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, (Float) result, 0.0F);
   }

   @Test
   public void evaluateMinusDouble()
   {
      double value = 602.0;
      Object result = evaluateUnaryExpr(value);
      assertEquals(-value, (Double) result, 0.0);
   }

   @Test
   public void evaluateMinusBigInteger()
   {
      BigInteger value = new BigInteger("602");
      Object result = evaluateUnaryExpr(value);
      assertEquals(0, value.negate().compareTo((BigInteger) result));
   }

   @Test
   public void evaluateMinusBigDecimal()
   {
      BigDecimal value = new BigDecimal("602");
      Object result = evaluateUnaryExpr(value);
      assertEquals(0, value.negate().compareTo((BigDecimal) result));
   }
}