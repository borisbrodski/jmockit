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
package org.jdesktop.animation.timing.triggers;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import mockit.*;
import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class MouseTriggerTest
{
   @Test
   public void testAddTrigger()
   {
      Component button = new JButton("Test");

      MouseTrigger trigger = MouseTrigger.addTrigger(button, null, MouseTriggerEvent.ENTER);

      MouseListener[] mouseListeners = button.getMouseListeners();
      assertSame(trigger, mouseListeners[mouseListeners.length - 1]);
   }

   @Test
   public void testAddTriggerWithAutoReverse()
   {
      Component button = new JButton("Test");

      MouseTrigger trigger = MouseTrigger.addTrigger(button, null, MouseTriggerEvent.ENTER, true);

      MouseListener[] mouseListeners = button.getMouseListeners();
      assertSame(trigger, mouseListeners[mouseListeners.length - 1]);
   }

   @Test
   public void testMouseEntered()
   {
      MouseListener trigger = createMouseTriggerWithExpectations(MouseTriggerEvent.ENTER);
      trigger.mouseEntered(null);
   }

   private MouseListener createMouseTriggerWithExpectations(final MouseTriggerEvent event)
   {
      final MouseTrigger mouseTrigger = new MouseTrigger(null, event);

      new Expectations()
      {
         final Trigger trigger = null;

         {
            mouseTrigger.fire(event);
         }
      };

      return mouseTrigger;
   }

   @Test
   public void testMouseExited()
   {
      MouseListener trigger = createMouseTriggerWithExpectations(MouseTriggerEvent.EXIT);
      trigger.mouseExited(null);
   }

   @Test
   public void testMousePressed()
   {
      MouseListener trigger = createMouseTriggerWithExpectations(MouseTriggerEvent.PRESS);
      trigger.mousePressed(null);
   }

   @Test
   public void testMouseReleased()
   {
      MouseListener trigger = createMouseTriggerWithExpectations(MouseTriggerEvent.RELEASE);
      trigger.mouseReleased(null);
   }

   @Test
   public void testMouseClicked()
   {
      MouseListener trigger = createMouseTriggerWithExpectations(MouseTriggerEvent.CLICK);
      trigger.mouseClicked(null);
   }
}
