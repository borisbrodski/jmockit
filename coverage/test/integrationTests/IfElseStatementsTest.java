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

public final class IfElseStatementsTest extends CoverageTest
{
   IfElseStatements tested;

   @Before
   public void setUp()
   {
      tested = new IfElseStatements();
   }

   @Test
   public void simpleIf()
   {
      tested.simpleIf(true);
      tested.simpleIf(false);

      findMethodData(tested, "simpleIf");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(8, 11);

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void ifAndElse()
   {
      tested.ifAndElse(true);
      tested.ifAndElse(false);

      findMethodData(tested, "ifAndElse");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(15, 21);

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void singleLineIf()
   {
      tested.singleLineIf(true);
      tested.singleLineIf(false);

      findMethodData(tested, "singleLineIf");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(25, 26);

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void singleLineIfAndElse()
   {
      tested.singleLineIfAndElse(true);
      tested.singleLineIfAndElse(false);

      findMethodData(tested, "singleLineIfAndElse");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(30, 31);

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void methodWithFourDifferentPathsAndSimpleLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSimpleLines(true, 0);
      tested.methodWithFourDifferentPathsAndSimpleLines(false, 1);

      findMethodData(tested, "methodWithFourDifferentPathsAndSimpleLines");
      assertEquals(4, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(35, 45);

      assertPath(4, 0); // should there be 5 nodes?
      assertPath(4, 1);
      assertPath(4, 1);
      assertPath(4, 0);
   }

   @Test
   public void methodWithFourDifferentPathsAndSegmentedLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSegmentedLines(false, -1);
      tested.methodWithFourDifferentPathsAndSegmentedLines(true, 1);

      findMethodData(tested, "methodWithFourDifferentPathsAndSegmentedLines");
      assertEquals(4, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertMethodLines(49, 53);

      assertPath(4, 1);
      assertPath(4, 0);
      assertPath(4, 0);
      assertPath(4, 1);
   }

   @Test
   public void singleLineMethodWithMultiplePaths()
   {
      tested.singleLineMethodWithMultiplePaths(true, false);

      findMethodData(tested, "singleLineMethodWithMultiplePaths");
      assertEquals(3, methodData.paths.size());
      assertEquals(1, methodData.getCoveredPaths());
      assertEquals(1, methodData.getExecutionCount());
      assertMethodLines(58, 61);

      assertPath(4, 0);
      assertPath(4, 1);
      assertPath(4, 0);
   }

   @Test
   public void returnInput()
   {
      assertEquals(2, tested.returnInput(1, true, false, false));
      assertEquals(2, tested.returnInput(2, false, false, false));
      assertEquals(2, tested.returnInput(3, false, true, false));
      assertEquals(4, tested.returnInput(4, false, false, true));
      assertEquals(5, tested.returnInput(5, true, true, false));
      assertEquals(5, tested.returnInput(6, false, true, true));
      assertEquals(7, tested.returnInput(7, true, true, true));
      assertEquals(9, tested.returnInput(8, true, false, true));

      findMethodData(tested, "returnInput");
      assertEquals(8, methodData.paths.size());
      assertEquals(8, methodData.getCoveredPaths());
      assertEquals(8, methodData.getExecutionCount());
      assertMethodLines(68, 81);

      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 1);
   }
}