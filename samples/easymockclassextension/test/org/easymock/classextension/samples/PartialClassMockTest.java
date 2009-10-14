/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

import static org.easymock.classextension.EasyMock.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Henri Tremblay
 */
public final class PartialClassMockTest
{
   private Rect rect;

   @Before
   public void setUp() throws Exception
   {
      rect = createMock(Rect.class, Rect.class.getMethod("getX"), Rect.class.getMethod("getY"));
   }

   @Test
   public void testGetArea()
   {
      expect(rect.getX()).andReturn(4);
      expect(rect.getY()).andReturn(5);
      replay(rect);

      assertEquals(20, rect.getArea());
      verify(rect);
   }
}
