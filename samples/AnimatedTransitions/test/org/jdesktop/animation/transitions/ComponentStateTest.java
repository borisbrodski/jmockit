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

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class ComponentStateTest
{
   @Mocked("print") final JComponent component = new JButton();

   @Test
   public void testCreateComponentStateForComponentOfZeroSize()
   {
      ComponentState state = new ComponentState(component);

      assertComponentState(state);
      assertNull(state.getSnapshot());
   }

   private void assertComponentState(ComponentState state)
   {
      assertSame(component, state.getComponent());
      assertEquals(component.getX(), state.getX());
      assertEquals(component.getY(), state.getY());
      assertEquals(component.getWidth(), state.getWidth());
      assertEquals(component.getHeight(), state.getHeight());
   }

   @Test
   public void testCreateComponentStateForSizedComponent()
   {
      // Setup:
      component.setSize(80, 60);

      // Expectations:
      new Expectations()
      {
         {
            component.print(withInstanceOf(Graphics.class));
         }
      };

      // Execution of code under test:
      ComponentState state = new ComponentState(component);

      // Verifications:
      assertComponentState(state);
      Image snapshotImg = state.getSnapshot();
      assertNotNull(snapshotImg);
      assertEquals(component.getWidth(), snapshotImg.getWidth(null));
      assertEquals(component.getHeight(), snapshotImg.getHeight(null));
   }

   @Test
   public void testPaintHierarchySingleBuffered(
      @Mocked(methods = "()", inverse = true) final Graphics graphics)
   {
      JComponent container = new JPanel();
      container.setOpaque(true);
      container.setBounds(10, 5, 120, 90);

      component.setOpaque(false);
      component.setBounds(15, 12, 80, 60);
      container.add(component);

      new Expectations()
      {
         {
            graphics.setClip(0, 0, 80, 60);
            graphics.translate(-15, -12);
            component.print(graphics);
         }
      };

      ComponentState.paintHierarchySingleBuffered(component, graphics);
   }

   @Test
   public void testEquals()
   {
      ComponentState state1 = new ComponentState(component);
      ComponentState state2 = new ComponentState(component);

      assertFalse(state1.equals(null));
      assertEquals(state1, state1);
      assertEquals(state1, state2);
   }

   @Test
   public void testHashCode()
   {
      ComponentState state1 = new ComponentState(component);
      ComponentState state2 = new ComponentState(component);

      assertEquals(state1.hashCode(), state2.hashCode());
   }

   @Test
   public void testToString()
   {
      ComponentState state1 = new ComponentState(component);
      ComponentState state2 = new ComponentState(component);
      
      assertEquals(state1.toString(), state2.toString());
   }
}
