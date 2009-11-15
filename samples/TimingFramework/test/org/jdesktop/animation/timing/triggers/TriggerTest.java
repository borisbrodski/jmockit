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

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;

public final class TriggerTest
{
   @Mocked Animator animator;

   @Test
   public void testFireWithNonRunningAnimator()
   {
      Trigger trigger = new Trigger(animator);

      new Expectations()
      {
         {
            animator.isRunning(); returns(false);
            animator.start();
         }
      };

      trigger.fire();
   }

   @Test
   public void testFireWithRunningAnimator()
   {
      Trigger trigger = new Trigger(animator);

      new Expectations()
      {
         {
            animator.isRunning(); returns(true);
            animator.stop();
            animator.start();
         }
      };

      trigger.fire();
   }

   @Test
   public void testFireAfterDisarmed()
   {
      Trigger trigger = new Trigger(animator);
      trigger.disarm();

      // Expect nothing.
      new Expectations() {};

      trigger.fire();
      trigger.fire(null);
   }

   @Test
   public void testFireWithTriggerEventAndNonRunningAnimator()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event);

      new Expectations()
      {
         {
            animator.isRunning(); returns(false);
            animator.setStartDirection(Animator.Direction.FORWARD);

            // The original Timing Framework "fire(event)" method did an inappropriate call to
            // "fire()", which would then recheck disarming of the trigger and that the animator was
            // not running; writing this test revealed this issue, leading to better production
            // code.
            animator.start();
         }
      };

      trigger.fire(event);
   }

   @Test
   public void testFireWithTriggerEventAndRunningAnimatorNotOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, false);

      new Expectations()
      {
         {
            animator.isRunning(); returns(true);
            animator.stop();
            animator.setStartDirection(Animator.Direction.FORWARD);
            animator.start();
         }
      };

      trigger.fire(event);
   }

   @Test
   public void testFireWithTriggerEventAndNonRunningAnimatorOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, true);

      new Expectations()
      {
         {
            animator.isRunning(); returns(false);
            animator.setStartFraction(0.0f);
            animator.isRunning(); returns(false);
            animator.setStartDirection(Animator.Direction.FORWARD);
            animator.start();
         }
      };

      trigger.fire(event);
   }

   @Test
   public void testFireWithTriggerEventAndRunningAnimatorOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, true);

      new Expectations()
      {
         {
            animator.isRunning(); returns(true);

            animator.getTimingFraction();
            float timingFraction = 0.2f;
            returns(timingFraction);

            animator.stop();
            animator.setStartFraction(timingFraction);
            animator.isRunning(); returns(false);
            animator.setStartDirection(Animator.Direction.FORWARD);
            animator.start();
         }
      };

      trigger.fire(event);
   }

   @Test
   public void testFireWithOppositeTriggerEventAndNonRunningAnimatorNotOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, false);

      new Expectations() {};

      trigger.fire(event.getOppositeEvent());
   }

   @Test
   public void testFireWithOppositeTriggerEventAndNonRunningAnimatorOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, true);

      new Expectations()
      {
         {
            animator.isRunning(); returns(false);
            animator.setStartFraction(1.0f - animator.getStartFraction());
            animator.setStartDirection(Animator.Direction.BACKWARD);
            animator.start();
         }
      };

      trigger.fire(event.getOppositeEvent());
   }

   @Test
   public void testFireWithOppositeTriggerEventAndRunningAnimatorOnAutoReverse()
   {
      TriggerEvent event = FocusTriggerEvent.IN;
      Trigger trigger = new Trigger(animator, event, true);

      new Expectations()
      {
         {
            animator.isRunning(); returns(true);
            animator.getTimingFraction(); float timingFraction = 0.2f; returns(timingFraction);
            animator.stop();
            animator.setStartFraction(timingFraction);
            animator.setStartDirection(Animator.Direction.BACKWARD);
            animator.start();
         }
      };

      trigger.fire(event.getOppositeEvent());
   }
}
