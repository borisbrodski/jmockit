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

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import static org.junit.Assert.*;

public final class EffectTest
{
   @Mocked JComponent component;
   @Mocked ComponentState state;

   final Effect effect = new Effect() {};

   @Test
   public void testSetComponentStates()
   {
      effect.setComponentStates(state, state);

      assertSame(state, effect.getStart());
      assertSame(state, effect.getEnd());
   }

   @Test
   public void testSetStartAndEndStates()
   {
      effect.setStart(state);
      effect.setEnd(state);
      
      assertSame(state, effect.getStart());
      assertSame(state, effect.getEnd());
   }

   @Test
   public void testSetBoundsFromIndividualComponents()
   {
      setUpEffect(true);

      effect.setBounds(1, 2, 3, 4);

      // The specified bounds should be used when the component is painted, so we call the paint
      // method and then verify that the component had its bounds correctly set.
      effect.paint(null);

      new Verifications()
      {
         {
            component.setBounds(new Rectangle(1, 2, 3, 4));
         }
      };
   }

   private void setUpEffect(boolean withStartState)
   {
      new NonStrictExpectations()
      {
         {
            state.getComponent(); returns(component);
         }
      };

      effect.setComponentStates(withStartState ? state : null, withStartState ? null : state);
   }

   @Test
   public void testSetBoundsFromRectangle()
   {
      setUpEffect(false);

      final Rectangle bounds = new Rectangle(1, 2, 3, 4);
      effect.setBounds(bounds);

      // The specified bounds should be used when the component is painted, so we call the paint
      // method and then verify that the component had its bounds correctly set.
      effect.paint(null);

      new Verifications()
      {
         {
            component.setBounds(bounds);
         }
      };
   }

   @Test
   public void testSetLocation(final Graphics2D g2D)
   {
      setUpEffect(true);

      final Point location = new Point(1, 2);
      effect.setLocation(location);

      // Prevents the Effect class from rendering to an internal image.
      effect.setRenderComponent(true);

      // The specified location should be used when the component is rendered, so we call the render
      // and then verify that the correct location was used.
      effect.render(g2D);

      new Verifications()
      {
         {
            g2D.translate(location.x, location.y);
         }
      };
   }

   @Test
   public void initEffectWithStartStateAndOutdatedComponentImage(final Image image)
   {
      setUpEffect(true);
      effect.setComponentImage(image);

      new NonStrictExpectations()
      {
         {
            image.getWidth(null); returns(100);
         }
      };

      effect.init(null, null);

      new Verifications()
      {
         {
            state.getX();
            state.getY();
            state.getWidth();
            state.getHeight();

            image.flush();
         }
      };
   }

   @Test
   public void cleanupDoesNothing(Animator animator)
   {
      // Empty strict expectations, so that any call to animator will fail the test.
      new Expectations() {};

      effect.cleanup(animator);
   }

   @Test
   public void initEffectWithEndStateAndUpToDateComponentImage(final Image image)
   {
      setUpEffect(false);
      effect.setComponentImage(image);

      effect.init(null, null);

      new Verifications()
      {
         {
            image.flush(); repeats(0);
         }
      };
   }

   @Test
   public void setupWithStartStateOnly(final Image componentImage)
   {
      effect.setComponentStates(state, null);

      new Expectations()
      {
         {
            state.getSnapshot(); returns(componentImage);
         }
      };

      assertSetupOfComponentImage(componentImage);
   }

   private void assertSetupOfComponentImage(Image expectedComponentImage)
   {
      // Exercise code under test:
      effect.setup(null);

      // Verify resulting state:
      assertSame(expectedComponentImage, effect.getComponentImage());
   }

   @Test
   public void setupWithEndStateOnly(final Image componentImage)
   {
      effect.setComponentStates(null, state);

      new Expectations()
      {
         {
            state.getSnapshot(); returns(componentImage);
         }
      };

      assertSetupOfComponentImage(componentImage);
   }

   @Test
   public void setupWithIdenticalStartAndEndStates(final Image componentImage)
   {
      effect.setComponentStates(state, state);

      new NonStrictExpectations()
      {
         {
            state.getSnapshot(); returns(componentImage);
         }
      };

      assertSetupOfComponentImage(componentImage);
   }

   @Test
   public void setupWithStartAndEndStatesOfDecreasingWidths(Image image, ComponentState endState)
   {
      assertSetupWithStartAndEndStatesOfDifferentSizes(image, endState, -10, 0);
   }

   private void assertSetupWithStartAndEndStatesOfDifferentSizes(
      final Image componentImage, final ComponentState endState, final int dx, final int dy)
   {
      final ComponentState startState = state;
      effect.setComponentStates(startState, endState);

      assertFalse(effect.getRenderComponent());

      new NonStrictExpectations()
      {
         {
            // Start state:
            onInstance(startState).getWidth(); returns(20);
            onInstance(startState).getHeight(); returns(20);

            // End state with different width or height:
            onInstance(endState).getWidth(); returns(20 + dx);
            onInstance(endState).getHeight(); returns(20 + dy);

            ComponentState stateToGetSnapshotImageFrom = dx < 0 || dy < 0 ? startState : endState;
            onInstance(stateToGetSnapshotImageFrom).getSnapshot(); returns(componentImage);
         }
      };

      assertSetupOfComponentImage(componentImage);
   }

   @Test
   public void setupWithStartAndEndStatesOfIncreasingWidths(Image image, ComponentState endState)
   {
      assertSetupWithStartAndEndStatesOfDifferentSizes(image, endState, 10, 0);
   }

   @Test
   public void setupWithStartAndEndStatesOfDecreasingHeights(Image image, ComponentState endState)
   {
      assertSetupWithStartAndEndStatesOfDifferentSizes(image, endState, 0, -10);
   }

   @Test
   public void setupWithStartAndEndStatesOfIncreasingHeights(Image image, ComponentState endState)
   {
      assertSetupWithStartAndEndStatesOfDifferentSizes(image, endState, 0, 10);
   }

   @Test
   public void paintEffectWithStartStateButNoComponentImage(final Graphics2D g2D)
   {
      setUpEffect(true);

      effect.paint(g2D);

      new VerificationsInOrder()
      {
         {
            component.setBounds((Rectangle) any);
            ComponentState.paintSingleBuffered(component, g2D);
         }
      };
   }

   @Test
   public void paintEffectWithStartStateAlreadySetup(final Image image, final Graphics2D g2D)
   {
      setUpEffect(true);
      effect.setComponentImage(image);
      effect.setWidth(20);
      effect.setHeight(10);

      effect.paint(g2D);

      new Verifications()
      {
         {
            g2D.drawImage(image, 0, 0, 20, 10, (ImageObserver) any);
         }
      };
   }
}
