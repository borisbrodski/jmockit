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
import static org.junit.Assert.*;

public final class AnimationManagerTest
{
   @BeforeClass
   public static void initializeAWTStaticState()
   {
      JComponent.getDefaultLocale();
   }

   @Test
   public void recreateImageForContainerOfSizeZero(final JComponent container)
   {
      // recreateImage() is called by the constructor.
      new AnimationManager(container);

      new Verifications()
      {
         {
            container.createImage(anyInt, anyInt); times = 0;
         }
      };
   }

   @Test
   public void recreateImageForContainerOfSizeNotZeroAndBackgroundStillUndefined(
      final JComponent container)
   {
      new NonStrictExpectations()
      {
         {
            container.getWidth(); result = 100;
            container.getHeight(); result = 80;
            container.createImage(100, 80); times = 1;
         }
      };

      // recreateImage() is called by the constructor.
      new AnimationManager(container);
   }

   @SuppressWarnings({"TooBroadScope"})
   @Test
   public void recreateImageForContainerOfSizeNotZeroAndBackgroundAlreadyDefined(
      final JComponent container, final BufferedImage transitionImageBG)
   {
      new Expectations()
      {
         {
            int cw = 100;
            int ch = 80;

            container.getWidth(); result = cw;
            container.getHeight(); result = ch;
            container.createImage(cw, ch);

            container.getWidth(); result = cw;
            container.getHeight(); result = ch;
            transitionImageBG.getWidth(); result = cw;
            transitionImageBG.getHeight(); result = ch;
         }
      };

      AnimationManager manager = new AnimationManager(container);
      setField(manager, transitionImageBG);
      manager.recreateImage();
   }

   @Test
   public void resetWhenEmpty(@Mocked("") JComponent container)
   {
      new AnimationManager(container).reset(null);
   }

   @Test
   public void resetWhenNotEmpty(@Mocked("") JComponent container)
   {
      final AnimationManager manager = new AnimationManager(container);

      new Expectations()
      {
         @Mocked("cleanup")
         final AnimationState animationState = new AnimationState(new JButton(), true);

         {
            manager.addStart(new JButton());

            animationState.cleanup(null);
         }
      };

      manager.reset(null);
      manager.reset(null); // check that a second call won't cleanup any AnimationState
   }

   @Test
   public void addStartStateForComponent()
   {
      final JButton component = new JButton();
      JComponent container = new JPanel();
      AnimationManager manager = new AnimationManager(container);

      new Expectations()
      {
         AnimationState animationState;
         ComponentState componentState;

         {
            animationState = new AnimationState(component, true);   // first addStart
            animationState.setStart(new ComponentState(component)); // second addStart
         }
      };

      manager.addStart(component);
      manager.addStart(component);
   }

   @Test
   public void addEndStateForComponent()
   {
      final JButton component = new JButton();
      JComponent container = new JPanel();
      AnimationManager manager = new AnimationManager(container);

      new Expectations()
      {
         AnimationState animationState;
         ComponentState componentState;

         {
            animationState = new AnimationState(component, false); // first addEnd
            animationState.setEnd(new ComponentState(component));  // second addEnd
         }
      };

      manager.addEnd(component);
      manager.addEnd(component);
   }

   @Test
   public void setupStart()
   {
      final JButton component = new JButton();
      JComponent container = new JPanel();
      container.add(component);
      component.setVisible(true);

      final AnimationManager manager = new AnimationManager(container);

      new Expectations(manager)
      {
         {
            manager.addStart(component);
         }
      };

      manager.setupStart();
   }

   @Test
   public void setupEndForComponentWithoutStartState()
   {
      final JButton component = new JButton();
      JComponent container = new JPanel();
      container.add(component);
      component.setVisible(true);

      final AnimationManager manager = new AnimationManager(container);

      new Expectations(manager)
      {
         @Mocked("(ComponentState, boolean)") final AnimationState animationState;
         @Mocked("(JComponent)") final ComponentState componentState;

         {
            componentState = new ComponentState(component);
            animationState = new AnimationState(componentState, false);
            endRecording();

            manager.setupEnd();

            Map<JComponent, AnimationState> compAnimStates = getField(manager, Map.class);
            assertSame(animationState.getComponent(), compAnimStates.get(component).getComponent());
         }
      };

      List<JComponent> changingComponents = getField(manager, List.class);
      assertTrue(changingComponents.contains(component));
   }

   @Test
   public void setupEndForComponentWithSameStartAndEndStates()
   {
      final JButton component = new JButton();
      JComponent container = new JPanel();
      container.add(component);
      component.setVisible(true);

      AnimationManager manager = new AnimationManager(container);
      final Map<JComponent, AnimationState> compAnimStates = getField(manager, Map.class);

      new Expectations()
      {
         @Mocked("(ComponentState, boolean)") AnimationState startState;

         {
            compAnimStates.put(component, startState);
            startState.setStart(new ComponentState(component));
         }
      };

      manager.setupEnd();

      assertTrue(compAnimStates.isEmpty());
   }

   @Test
   public void setupEndForComponentWithDifferentStartAndEndStates()
   {
      final JButton component = new JButton();
      final JComponent container = new JPanel();
      container.add(component);
      component.setVisible(true);

      new Expectations()
      {
         @Mocked("(ComponentState, boolean)") AnimationState animationState;

         {
            // Creates the start state for the component and registers it in the manager.
            AnimationManager manager = new AnimationManager(container);
            Map<JComponent, AnimationState> compAnimStates = getField(manager, Map.class);
            compAnimStates.put(component, animationState);
            animationState.setStart(new ComponentState(component));
            component.setLocation(100, 50);

            // Expectations:
            animationState.setEnd(new ComponentState(component));
            endRecording();

            manager.setupEnd();

            assertEquals(1, compAnimStates.size());

            List<JComponent> changingComponents = getField(manager, List.class);
            assertTrue(changingComponents.contains(component));
         }
      };
   }

   @Test
   public void paint(final Graphics g)
   {
      JComponent container = new JPanel();
      JButton component = new JButton();
      AnimationManager manager = new AnimationManager(container);
      manager.addStart(component);

      new Expectations()
      {
         AnimationState animationState;
         
         {
            g.drawImage(null, 0, 0, null);
            animationState.paint(g);
         }
      };

      manager.paint(g);
   }
}
