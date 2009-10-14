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
package org.jdesktop.animation.transitions;

import java.awt.*;
import javax.swing.*;

import mockit.*;
import mockit.integration.junit4.*;
import org.junit.*;

// The tests in this class probably aren't worth the trouble, but since we want to achieve maximum
// coverage...
public final class AnimationLayerTest extends JMockitTest
{
   @Test
   public void testSetupBackground()
   {
      final AnimationLayer animationLayer = new AnimationLayer(null);

      new Expectations()
      {
         JRootPane rootPane;
         @Mocked("getRootPane") JComponent component;
         JPanel glassPane;
         final SwingUtilities swingUtilities = null;

         {
            component.getRootPane(); returns(rootPane);
            rootPane.getGlassPane(); returns(glassPane);
            SwingUtilities.convertPoint(component, new Point(0, 0), glassPane);
            returns(new Point(10, 10));
            endRecording();

            animationLayer.setupBackground(component);

            Point componentLocation = getField(animationLayer, Point.class);
            assertEquals(new Point(10, 10), componentLocation);
         }
      };
   }

   @Test
   public void testPaintComponent(
      @Mocked(methods = "getTransitionImage", inverse = true) ScreenTransition screenTransition,
      @Mocked("()") Graphics graphics)
   {
      AnimationLayer animationLayer = new AnimationLayer(screenTransition);
      animationLayer.paintComponent(graphics);
   }
}
