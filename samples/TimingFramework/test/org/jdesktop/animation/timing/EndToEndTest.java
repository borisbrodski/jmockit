/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.timing;

import java.awt.*;
import javax.swing.*;

import org.jdesktop.animation.timing.interpolation.*;

import org.junit.*;
import static org.junit.Assert.*;

public final class EndToEndTest
{
   static JButton animated;

   @BeforeClass
   public static void createUI() throws Exception
   {
      ImageIcon icon = new ImageIcon("../../www/javadoc/resources/logo.png");
      animated = new JButton("JMockit", icon);
      animated.setSize(icon.getIconWidth() + 90, icon.getIconHeight() + 10);

      JFrame mainWindow = new JFrame("Timing Framework");
      mainWindow.setLayout(null);
      mainWindow.add(animated);
      mainWindow.setBounds(300, 200, 600, 400);
      mainWindow.setVisible(true);
   }

   @Test
   public void animateButtonPositionInWindowThroughPropertySetter() throws Exception
   {
      Animator animator = new Animator(5000);
      Point finalLocation = new Point(200, 320);
      animator.addTarget(
         new PropertySetter<Point>(
            animated, "location",
            new Point(10, 10), new Point(100, 60), new Point(450, 200), finalLocation));

      animator.start();
      Thread.sleep(animator.getDuration() + 100);

      assertFalse(animator.isRunning());
      assertEquals(finalLocation, animated.getLocation());
   }
}
