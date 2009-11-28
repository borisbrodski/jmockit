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

      assertLines(8, 11, 3);
      assertLine(8, 1, 1, 2);
      assertLine(9, 1, 1, 1);
      assertLine(11, 1, 1, 2);

      findMethodData(8, "simpleIf");
      assertPaths(2, 2, 2);
      assertMethodLines(8, 11);
      assertPath(4, 1);
      assertPath(4, 1);
   }

   @Test
   public void ifAndElse()
   {
      tested.ifAndElse(true);
      tested.ifAndElse(false);

      findMethodData(15, "ifAndElse");
      assertPaths(2, 2, 2);
      assertMethodLines(15, 21);
      assertPath(4, 1);
      assertPath(4, 1);
   }

   @Test
   public void singleLineIf()
   {
      tested.singleLineIf(true);
      tested.singleLineIf(false);

      findMethodData(25, "singleLineIf");
      assertPaths(2, 2, 2);
      assertMethodLines(25, 26);
      assertPath(4, 1);
      assertPath(4, 1);
   }

   @Test
   public void singleLineIfAndElse()
   {
      tested.singleLineIfAndElse(true);
      tested.singleLineIfAndElse(false);

      findMethodData(30, "singleLineIfAndElse");
      assertPaths(2, 2, 2);
      assertMethodLines(30, 31);
      assertPath(4, 1);
      assertPath(4, 1);
   }

   @Test
   public void methodWithFourDifferentPathsAndSimpleLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSimpleLines(true, 0);
      tested.methodWithFourDifferentPathsAndSimpleLines(false, 1);

      findMethodData(35, "methodWithFourDifferentPathsAndSimpleLines");
      assertPaths(4, 2, 2);
      assertMethodLines(35, 45);
      assertPath(6, 0);
      assertPath(6, 1);
      assertPath(6, 1);
      assertPath(6, 0);
   }

   @Test
   public void methodWithFourDifferentPathsAndSegmentedLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSegmentedLines(false, -1);
      tested.methodWithFourDifferentPathsAndSegmentedLines(true, 1);

      findMethodData(49, "methodWithFourDifferentPathsAndSegmentedLines");
      assertPaths(4, 2, 2);
      assertMethodLines(49, 53);
      assertPath(6, 1);
      assertPath(6, 0);
      assertPath(6, 0);
      assertPath(6, 1);
   }

   @Test
   public void singleLineMethodWithMultiplePaths()
   {
      tested.singleLineMethodWithMultiplePaths(true, false);

      findMethodData(58, "singleLineMethodWithMultiplePaths");
      assertPaths(3, 1, 1);
      assertMethodLines(58, 61);
      assertPath(6, 0);
      assertPath(5, 1);
      assertPath(6, 0);
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

      findMethodData(68, "returnInput");
      assertPaths(8, 8, 8);
      assertMethodLines(68, 81);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
      assertPath(8, 1);
   }
}