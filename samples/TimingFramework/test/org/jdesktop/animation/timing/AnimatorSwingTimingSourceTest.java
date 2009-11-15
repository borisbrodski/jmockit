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
package org.jdesktop.animation.timing;

import java.awt.event.*;
import javax.swing.*;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

@UsingMocksAndStubs(java.awt.Toolkit.class)
public final class AnimatorSwingTimingSourceTest
{
   private ActionListener timerTarget;

   @Test
   public void timingSourceEventOnSwingTimingSourceForRunningAnimator(
      final TimingTarget timingTarget)
   {
      Mockit.setUpMocks(new MockTimer());

      final Animator animator = new Animator(50);

      new Expectations()
      {
         @Mocked("nanoTime") final System system = null;

         {
            animator.addTarget(timingTarget);

            System.nanoTime(); returns(0L, 50L * 1000000);

            timingTarget.begin();
            timingTarget.timingEvent(1.0f);
            timingTarget.end();
         }
      };

      animator.start();
      timerTarget.actionPerformed(null);

      // Exercise other methods of the SwingTimingSource to fully cover the code, verifying through
      // MockTimer.
      animator.setResolution(10);
      animator.setStartDelay(0);
   }

   @MockClass(realClass = Timer.class)
   class MockTimer
   {
      @Mock(invocations = 1) // invocation from Animator(d)
      void $init(int delay, ActionListener actionListener)
      {
         assertEquals(20, delay); // 20 is the initial Animator resolution
         assertNotNull(actionListener);
         timerTarget = actionListener;
      }

      @Mock(invocations = 1)
      void start()
      {
      }

      @Mock(invocations = 1) // invocation from animator.setResolution
      void setDelay(int delay)
      {
         assertEquals(10, delay);
      }

      @Mock(invocations = 2) // one invocation from Animator(d), another from animator.setStartDelay
      void setInitialDelay(int initialDelay)
      {
         assertEquals(0, initialDelay);
      }
   }
}
