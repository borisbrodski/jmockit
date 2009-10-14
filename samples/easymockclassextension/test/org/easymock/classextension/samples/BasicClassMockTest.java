/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

import static org.easymock.classextension.EasyMock.*;
import org.junit.*;

/**
 * Example of how to use <code>MockClassControl</code>.
 */
public final class BasicClassMockTest
{
   private Printer printer;
   private Document document;

   @Before
   public void setUp()
   {
      printer = createMock(Printer.class);
      document = new Document(printer);
   }

   @Test
   public void testPrintContent()
   {
      printer.print("Hello world");
      replay(printer);

      document.setContent("Hello world");
      document.print();

      verify(printer); // make sure Printer.print was called
   }

   @Test
   public void testPrintEmptyContent()
   {
      printer.print("");
      replay(printer);

      document.setContent("");
      document.print();

      verify(printer); // make sure Printer.print was called
   }
}
