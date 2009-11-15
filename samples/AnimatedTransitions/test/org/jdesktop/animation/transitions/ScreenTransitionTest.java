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

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;

public final class ScreenTransitionTest
{
   @Mocked(methods = {"setAnimator", "start"}, inverse = true) private ScreenTransition transition;
   @Mocked private Animator animator;

   @Test
   public void setAnimator()
   {
      new Expectations()
      {
         {
            animator.isRunning(); returns(false);

            // target will be null because the constructor was mocked to do nothing:
            animator.addTarget(null);
         }
      };

      transition.setAnimator(animator);
   }

   @Test(expected = IllegalArgumentException.class)
   public void setAnimatorToNull()
   {
      transition.setAnimator(null);
   }

   @Test(expected = IllegalStateException.class)
   public void setAnimatorAlreadyRunning()
   {
      new Expectations()
      {
         {
            animator.isRunning(); returns(true);
         }
      };

      transition.setAnimator(animator);
   }

   @Test
   public void startWithNonRunningAnimator()
   {
      new Expectations()
      {
         {
            setField(transition, animator);

            animator.isRunning(); returns(false);
            animator.start();
         }
      };

      transition.start();
   }

   @Test
   public void startWithRunningAnimator()
   {
      new Expectations()
      {
         {
            setField(transition, animator);

            animator.isRunning(); returns(true);
            animator.stop();
            animator.start();
         }
      };

      transition.start();
   }
}
