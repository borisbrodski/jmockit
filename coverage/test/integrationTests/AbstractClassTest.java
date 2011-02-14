/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class AbstractClassTest extends CoverageTest
{
   AbstractClassWithNoExecutableLines tested;

   @Before
   public void setUp()
   {
      tested = new AbstractClassWithNoExecutableLines()
      {
         @Override void doSomething(String s, boolean b) {}
         @Override int returnValue() { return 0; }
      };
   }

   @Test
   public void useAbstractClass()
   {
      tested.doSomething("test", true);
      tested.returnValue();

      assertEquals(1, fileData.lineToLineData.size());
      assertLines(3, 3, 1);
      assertEquals(100, fileData.getCodeCoveragePercentage());

      assertEquals(1, fileData.firstLineToMethodData.size());
      findMethodData(3, AbstractClassWithNoExecutableLines.class.getSimpleName());
      assertMethodLines(3, 3);
      assertPaths(1, 1, 1);
      assertPath(2, 1);
      assertEquals(100, fileData.getPathCoveragePercentage());
   }
}