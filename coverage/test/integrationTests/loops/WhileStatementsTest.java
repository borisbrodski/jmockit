/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests.loops;

import org.junit.*;

import integrationTests.*;

public final class WhileStatementsTest extends CoverageTest
{
   WhileStatements tested;

   @Test
   public void whileBlockInSeparateLines()
   {
      tested.whileBlockInSeparateLines();

      assertLines(7, 12, 4);
      assertLine(7, 1, 1, 1);
      assertLine(9, 2, 2, 6);
      assertLine(10, 1, 1, 5);
      assertLine(12, 1, 1, 1);

      findMethodData(7, "whileBlockInSeparateLines");
      assertMethodLines(7, 12);
      assertPaths(2, 1, 1);
      assertPath(4, 0);
      assertPath(5, 1);
   }

   @Test
   public void whileBlockInSingleLine()
   {
      tested.whileBlockInSingleLine(0);
      tested.whileBlockInSingleLine(1);
      tested.whileBlockInSingleLine(2);

      assertLines(15, 16, 2);
      assertLine(15, 2, 2, 6);
      assertLine(16, 1, 1, 3);

      findMethodData(15, "whileBlockInSingleLine");
      assertMethodLines(15, 16);
      // TODO: fix
//      assertPaths(2, 2, 3);
//      assertPath(3, 2);
//      assertPath(3, 1);
   }

   @Test
   public void doWhileInSeparateLines()
   {
      tested.doWhileInSeparateLines();

      assertLines(58, 63, 4);
      assertLine(58, 1, 1, 1);
      assertLine(61, 1, 1, 3);
      assertLine(62, 1, 1, 3);
      assertLine(63, 1, 1, 1);

      findMethodData(58, "doWhileInSeparateLines");
      assertMethodLines(58, 63);
//      assertPaths(2, 1, 3);
//      assertPath(4, 0);
//      assertPath(4, 0);
   }
}
