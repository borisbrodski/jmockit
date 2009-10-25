/*
 * JMockit Samples
 * Copyright (c) 2009 Rogério Liesenfeld
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

import org.hamcrest.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.jdesktop.animation.timing.Animator.*;

import org.junit.*;

import mockit.*;
import mockit.integration.junit4.*;

@Capturing(baseType = TimingSource.class)
public final class AnimatorTimingSourceTest extends JMockitTest
{
   @Test
   public void setTimer(final TimingSource timingSource)
   {
      final Animator animator = new Animator(500);

      new Expectations()
      {
         {
            // Expectations for setTimer:
            timingSource.addEventListener(withInstanceOf(TimingEventListener.class));
            timingSource.setResolution(animator.getResolution());
            timingSource.setStartDelay(animator.getStartDelay());

            // Expectations for notification of events:
            timingSource.start();
            timingSource.stop();
         }
      };

      animator.setTimer(timingSource);
      animator.start();
      animator.stop();
   }

   @Test
   public void setTimerToCustomTimingSourceThenResetBackToOriginal(final TimingSource timingSource)
   {
      Animator animator = new Animator(50);

      new Expectations()
      {
         {
            timingSource.addEventListener(withInstanceOf(TimingEventListener.class));
            timingSource.setResolution(withAny(1));
            timingSource.setStartDelay(withAny(1));
            timingSource.removeEventListener(withInstanceOf(TimingEventListener.class));
            endRecording();
         }
      };

      animator.setTimer(timingSource);
      animator.setTimer(null);
   }

   private static final class EmptyTimingSource extends TimingSource
   {
      @Override
      public void start() {}

      @Override
      public void stop() {}

      @Override
      public void setResolution(int resolution) {}

      @Override
      public void setStartDelay(int delay) {}
   }

   @Test
   public void timingEventOnTimingSource()
   {
      Animator animator = new Animator(50);
      final TimingSource timingSource = new EmptyTimingSource();

      new Expectations()
      {
         @Mocked(capture = 1)
         private TimingEventListener timingEventTarget;

         {
            timingEventTarget.timingSourceEvent(timingSource);
         }
      };

      animator.setTimer(timingSource);
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForNonRunningAnimator()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations()
      {
         TimingTarget timingTarget;

         {
            // Passing mock object into code under test:
            animator.addTarget(timingTarget);

            // Expectations:
            timingTarget.timingEvent(0.0f);
            repeats(0); // Animator is not running, so no timing event is expected.
         }
      };

      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningAnimator()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations()
      {
         TimingTarget timingTarget;

         {
            // Passing mock object into code under test:
            animator.addTarget(timingTarget);

            // Expectations:
            timingTarget.begin();
            timingTarget.timingEvent(withEqual(0.0f, 0.04));
         }
      };

      animator.start();
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningAnimatorAtTimeToStop()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations()
      {
         @Mocked("nanoTime") final System system = null;
         @Mocked TimingTarget timingTarget;

         {
            animator.addTarget(timingTarget);

            // For the call to animator.start():
            System.nanoTime(); returns(0L);

            // For the call to timingSource.timingEvent():
            System.nanoTime(); returns(50L * 1000000);

            // Resulting expected interactions:
            timingTarget.begin();
            timingTarget.timingEvent(withEqual(1.0f, 0));
            timingTarget.end();
         }
      };

      animator.start();
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningRepeatingAnimator()
   {
      final Animator animator = new Animator(50, INFINITE, RepeatBehavior.LOOP, null);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations()
      {
         @Mocked("nanoTime") final System system = null;
         @Mocked TimingTarget timingTarget;

         {
            animator.addTarget(timingTarget);

            System.nanoTime(); returns(0L);
            System.nanoTime(); returns(60L * 1000000);

            timingTarget.begin();
            timingTarget.repeat();
            timingTarget.timingEvent(with(0.2f, (Matcher<Float>) greaterThan(0.0f)));
         }
      };

      animator.start();
      timingSource.timingEvent();
   }
}
