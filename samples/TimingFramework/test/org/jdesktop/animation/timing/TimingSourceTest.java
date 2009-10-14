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

import mockit.*;
import mockit.integration.junit4.*;

import org.junit.*;

public final class TimingSourceTest extends JMockitTest
{
   @Mocked private TimingEventListener timingEventListener;
   private TimingSource source;

   @Before
   public void setUp()
   {
      source = new TestTimingSource();
   }

   private static final class TestTimingSource extends TimingSource
   {
      @Override
      public void start()
      {}

      @Override
      public void stop()
      {}

      @Override
      public void setResolution(int resolution)
      {}

      @Override
      public void setStartDelay(int delay)
      {}
   }

   @Test
   public void testAddEventListener()
   {
      new Expectations()
      {
         {
            timingEventListener.timingSourceEvent(source);
         }
      };

      source.addEventListener(timingEventListener);
      source.timingEvent();
   }

   @Test
   public void testRemoveEventListener()
   {
      source.addEventListener(timingEventListener);

      // Expects nothing.

      source.removeEventListener(timingEventListener);
      source.timingEvent();

      new FullVerifications() {};
   }
}
