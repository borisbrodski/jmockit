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

import org.junit.*;

import mockit.*;
import mockit.integration.junit4.*;

import static org.jdesktop.animation.timing.Animator.*;

public final class AnimatorLifecycleTest extends JMockitTest
{
   @Capturing
   private TimingSource timer;

   private Animator animator;

   @Before
   public void setUp()
   {
      TimingSource mockTimer = timer;
      animator = new Animator(500);
      assertNotSame(mockTimer, timer);
   }

   @Test
   public void testStart()
   {
      assertFalse(animator.isRunning());

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStartForwardAtIntermediateFraction()
   {
      animator.setStartFraction(0.2f);

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStartBackwardAtIntermediateFraction()
   {
      animator.setStartDirection(Direction.BACKWARD);
      animator.setStartFraction(0.8f);

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStop()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      assertTrue(animator.isRunning());
      animator.stop();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testCancel()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      animator.cancel();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testPause()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      animator.pause();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testResume()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
            timer.start();
         }
      };

      animator.start();
      animator.pause();
      assertFalse(animator.isRunning());
      animator.resume();
      assertTrue(animator.isRunning());
   }

   @Test(expected = IllegalStateException.class)
   public void testChangeConfigurationWhileRunning()
   {
      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();
      animator.setDuration(100);
   }
}
