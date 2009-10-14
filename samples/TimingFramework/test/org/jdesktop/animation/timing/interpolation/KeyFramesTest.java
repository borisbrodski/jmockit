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
package org.jdesktop.animation.timing.interpolation;

import static org.junit.Assert.*;
import org.junit.runner.*;
import org.junit.*;
import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public final class KeyFramesTest
{
   private KeyFrames<Integer> keyFrames;

   @Test
   public void testCreateKeyFramesFromGivenKeyValuesOnly()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2, 5);
      keyFrames = new KeyFrames<Integer>(keyValues);

      assertSame(keyValues, keyFrames.getKeyValues());

      KeyTimes keyTimes = keyFrames.getKeyTimes();
      assertEquals(3, keyTimes.getSize());
      assertEquals(0.0f, keyTimes.getTime(0), 0.0f);
      assertEquals(0.5f, keyTimes.getTime(1), 0.0f);
      assertEquals(1.0f, keyTimes.getTime(2), 0.0f);

      assertValues();
      assertIntervals();
   }

   private void assertValues()
   {
      assertValue(0, 0.0f);
      assertValue(1, 0.25f);
      assertValue(1, 0.499f);
      assertValue(2, 0.5f);
      assertValue(2, 0.625f);
      assertValue(2, 0.665f);
      assertValue(3, 0.67f);
      assertValue(3, 0.75f);
      assertValue(3, 0.833f);
      assertValue(4, 0.85f);
      assertValue(5, 1.0f);
   }

   private void assertValue(Object keyValue, float fraction)
   {
      Integer value = keyFrames.getValue(fraction);
      assertEquals(keyValue, value);
   }

   private void assertIntervals()
   {
      assertInterval(0, 0.1f);
      assertInterval(0, 0.2f);
      assertInterval(0, 0.25f);
      assertInterval(0, 0.50f);
      assertInterval(1, 0.51f);
      assertInterval(1, 0.625f);
      assertInterval(1, 0.76f);
      assertInterval(1, 0.9f);
      assertInterval(1, 1.0f);
   }

   private void assertInterval(int expectedInterval, float fraction)
   {
      assertEquals(expectedInterval, keyFrames.getInterval(fraction));
   }

   @Test
   public void testCreateKeyFramesWithGivenKeyValuesAndTimes()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2, 5);
      KeyTimes keyTimes = new KeyTimes(0.0f, 0.5f, 1.0f);
      keyFrames = new KeyFrames<Integer>(keyValues, keyTimes);

      assertSame(keyValues, keyFrames.getKeyValues());
      assertSame(keyTimes, keyFrames.getKeyTimes());
      assertValues();
      assertIntervals();
   }

   @Test(expected = IllegalArgumentException.class)
   public void testCreateKeyFramesFromKeyValuesAndTimesWithDifferentNumbersOfElements()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2, 5);
      KeyTimes keyTimes = new KeyTimes(0.0f, 1.0f);
      new KeyFrames<Integer>(keyValues, keyTimes);
   }

   @Test
   public void testCreateKeyFramesWithGivenKeyValuesAndInterpolators()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2);
      keyFrames = new KeyFrames<Integer>(keyValues, LinearInterpolator.getInstance());

      assertSame(keyValues, keyFrames.getKeyValues());
      assertEquals(2, keyFrames.getKeyTimes().getSize());
      assertValue(1, 0.5f);
      assertInterval(0, 0.4f);
      assertInterval(0, 1.0f);
   }

   @Test
   public void testCreateKeyFramesWithGivenKeyValuesAndSingleInterpolator()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 1, 2);
      keyFrames = new KeyFrames<Integer>(keyValues, LinearInterpolator.getInstance());

      assertSame(keyValues, keyFrames.getKeyValues());
      assertEquals(3, keyFrames.getKeyTimes().getSize());
      assertValue(1, 0.5f);
      assertInterval(0, 0.4f);
      assertInterval(1, 1.0f);
      assertInterval(2, 1.5f);
      assertInterval(2, 2.0f);
   }

   @Test
   public void testCreateKeyFramesWithGivenKeyValuesAndTimesAndInterpolators()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2);
      KeyTimes keyTimes = new KeyTimes(0.0f, 1.0f);
      keyFrames = new KeyFrames<Integer>(keyValues, keyTimes, LinearInterpolator.getInstance());

      assertSame(keyValues, keyFrames.getKeyValues());
      assertSame(keyTimes, keyFrames.getKeyTimes());
      assertValue(1, 0.5f);
      assertInterval(0, 0.4f);
      assertInterval(0, 1.0f);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testCreateKeyFramesWithInvalidNumberOfInterpolators()
   {
      KeyValues<Integer> keyValues = KeyValues.create(2);
      new KeyFrames<Integer>(
         keyValues, LinearInterpolator.getInstance(), DiscreteInterpolator.getInstance());
   }

   @Test
   public void testGetValueWithBuggyInterpolator()
   {
      KeyValues<Integer> keyValues = KeyValues.create(0, 2);

      keyFrames = new KeyFrames<Integer>(keyValues, new Interpolator()
      {
         public float interpolate(float fraction)
         {
            return fraction < 0.5f ? -1.0f : 2.0f;
         }
      });

      assertValue(0, 0.0f);
      assertValue(2, 1.0f);
   }
}
