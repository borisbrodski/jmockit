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
package org.jdesktop.animation.transitions.effects;

import mockit.*;
import mockit.integration.junit4.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import org.jdesktop.animation.transitions.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class FadeOutTest
{
   @Mocked private Animator animator;

   @Test
   public void testInit(ComponentState start, @Mocked("init") final Effect effect)
   {
      final FadeOut fadeOut = new FadeOut(start);

      new Expectations(PropertySetter.class)
      {
         {
            animator.addTarget(new PropertySetter(fadeOut, "opacity", 1.0f, 0.0f));
            effect.init(animator, null);
         }
      };

      fadeOut.init(animator, null);
   }

   @Test
   public void testCleanup()
   {
      new Expectations()
      {
         {
            animator.removeTarget(null);
         }
      };

      new FadeOut().cleanup(animator);
   }
}
