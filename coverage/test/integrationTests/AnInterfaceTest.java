/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class AnInterfaceTest extends CoverageTest
{
   AnInterface tested;

   @Before
   public void setUp()
   {
      tested = new AnInterface()
      {
         public void doSomething(String s, boolean b) {}
         public int returnValue() { return 0; }
      };
   }

   @Test
   public void useAnInterface()
   {
      tested.doSomething("test", true);

      assertTrue(fileData.lineToLineData.isEmpty());
      assertEquals(-1, fileData.getCodeCoveragePercentage());
      assertEquals(0, fileData.getTotalSegments());
      assertEquals(0, fileData.getCoveredSegments());

      assertTrue(fileData.firstLineToMethodData.isEmpty());
      assertEquals(-1, fileData.getPathCoveragePercentage());
      assertEquals(0, fileData.getTotalPaths());
      assertEquals(0, fileData.getCoveredPaths());
   }
}