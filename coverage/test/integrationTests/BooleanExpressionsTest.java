/*
 * JMockit Coverage
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
package integrationTests;

import org.junit.*;

public final class BooleanExpressionsTest extends CoverageTest
{
   final BooleanExpressions tested = new BooleanExpressions();

   @Test
   public void evalOnlySomeCombinations()
   {
      assertTrue(tested.eval1(true, true, 0));
      assertFalse(tested.eval1(true, false, 0));
   }

   @Test
   public void evalBuggyCombination()
   {
      // Only this combination will fail if the third condition in eval1 is changed to "z < 0",
      // which demonstrates that a more sophisticated metric than branch coverage is needed.
      assertTrue(tested.eval1(true, false, 1));
   }

   @Test
   public void evalAllCombinations()
   {
      assertTrue(tested.eval2(true, true, 0));
      assertTrue(tested.eval2(true, false, 1));
      assertFalse(tested.eval2(true, false, 0));
      assertFalse(tested.eval2(false, true, 0));
   }

   @Test
   public void evalOnlyFirstBranch()
   {
      assertFalse(tested.eval3(false, true, false));
   }

   @Test
   public void evalOnlyFirstAndSecondBranches()
   {
      assertFalse(tested.eval4(false, true, false));
      assertFalse(tested.eval4(false, false, false));
      assertFalse(tested.eval4(false, true, true));
      assertFalse(tested.eval4(false, false, true));
      assertTrue(tested.eval4(true, true, false));
      assertTrue(tested.eval4(true, true, true));
   }

   @Test
   public void methodWithComplexExpressionWhichCallsAnotherInSameClass()
   {
      BooleanExpressions.isSameTypeIgnoringAutoBoxing(int.class, Integer.class);

      findMethodData(27, "isSameTypeIgnoringAutoBoxing");
      assertPaths(8, 1, 1);

      findMethodData(35, "isWrapperOfPrimitiveType");
      assertPaths(31, 1, 1);
   }
}
