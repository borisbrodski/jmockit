/*
 * JMockit Samples
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jdesktop.animation.transitions.effects;

import java.awt.*;

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import org.jdesktop.animation.transitions.*;

public final class MoveTest
{
   @Mocked Animator animator;

   @Test
   public void testInit(
      final ComponentState start, final ComponentState end,
      @Mocked("init") final Effect effectSuperClass)
   {
      final Move effect = new Move(start, end);

      new Expectations(PropertySetter.class)
      {
         {
            Point startPoint = new Point(start.getX(), start.getY());
            Point endPoint = new Point(end.getX(), end.getY());
            new PropertySetter<Point>(effect, "location", startPoint, endPoint);
            animator.addTarget(withInstanceOf(PropertySetter.class));
            effectSuperClass.init(animator, null);
         }
      };

      effect.init(animator, null);
   }

   @Test
   public void testInitWithParentEffect(
      final ComponentState start, final ComponentState end,
      @Mocked("init") final Effect effectSuperClass)
   {
      Move effect = new Move(start, end);
      final Unchanging parentEffect = new Unchanging();

      new Expectations(PropertySetter.class)
      {
         {
            Point startPoint = new Point(start.getX(), start.getY());
            Point endPoint = new Point(end.getX(), end.getY());
            new PropertySetter<Point>(parentEffect, "location", startPoint, endPoint);
            animator.addTarget(withInstanceOf(PropertySetter.class));
            effectSuperClass.init(animator, null);
         }
      };

      effect.init(animator, parentEffect);
   }

   @Test
   public void testCleanup()
   {
      new Expectations()
      {
         {
            animator.removeTarget(null);
         }
      };

      new Move().cleanup(animator);
   }
}
