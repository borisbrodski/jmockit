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
