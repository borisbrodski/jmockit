/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

// The tests in this class probably aren't worth the trouble, but since we want to achieve maximum
// coverage...
public final class AnimationLayerTest
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
