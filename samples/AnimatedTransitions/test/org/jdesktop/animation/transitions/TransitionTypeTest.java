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

import javax.swing.*;

import mockit.integration.junit4.*;
import org.jdesktop.animation.transitions.effects.*;
import org.junit.*;

public final class TransitionTypeTest extends JMockitTest
{
   private final JComponent component = new JComponent() {};
   private final Effect effect = new Unchanging();

   @Test
   public void testSetEffectAndGetEffect()
   {
      for (TransitionType transitionType : TransitionType.values()) {
         transitionType.setEffect(component, effect);
         assertSame(effect, transitionType.getEffect(component));
      }
   }

   @Test
   public void testSetEffectWithNull()
   {
      TransitionType transitionType = TransitionType.APPEARING;
      transitionType.setEffect(component, effect);

      transitionType.setEffect(component, null);

      assertNull(transitionType.getEffect(component));
   }

   @Test
   public void testRemoveEffect()
   {
      for (TransitionType transitionType : TransitionType.values()) {
         transitionType.setEffect(component, effect);
         transitionType.removeEffect(component);
         assertNull(transitionType.getEffect(component));
      }
   }

   @Test
   public void testClearEffects()
   {
      for (TransitionType transitionType : TransitionType.values()) {
         transitionType.setEffect(component, effect);
         transitionType.clearEffects();
         assertNull(transitionType.getEffect(component));
      }
   }

   @SuppressWarnings({"MethodWithMultipleLoops"})
   @Test
   public void testClearAllEffects()
   {
      for (TransitionType transitionType : TransitionType.values()) {
         transitionType.setEffect(component, effect);
      }

      TransitionType.clearAllEffects();

      for (TransitionType transitionType : TransitionType.values()) {
         assertNull(transitionType.getEffect(component));
      }
   }
}
