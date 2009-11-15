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

import javax.swing.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class ActionTriggerTest
{
   @Test
   public void testAddTrigger()
   {
      AbstractButton button = new JButton("Test");

      ActionTrigger trigger = ActionTrigger.addTrigger(button, null);

      assertSame(trigger, button.getActionListeners()[0]);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAddTriggerFailsOnObjectWithoutAddActionListenerMethod()
   {
      ActionTrigger.addTrigger(new Object(), null);
   }

   @Test
   public void testActionPerformed()
   {
      ActionTrigger actionTrigger = new ActionTrigger(null);

      new Expectations()
      {
         Trigger trigger;

         {
            trigger.fire();
         }
      };

      actionTrigger.actionPerformed(null);
   }
}
