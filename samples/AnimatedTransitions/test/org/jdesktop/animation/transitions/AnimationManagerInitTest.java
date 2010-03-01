/*
 * JMockit Samples
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
import java.util.List;
import java.util.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static mockit.Deencapsulation.*;
import org.jdesktop.animation.timing.*;

public final class AnimationManagerInitTest
{
   @Mocked Animator animator;
   @Mocked AnimationState animationState;
   @Mocked ComponentState componentState;

   JComponent component;
   AnimationManager manager;

   @Before
   public void setUp()
   {
      final JComponent container = new JPanel();
      container.setSize(100, 100);

      manager = new AnimationManager(container);

      Map<JComponent, AnimationState> animationStates = getField(manager, Map.class);
      component = new JButton();
      animationStates.put(component, animationState);

      // Common stubbings:
      new NonStrictExpectations(container)
      {
         BufferedImage bgImage;
         Graphics gImg;

         {
            container.createImage(100, 100); result = bgImage;
            bgImage.getGraphics(); result = gImg;
         }
      };
   }

   @After
   public void verifyInitializationOfAnimationState()
   {
      new Verifications()
      {{
         animationState.init(animator);
      }};
   }

   @Test
   public void initForComponentWithStartStateOnly()
   {
      new NonStrictExpectations()
      {
         {
            // Expect checking of states to remove those components completely outside the
            // container:
            animationState.getStart(); result = componentState;
         }
      };

      manager.init(animator);
   }

   @Ignore @Test
   public void initForComponentWithEndStateOnly()
   {
      // Expect checking of states to remove those components completely outside the container:
      new NonStrictExpectations()
      {
         {
            animationState.getEnd(); result = componentState;
         }
      };

      manager.init(animator);
   }

   @Ignore @Test
   public void initForComponentWithStartAndEndStates()
   {
      // Expect checking of states to remove those components completely outside the container:
      new NonStrictExpectations()
      {
         {
            animationState.getStart(); result = componentState;
            animationState.getEnd(); result = componentState;
         }
      };

      manager.init(animator);
   }

   @Ignore @Test
   public void initForComponentCompletelyOutsideTheContainer()
   {
      new NonStrictExpectations()
      {
         {
            animationState.getStart(); result = componentState;
            componentState.getX(); result = -10;
            componentState.getY(); result = -8;
            componentState.getWidth(); result = 8;
            componentState.getHeight(); result = 6;
         }
      };

      manager.init(animator);
   }

   @Ignore @Test
   public void initForChangingComponent()
   {
      List<JComponent> changingComponents = getField(manager, List.class);
      changingComponents.add(component);

      new NonStrictExpectations()
      {
         {
            animationState.getStart(); result = componentState;
         }
      };

      manager.init(animator);
   }
}
