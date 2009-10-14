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

import mockit.integration.junit4.*;
import mockit.*;
import org.junit.*;
import org.jdesktop.animation.timing.interpolation.*;
import static org.jdesktop.animation.timing.Animator.*;

@Capturing(baseType = TimingSource.class)
@SuppressWarnings({"ClassWithTooManyMethods"})
public final class AnimatorInitializationTest extends JMockitTest
{
   @Test
   public void testGetDuration()
   {
      assertEquals(500, new Animator(500).getDuration());
   }

   @Test
   public void testSetDuration()
   {
      Animator animator = new Animator(500);
      animator.setDuration(300);
      assertEquals(300, animator.getDuration());
   }

   @Test
   public void testGetStartDirection()
   {
      assertSame(Direction.FORWARD, new Animator(500).getStartDirection());
   }

   @Test
   public void testSetStartDirection()
   {
      Animator animator = new Animator(500);
      animator.setStartDirection(Direction.BACKWARD);
      assertSame(Direction.BACKWARD, animator.getStartDirection());
   }

   @Test
   public void testGetInterpolator()
   {
      assertSame(LinearInterpolator.getInstance(), new Animator(500).getInterpolator());
   }

   @Test
   public void testSetInterpolator()
   {
      Animator animator = new Animator(500);
      DiscreteInterpolator interpolator = DiscreteInterpolator.getInstance();

      animator.setInterpolator(interpolator);

      assertSame(interpolator, animator.getInterpolator());
   }

   @Test
   public void testGetAcceleration()
   {
      assertEquals(0, new Animator(500).getAcceleration(), 0);
   }

   @Test
   public void testSetAcceleration()
   {
      Animator animator = new Animator(500);
      animator.setAcceleration(0.2f);
      assertEquals(0.2f, animator.getAcceleration(), 0);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetInvalidAcceleration()
   {
      Animator animator = new Animator(500);
      animator.setAcceleration(1.2f);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetAccelerationIncompatibleWithDeceleration()
   {
      Animator animator = new Animator(500);
      animator.setDeceleration(0.6f);
      animator.setAcceleration(0.5f);
   }

   @Test
   public void testGetDeceleration()
   {
      assertEquals(0, new Animator(500).getDeceleration(), 0);
   }

   @Test
   public void testSetDeceleration()
   {
      Animator animator = new Animator(500);
      animator.setDeceleration(0.2f);
      assertEquals(0.2f, animator.getDeceleration(), 0);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetInvalidDeceleration()
   {
      Animator animator = new Animator(500);
      animator.setDeceleration(1.2f);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetDecelerationIncompatibleWithAcceleration()
   {
      Animator animator = new Animator(500);
      animator.setAcceleration(0.5f);
      animator.setDeceleration(0.6f);
   }

   @Test
   public void testFullConstructor()
   {
      Animator animator = new Animator(250, 3, RepeatBehavior.LOOP, null);

      assertEquals(250, animator.getDuration());
      assertEquals(3, animator.getRepeatCount(), 0);
      assertSame(RepeatBehavior.LOOP, animator.getRepeatBehavior());
   }

   @Test
   public void testFullConstructorWithDefaultRepeatBehavior()
   {
      Animator animator = new Animator(250, 3, null, null);

      assertSame(RepeatBehavior.REVERSE, animator.getRepeatBehavior());
   }

   @Test
   public void testSetRepeatCount()
   {
      Animator animator = new Animator(250);
      animator.setRepeatCount(5);
      assertEquals(5, animator.getRepeatCount(), 0);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetInvalidRepeatCount()
   {
      Animator animator = new Animator(250);
      animator.setRepeatCount(-5);
   }

   @Test
   public void testGetResolution()
   {
      assertEquals(20, new Animator(500).getResolution());
   }

   @Test
   public void testSetResolution()
   {
      Animator animator = new Animator(500);
      animator.setResolution(30);
      assertEquals(30, animator.getResolution());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetInvalidResolution()
   {
      Animator animator = new Animator(500);
      animator.setResolution(-10);
   }

   @Test
   public void testGetStartDelay()
   {
      assertEquals(0, new Animator(500).getStartDelay());
   }

   @Test
   public void testSetStartDelay()
   {
      Animator animator = new Animator(500);
      animator.setStartDelay(40);
      assertEquals(40, animator.getStartDelay());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetInvalidStartDelay()
   {
      Animator animator = new Animator(500);
      animator.setStartDelay(-4);
   }

   @Test
   public void testGetRepeatBehavior()
   {
      assertSame(RepeatBehavior.REVERSE, new Animator(500).getRepeatBehavior());
   }

   @Test
   public void testSetRepeatBehavior()
   {
      Animator animator = new Animator(500);
      animator.setRepeatBehavior(RepeatBehavior.LOOP);
      assertSame(RepeatBehavior.LOOP, animator.getRepeatBehavior());
   }

   @Test
   public void testSetDefaultRepeatBehavior()
   {
      Animator animator = new Animator(500);
      animator.setRepeatBehavior(null);
      assertSame(RepeatBehavior.REVERSE, animator.getRepeatBehavior());
   }

   @Test
   public void testGetEndBehavior()
   {
      assertSame(EndBehavior.HOLD, new Animator(500).getEndBehavior());
   }

   @Test
   public void testSetEndBehavior()
   {
      Animator animator = new Animator(500);
      animator.setEndBehavior(EndBehavior.RESET);
      assertSame(EndBehavior.RESET, animator.getEndBehavior());
   }

   @Test
   public void testGetStartFraction()
   {
      assertEquals(0, new Animator(500).getStartFraction(), 0);
   }

   @Test
   public void testSetStartFraction()
   {
      Animator animator = new Animator(500);
      animator.setStartFraction(0.1f);
      assertEquals(0.1f, animator.getStartFraction(), 0);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testSetNegativeStartFraction()
   {
      Animator animator = new Animator(500);
      animator.setStartFraction(-0.1f);
   }

   @Test
   public void testAddTarget()
   {
      final Animator animator = new Animator(500);

      // Verify the target indirectly:
      new Expectations()
      {
         TimingTarget target;

         {
            animator.addTarget(target);
            target.end();
         }
      };

      animator.stop();
   }

   @Test
   public void testRemoveTarget()
   {
      final Animator animator = new Animator(500);

      // Verify the target indirectly:
      new Expectations()
      {
         TimingTarget target;

         {
            animator.addTarget(target);
            animator.removeTarget(target);
            target.end(); repeats(0);
         }
      };

      animator.stop();
   }
}
