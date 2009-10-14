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
public final class FocusTriggerTest
{
   @Test
   public void testAddTrigger()
   {
      Component button = new JButton("Test");

      FocusTrigger trigger = FocusTrigger.addTrigger(button, null, FocusTriggerEvent.IN);

      FocusListener[] focusListeners = button.getFocusListeners();
      assertSame(trigger, focusListeners[focusListeners.length - 1]);
   }

   @Test
   public void testAddTriggerWithAutoReverse()
   {
      Component label = new JLabel();

      FocusTrigger trigger = FocusTrigger.addTrigger(label, null, FocusTriggerEvent.IN, true);

      assertSame(trigger, label.getFocusListeners()[0]);
   }

   @Test
   public void testFocusGained()
   {
      final FocusTrigger focusTrigger = new FocusTrigger(null, FocusTriggerEvent.IN);

      new Expectations(Trigger.class)
      {
         {
            focusTrigger.fire(FocusTriggerEvent.IN);
         }
      };

      focusTrigger.focusGained(null);
   }

   @Test
   public void testFocusLost()
   {
      final FocusTrigger focusTrigger = new FocusTrigger(null, FocusTriggerEvent.OUT);

      new Expectations(Trigger.class)
      {
         {
            focusTrigger.fire(FocusTriggerEvent.OUT);
         }
      };

      focusTrigger.focusLost(null);
   }
}
