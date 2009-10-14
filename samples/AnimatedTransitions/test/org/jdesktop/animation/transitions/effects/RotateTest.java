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
import javax.swing.*;

import mockit.*;
import mockit.integration.junit4.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import org.jdesktop.animation.transitions.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class RotateTest
{
   @Mocked private Animator animator;
   @Mocked({"init", "setup"}) private Effect effect;

   @Test
   public void testInit(ComponentState start, ComponentState end)
   {
      final Rotate rotate = new Rotate(start, end, 45, 100, 60);

      new Expectations(PropertySetter.class)
      {
         {
            animator.addTarget(new PropertySetter(rotate, "radians", 0.0, Math.PI / 4));
            effect.init(animator, null);
         }
      };

      rotate.init(animator, null);
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

      new Rotate(0, 0, 0).cleanup(animator);
   }

   @Test
   public void testSetup(@Mocked({"translate", "rotate"}) final Graphics2D g2D)
   {
      JComponent component = new JButton();
      component.setSize(80, 60);
      Rotate rotate = new Rotate(90, component);
      rotate.setRadians(0.2);

      new Expectations()
      {
         {
            g2D.translate(40, 30);
            g2D.rotate(0.2);
            g2D.translate(-40, -30);
            effect.setup(g2D);
         }
      };

      rotate.setup(g2D);
   }
}
