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

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class LogicalAndExprTest
{
   @Test
   public void parseWithOneTerm()
   {
      Tokens tokens = new Tokens("a.code <> 0");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertTrue(expr instanceof EqualityExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithThreeTerms()
   {
      Tokens tokens = new Tokens("a.code=1 and b.number < 5 and upper(ab.name) like 'Ab%'");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalAndExpr);
      LogicalAndExpr andExpr = (LogicalAndExpr) expr;
      assertEquals(3, andExpr.negatedExprs.size());
   }

   @Test
   public void parseWithOneTermContainingANot()
   {
      Tokens tokens = new Tokens("a.code >= 1 and a.code <= 9 and not 5 > b.number");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalAndExpr);
      LogicalAndExpr andExpr = (LogicalAndExpr) expr;
      assertEquals(3, andExpr.negatedExprs.size());
   }

   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(", test");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test
   public void parseOrFollowedByRestOfSomeBiggerExpression()
   {
      Tokens tokens = new Tokens("xyz.active )");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      assertEquals(0, tokens.getPosition());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithLastTermMissing()
   {
      Tokens tokens = new Tokens("xyz.active and ||");
      LogicalAndExpr.parse(tokens);
   }

   @Test
   public void evaluateTwoTerms()
   {
      LogicalAndExpr expr = (LogicalAndExpr) LogicalAndExpr.parse(new Tokens("2 < 3 and 2=2"));
      assertTrue(expr.evaluate(new QueryEval()));
   }

   @Test
   public void evaluateThreeTermsWithShortCircuit()
   {
      LogicalAndExpr expr =
         (LogicalAndExpr) LogicalAndExpr.parse(new Tokens("true and false and a > 0"));
      assertFalse(expr.evaluate(new QueryEval()));
   }

   @Test
   public void evaluateAsFalse()
   {
      Tokens tokens = new Tokens("false and 1 != 1.0");
      LogicalAndExpr expr = (LogicalAndExpr) LogicalAndExpr.parse(tokens);
      assertFalse(expr.evaluate(new QueryEval()));
   }
}