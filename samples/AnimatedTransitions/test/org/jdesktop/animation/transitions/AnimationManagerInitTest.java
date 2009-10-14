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
import java.util.List;
import java.util.*;
import javax.swing.*;

import org.jdesktop.animation.timing.*;

import org.junit.*;

import static mockit.Deencapsulation.*;
import mockit.*;
import mockit.integration.junit4.*;

public final class AnimationManagerInitTest extends JMockitTest
{
   @Mocked private Animator animator;
   @Mocked({"createImage", "setVisible"}) private JComponent container;
   @Mocked private BufferedImage bgImage;
   @Mocked private AnimationState animationState;
   @Mocked({"(JComponent)", "get.+", "paintHierarchySingleBuffered"})
   private ComponentState componentState;

   private JComponent component;
   private AnimationManager manager;

   @Before
   public void setUp()
   {
      container = new JPanel();

      manager = new AnimationManager(container);
      container.setSize(100, 100);

      Map<JComponent, AnimationState> animationStates = getField(manager, Map.class);
      component = new JButton();
      animationStates.put(component, animationState);

      // Common initial expectation:
      new Expectations()
      {
         {
            container.createImage(100, 100); returns(bgImage);
         }
      };
   }

   @Test
   public void initForComponentWithStartStateOnly()
   {
      new Expectations()
      {
         {
            // Expect checking of states to remove those components completely outside the
            // container:
            animationState.getStart(); returns(componentState);
            animationState.getEnd(); returns(null);
         }
      };

      new ReadingOfComponentBounds();
      new PaintingOfBackgroundImageOnContainer();

      // Expect initialization of animation states:
      new Expectations()
      {
         {
            animationState.init(animator);
         }
      };

      manager.init(animator);
   }

   final class ReadingOfComponentBounds extends NonStrictExpectations
   {{
      componentState.getWidth(); returns(20);
      componentState.getHeight(); returns(10);
   }}

   final class PaintingOfBackgroundImageOnContainer extends NonStrictExpectations
   {
      Graphics gImg;

      {
         bgImage.getGraphics(); returns(gImg);
         gImg.clearRect(0, 0, bgImage.getWidth(), bgImage.getHeight());
         ComponentState.paintHierarchySingleBuffered(
         withInstanceOf(JComponent.class), withSameInstance(gImg));
      }
   }

   @Test
   public void initForComponentWithEndStateOnly()
   {
      // Expect checking of states to remove those components completely outside the container:
      new Expectations()
      {
         {
            animationState.getStart(); returns(null);
            animationState.getEnd(); returns(componentState);
         }
      };

      new ReadingOfComponentBounds();
      new PaintingOfBackgroundImageOnContainer();

      new Expectations() {{ animationState.init(animator); }};

      manager.init(animator);
   }

   @Test
   public void initForComponentWithStartAndEndStates()
   {
      // Expect checking of states to remove those components completely outside the container:
      new Expectations()
      {
         {
            animationState.getStart(); returns(componentState);
            animationState.getEnd(); returns(componentState);
         }
      };

      new ReadingOfComponentBounds();
      new ReadingOfComponentBounds();
      new PaintingOfBackgroundImageOnContainer();

      new Expectations() {{ animationState.init(animator); }};

      manager.init(animator);
   }

   @Test
   public void initForComponentCompletelyOutsideTheContainer()
   {
      new NonStrictExpectations()
      {
         {
            animationState.getStart(); returns(componentState);
            componentState.getX(); returns(-10);
            componentState.getY(); returns(-8);
            componentState.getWidth(); returns(8);
            componentState.getHeight(); returns(6);
         }
      };

      new PaintingOfBackgroundImageOnContainer();

      manager.init(animator);

      new Verifications()
      {
         {
            animationState.init(animator);
         }
      };
   }

   @Test
   public void initForChangingComponent()
   {
      List<JComponent> changingComponents = getField(manager, List.class);
      changingComponents.add(component);

      new Expectations()
      {
         {
            animationState.getStart(); returns(componentState);
            animationState.getEnd(); returns(null);
         }
      };

      new ReadingOfComponentBounds();

      component.setVisible(false);
      new PaintingOfBackgroundImageOnContainer();
      component.setVisible(true);

      new Expectations() {{ animationState.init(animator); }};

      // TODO: in this test, sometimes during GC the JVM thread finalizer decides to finalize
      // Graphics objects, causing spurious expectations to be recorded; implement checks to prevent
      // threads other than the the thread running the test to record or verify expectations, while
      // allowing invocations in the replay phase from any user thread (but not from internal JVM
      // threads such as the finalizer thread)

      manager.init(animator);
   }
}
