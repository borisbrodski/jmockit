/*
 * JMockit Samples
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
import static org.junit.Assert.*;

public final class TimingTriggerTest
{
   @Test
   public void testAddTrigger(final Animator source, final Animator target)
   {
      final TimingTriggerEvent event = TimingTriggerEvent.START;

      new Expectations(TimingTrigger.class)
      {
         @Mocked(capture = 2)
         TimingTrigger timingTrigger;

         {
            source.addTarget(new TimingTrigger(target, event, false));
            endRecording();

            TimingTrigger triggerAdded = TimingTrigger.addTrigger(source, target, event);
            assertSame(timingTrigger, triggerAdded);
         }
      };
   }

   @Test
   public void testAddTriggerWithAutoReverse(final Animator source, final Animator target)
   {
      final TimingTriggerEvent event = TimingTriggerEvent.STOP;

      new Expectations(TimingTrigger.class)
      {
         @Mocked(capture = 2)
         TimingTrigger timingTrigger;

         {
            timingTrigger = new TimingTrigger(target, event, true);
            source.addTarget(timingTrigger);
            endRecording();

            TimingTrigger triggerAdded = TimingTrigger.addTrigger(source, target, event, true);
            assertSame(timingTrigger, triggerAdded);
         }
      };
   }

   @Test
   public void testBegin()
   {
      final TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.START);

      new Expectations(Trigger.class)
      {
         {
            timingTrigger.fire(TimingTriggerEvent.START);
         }
      };

      timingTrigger.begin();
   }

   @Test
   public void testEnd()
   {
      final TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.STOP);

      new Expectations(Trigger.class)
      {
         {
            timingTrigger.fire(TimingTriggerEvent.STOP);
         }
      };

      timingTrigger.end();
   }

   @Test
   public void testRepeat()
   {
      final TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.REPEAT);

      new Expectations(Trigger.class)
      {
         {
            timingTrigger.fire(TimingTriggerEvent.REPEAT);
         }
      };

      timingTrigger.repeat();
   }

   @Test
   public void testTimingEvent()
   {
      TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.STOP);

      new Expectations()
      {
         // Mocks the classes where methods could potentially be called by mistake;
         // if any call actually happens, the test will fail.
         @Mocked(methods = "timingEvent", inverse = true)
         final TimingTrigger unused = null;
      };

      timingTrigger.timingEvent(0.0f);
   }
}
