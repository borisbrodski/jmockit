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
package integrationTests.loops;

import org.junit.*;

import integrationTests.*;

public final class WhileStatementsTest extends CoverageTest
{
   WhileStatements tested = new WhileStatements();

   @Test
   public void whileBlockInSeparateLines()
   {
      tested.whileBlockInSeparateLines();

      assertLines(7, 12, 4);
      assertLine(7, 1, 1, 1);
      assertLine(9, 1, 1, 6);
      assertLine(10, 1, 1, 5);
      assertLine(12, 1, 1, 1);

      findMethodData(7, "whileBlockInSeparateLines");
      assertMethodLines(7, 12);
      assertPaths(2, 1, 1);
      assertPath(4, 0);
      assertPath(5, 1);
   }

   @Ignore @Test
   public void whileBlockInSingleLine()
   {
//      tested.whileBlockInSingleLine(0);
      tested.whileBlockInSingleLine(1);
//      tested.whileBlockInSingleLine(2);

      assertLines(15, 16, 2);
//      assertLine(15, 2, 2, 6); // TODO: fix
      assertLine(16, 1, 1, 3);

      findMethodData(15, "whileBlockInSingleLine");
      assertMethodLines(15, 16);
      assertPaths(2, 2, 3);
//      assertPath(3, 2);
//      assertPath(3, 1);
   }
}
