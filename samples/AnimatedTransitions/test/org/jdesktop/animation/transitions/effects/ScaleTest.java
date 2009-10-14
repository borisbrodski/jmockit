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
public final class ScaleTest
{
   @Mocked private ComponentState start;
   @Mocked private ComponentState end;
   @Mocked private final PropertySetter<Integer> propertySetter = null;
   @Mocked private Animator animator;
   @Mocked("init") private Effect effect;

   @Test
   public void testInit()
   {
      final Scale scale = new Scale(start, end);

      new Expectations()
      {
         {
            animator.addTarget(
               new PropertySetter(scale, "width", start.getWidth(), end.getWidth()));
            animator.addTarget(
               new PropertySetter(scale, "height", start.getHeight(), end.getHeight()));
            effect.init(animator, null);
         }
      };

      scale.init(animator, null);
   }

   @Test
   public void testInitWithParentEffect()
   {
      final Unchanging parentEffect = new Unchanging();
      Scale scale = new Scale(start, end);

      new Expectations()
      {
         {
            animator.addTarget(
               new PropertySetter(parentEffect, "width", start.getWidth(), end.getWidth()));
            animator.addTarget(
               new PropertySetter(parentEffect, "height", start.getHeight(), end.getHeight()));
            effect.init(animator, null);
         }
      };

      scale.init(animator, parentEffect);
   }

   @Test
   public void testCleanup()
   {
      new Expectations()
      {
         {
            animator.removeTarget(null);
            animator.removeTarget(null);
         }
      };

      new Scale().cleanup(animator);
   }
}
