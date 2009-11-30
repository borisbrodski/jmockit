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
package integrationTests.otherControlStructures;

import org.junit.*;

import integrationTests.*;

public final class SwitchStatementsTest extends CoverageTest
{
   final SwitchStatements tested = new SwitchStatements();

   @Test
   public void switchStatementWithSparseCasesAndDefault()
   {
      tested.switchStatementWithSparseCasesAndDefault('A');
      tested.switchStatementWithSparseCasesAndDefault('\0');

      assertLines(7, 22, 5);
      assertLine(7, 1, 1, 2);
      assertLine(9, 1, 1, 1);
      assertLine(10, 1, 1, 1);
      assertLine(18, 1, 1, 1);
      assertLine(20, 1, 0, 0);
      assertLine(22, 1, 1, 1);

      findMethodData(7, "switchStatementWithSparseCasesAndDefault");
      assertMethodLines(7, 22);
      assertPaths(4, 2, 2);
      assertPath(7, 1);
      assertPath(7, 0);
      assertPath(5, 1);
      assertPath(5, 0);
   }

   @Test
   public void switchStatementWithSparseCasesAndDefaultOnDefaultCase()
   {
      try {
         tested.anotherSwitchStatementWithSparseCasesAndDefault('x');
      }
      catch (IllegalArgumentException e) {
         // OK
      }

      findMethodData(26, "anotherSwitchStatementWithSparseCasesAndDefault");
      assertPaths(2, 1, 1);
   }

   @Test
   public void switchStatementWithCompactCasesAndDefault()
   {
      tested.switchStatementWithCompactCasesAndDefault(2);
      tested.switchStatementWithCompactCasesAndDefault(4);

      findMethodData(37, "switchStatementWithCompactCasesAndDefault");
      assertPaths(4, 2, 2);
      assertPath(7, 0);
      assertPath(7, 1);
      assertPath(5, 1);
      assertPath(5, 0);
   }

   @Test
   public void switchStatementWithCompactCasesAndDefaultOnDefaultCase()
   {
      try {
         tested.anotherSwitchStatementWithCompactCasesAndDefault(1);
         tested.anotherSwitchStatementWithCompactCasesAndDefault(5);
      }
      catch (IllegalArgumentException e) {
         // OK
      }

      findMethodData(56, "anotherSwitchStatementWithCompactCasesAndDefault");
      assertPaths(2, 2, 2);
   }

   @Test
   public void switchStatementWithSparseCasesAndNoDefault()
   {
      tested.switchStatementWithSparseCasesAndNoDefault('f');
      tested.switchStatementWithSparseCasesAndNoDefault('b');

      findMethodData(64, "switchStatementWithSparseCasesAndNoDefault");
      assertPaths(3, 2, 2);
      assertPath(7, 0);
      assertPath(7, 1);
      assertPath(5, 1);
   }

   @Test
   public void switchStatementWithCompactCasesAndNoDefault()
   {
      tested.switchStatementWithCompactCasesAndNoDefault(0);
      tested.switchStatementWithCompactCasesAndNoDefault(4);
      tested.switchStatementWithCompactCasesAndNoDefault(5);

      findMethodData(76, "switchStatementWithCompactCasesAndNoDefault");
      assertPaths(4, 2, 3);
      assertPath(5, 0);
      assertPath(5, 0);
      assertPath(7, 1);
      assertPath(5, 2);
   }

   @Test
   public void switchStatementWithExitInAllCases()
   {
      tested.switchStatementWithExitInAllCases(1);
      tested.switchStatementWithExitInAllCases(2);

      findMethodData(89, "switchStatementWithExitInAllCases");
      assertPaths(3, 2, 2);
      assertPath(5, 1);
      assertPath(5, 1);
      assertPath(5, 0);
   }
}