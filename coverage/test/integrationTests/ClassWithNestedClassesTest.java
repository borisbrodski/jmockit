/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class ClassWithNestedClassesTest extends CoverageTest
{
   final ClassWithNestedClasses tested = null;

   @Test
   public void exerciseNestedClasses()
   {
      ClassWithNestedClasses.doSomething();

      assertEquals(9, fileData.lineToLineData.size());
      assertEquals(44, fileData.getCodeCoveragePercentage());
      assertEquals(9, fileData.getTotalSegments());
      assertEquals(4, fileData.getCoveredSegments());

      assertEquals(5, fileData.firstLineToMethodData.size());
      assertEquals(60, fileData.getPathCoveragePercentage());
      assertEquals(5, fileData.getTotalPaths());
      assertEquals(3, fileData.getCoveredPaths());
   }
}
