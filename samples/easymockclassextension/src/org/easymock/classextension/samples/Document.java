/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

public class Document
{
   private final Printer printer;
   private String content;

   public Document(Printer printer)
   {
      this.printer = printer;
   }

   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   public void print()
   {
      printer.print(content);
   }
}
