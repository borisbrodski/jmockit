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
import java.awt.image.*;
import javax.swing.*;

import static junit.framework.Assert.*;
import mockit.*;
import mockit.integration.junit4.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
@UsingMocksAndStubs(PropertySetter.class)
public final class ScreenTransitionExecutionTest
{
   @Mocked private JComponent container;
   @Mocked private TransitionTarget target;
   @Mocked private Animator animator;
   @Mocked private AnimationManager manager;
   @Mocked private AnimationLayer animationLayer;
   @Mocked(methods = "()", capture = 1) private TimingTarget timingTarget;

   private ScreenTransition transition;

   @Before
   public void createTransition()
   {
      transition = new ScreenTransition(container, target, animator);
   }

   @Test
   public void beginTransition(final Graphics2D g2D)
   {
      new NonStrictExpectations()
      {
         final int width = 200;
         final int height = 150;
         JRootPane rootPane;
         BufferedImage transitionImage;

         {
            container.getWidth(); returns(width);
            container.getHeight(); returns(height);

            container.createImage(width, height); returns(transitionImage);

            container.getRootPane(); returns(rootPane);
            transitionImage.getGraphics(); returns(g2D);
         }
      };

      timingTarget.begin();
      assertNotNull(transition.getTransitionImage());

      new VerificationsInOrder()
      {
         {
            manager.setupStart();
            animationLayer.setupBackground(container);
            target.setupNextScreen();
            manager.setupEnd();
            manager.init(animator);
            manager.paint(g2D);
         }
      };
   }

   @Test
   public void endTransition()
   {
      new NonStrictExpectations()
      {
         JRootPane rootPane;
         Component savedGlassPane;

         {
            container.getRootPane(); returns(rootPane);
            setField(transition, savedGlassPane);
         }
      };

      timingTarget.end();

      new Verifications()
      {
         {
            animationLayer.setVisible(false);
            container.setVisible(true);
            manager.reset(animator);
         }
      };
   }
}
