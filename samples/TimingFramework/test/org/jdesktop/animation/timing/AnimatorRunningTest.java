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

import mockit.*;
import static mockit.Mockit.*;
import static org.jdesktop.animation.timing.Animator.*;
import org.junit.*;
import static org.junit.Assert.*;

@UsingMocksAndStubs(java.awt.Toolkit.class)
@Capturing(baseType = TimingSource.class)
public final class AnimatorRunningTest
{
   @MockClass(realClass = System.class)
   class MockSystem
   {
      @Mock
      public long nanoTime() { return currentTimeInMillis * 1000000L; }
   }

   @Before
   public void setUpTest() { setUpMocks(new MockSystem()); }

   private long currentTimeInMillis;
   private final Animator animator = new Animator(500);

   @Test
   public void testGetTotalElapsedTimeFromGivenTime()
   {
      // Just to check the initial state:
      assertEquals(0, animator.getTotalElapsedTime(0));

      animator.start();

      long elapsedTime = animator.getTotalElapsedTime(10);
      assertEquals(10, elapsedTime);
   }

   @Test
   public void testGetTotalElapsedTimeFromCurrentTime()
   {
      assertEquals(0, animator.getTotalElapsedTime(0));

      animator.start();
      currentTimeInMillis = 10;

      long elapsedTime = animator.getTotalElapsedTime();
      assertEquals(10, elapsedTime);
   }

   @Test
   public void testGetCycleElapsedTimeFromGivenTime()
   {
      assertEquals(0, animator.getCycleElapsedTime(0));

      animator.start();

      long elapsedTime = animator.getCycleElapsedTime(10);
      assertEquals(10, elapsedTime);
   }

   @Test
   public void testGetCycleElapsedTimeFromCurrentTime()
   {
      assertEquals(0, animator.getCycleElapsedTime(0));

      animator.start();
      currentTimeInMillis = 18;

      long elapsedTime = animator.getCycleElapsedTime();
      assertEquals(18, elapsedTime);
   }

   @Test
   public void getTimingFractionAtStartWithInfiniteDurationForward()
   {
      animator.setDuration(INFINITE);
      animator.start();
      assertEquals(0, animator.getTimingFraction(), 0);
   }

   @Test
   public void getTimingFractionAtStartWithFiniteDurationForward()
   {
      animator.start();
      assertEquals(0, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtStartWithFiniteDurationBackward()
   {
      animator.setStartDirection(Direction.BACKWARD);
      animator.setStartFraction(1.0f);
      animator.start();
      assertEquals(1, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtEndWithFiniteDurationForward()
   {
      animator.setDuration(5);
      animator.start();
      currentTimeInMillis = 5;
      assertEquals(1, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtStartWithAccelerationAndFiniteDurationForward()
   {
      animator.setAcceleration(0.1f);
      animator.start();
      assertEquals(0, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtStartWithDecelerationAndFiniteDurationForward()
   {
      animator.setDeceleration(0.2f);
      animator.start();
      assertEquals(0, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtEndWithDecelerationAndFiniteDurationForward()
   {
      animator.setDuration(5);
      animator.setDeceleration(0.2f);
      animator.start();
      currentTimeInMillis = 5;
      assertEquals(1, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAfterFirstCycleWithFiniteDurationForward()
   {
      animator.setDuration(20);
      animator.setRepeatCount(2);
      animator.start();
      currentTimeInMillis = 30;
      assertEquals(0.5, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAfterFirstCycleWithFiniteDurationBackward()
   {
      animator.setDuration(20);
      animator.setRepeatCount(2);
      animator.setStartDirection(Direction.BACKWARD);
      animator.setStartFraction(1.0f);
      animator.start();
      currentTimeInMillis = 30;
      assertEquals(0.5, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtEndWithFiniteDurationForwardAndResetting()
   {
      animator.setDuration(20);
      animator.setEndBehavior(EndBehavior.RESET);
      animator.start();
      currentTimeInMillis = 21;
      assertEquals(0, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtEndWithFiniteDurationForwardAndFractionalRepeatCount()
   {
      animator.setDuration(20);
      animator.setRepeatCount(1.5);
      animator.start();
      currentTimeInMillis = 31;
      assertEquals(1, animator.getTimingFraction(), 0.01);
   }

   @Test
   public void getTimingFractionAtEndWithFiniteDurationBackward()
   {
      animator.setDuration(20);
      animator.setStartDirection(Direction.BACKWARD);
      animator.setStartFraction(1.0f);
      animator.start();
      currentTimeInMillis = 21;
      assertEquals(0, animator.getTimingFraction(), 0.01);
   }

   // This test is for a private method in the Animator class. Usually, such methods should be
   // covered through other tests targeted at the public methods which use the private one. In
   // this case, however, there was no obvious way to fully exercise the private method except by
   // calling it directly.
   @Test
   public void testClampedBetweenZeroAndOne()
   {
      new Expectations()
      {
         {
            Float minFraction = invoke(animator, "clampedBetweenZeroAndOne", -0.02f);
            assertEquals(0.0, minFraction, 0.0);

            Float maxFraction = invoke(animator, "clampedBetweenZeroAndOne", 1.02f);
            assertEquals(1.0, maxFraction, 0.0);
         }
      };
   }
}
