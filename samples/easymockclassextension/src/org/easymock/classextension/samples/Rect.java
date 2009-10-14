/*
 * Copyright (c) 2003-2007 OFFIS, Henri Tremblay.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.classextension.samples;

public class Rect
{
   private int x;
   private int y;

   public int getX()
   {
      return x;
   }

   public void setX(int x)
   {
      this.x = x;
   }

   public int getY()
   {
      return y;
   }

   public void setY(int y)
   {
      this.y = y;
   }

   public int getArea()
   {
      return getX() * getY();
   }
}
