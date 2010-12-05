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
import static org.junit.Assert.*;
import org.junit.*;

public final class ConstantExprTest
{
   @Test
   public void parseNUM_INT()
   {
      assertParseAndEvaluateConstant("4", 4);
   }

   @Test
   public void parseNUM_LONG()
   {
      assertParseAndEvaluateConstant("9876543210", 9876543210L);
   }

   @Test
   public void parseNUM_DOUBLE()
   {
      assertParseAndEvaluateConstant("987654321098.01", 987654321098.01);
   }

   @Test
   public void parseQUOTED_STRING()
   {
      assertParseAndEvaluateConstant("'Test'", "Test");
   }

   @Test
   public void parseNULL()
   {
      assertParseAndEvaluateConstant("null", null);
   }

   @Test
   public void parseTRUE()
   {
      assertParseAndEvaluateConstant("true", true);
   }

   @Test
   public void parseFALSE()
   {
      assertParseAndEvaluateConstant("false", false);
   }

   @Test
   public void parseEMPTY()
   {
      assertParseAndEvaluateConstant("empty", Collections.EMPTY_SET);
   }

   private void assertParseAndEvaluateConstant(String token, Object value)
   {
      ConstantExpr expr = ConstantExpr.parse(new Tokens(token));
      assertEquals(value, expr.value);
      assertEquals(value, expr.evaluate(new QueryEval()));
   }

   public void parseNotAKnownConstant()
   {
      assertNull(ConstantExpr.parse(new Tokens("jsdf")));
   }
}
