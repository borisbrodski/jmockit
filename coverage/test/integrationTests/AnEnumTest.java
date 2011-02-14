/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class AnEnumTest extends CoverageTest
{
   AnEnum tested;

   @Test
   public void useAnEnum()
   {
      tested = AnEnum.OneValue;

      assertEquals(100, fileData.getCodeCoveragePercentage());
      assertEquals(100, fileData.getPathCoveragePercentage());
   }
}