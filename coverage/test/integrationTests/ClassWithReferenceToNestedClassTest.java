/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.Test;

public final class ClassWithReferenceToNestedClassTest extends CoverageTest
{
   final ClassWithReferenceToNestedClass tested = null;

   @Test
   public void exerciseOnePathOfTwo()
   {
      ClassWithReferenceToNestedClass.doSomething();

      assertEquals(2, fileData.lineToLineData.size());
      assertEquals(50, fileData.getCodeCoveragePercentage());
      assertEquals(2, fileData.getTotalSegments());
      assertEquals(1, fileData.getCoveredSegments());

      assertEquals(2, fileData.firstLineToMethodData.size());
      assertEquals(50, fileData.getPathCoveragePercentage());
      assertEquals(2, fileData.getTotalPaths());
      assertEquals(1, fileData.getCoveredPaths());
   }
}