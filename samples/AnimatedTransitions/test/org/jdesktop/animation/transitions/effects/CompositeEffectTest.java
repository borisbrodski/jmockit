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

import java.awt.*;
import java.util.List;

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.transitions.*;
import static org.junit.Assert.*;

public final class CompositeEffectTest
{
   @Mocked private ComponentState start;
   @Mocked private ComponentState end;
   @Mocked({"init", "setup"}) private Effect effect1;
   @Mocked({"init", "setup"}) private Effect effect2;
   @Mocked({"init", "setup"}) private Effect effectSuperClass;

   private CompositeEffect composite;

   @Before
   public void setUp()
   {
      composite = new CompositeEffect();
   }

   @Test
   public void testAddEffect()
   {
      Move effect = new Move(start, end);
      effect.setRenderComponent(true);

      composite.addEffect(effect);

      List<Effect> effects = Deencapsulation.getField(composite, List.class);
      assertTrue(effects.contains(effect));
      assertTrue(composite.getRenderComponent());
      assertSame(start, composite.getStart());
      assertSame(end, composite.getEnd());
   }

   @Test
   public void testSetStart()
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations()
      {
         {
            effect1.setStart(start);
            effect2.setStart(start);
            effectSuperClass.setStart(start);
         }
      };

      composite.setStart(start);
   }

   @Test
   public void testSetEnd()
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations()
      {
         {
            effect1.setEnd(end);
            effect2.setEnd(end);
            effectSuperClass.setEnd(start);
         }
      };

      composite.setEnd(start);
   }

   @Test
   public void testInit(final Animator animator)
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations()
      {
         {
            onInstance(effect1).init(animator, composite);
            onInstance(effect2).init(animator, composite);
            effectSuperClass.init(animator, null);
         }
      };

      composite.init(animator, null);
   }

   @Test
   public void testCleanup(final Animator animator)
   {
      composite.addEffect(effect1);

      new Expectations()
      {
         {
            effect1.cleanup(animator);
         }
      };

      composite.cleanup(animator);
   }

   @Test
   public void testSetup()
   {
      CompositeEffect compositeEffect = new CompositeEffect(effect1);
      compositeEffect.addEffect(effect2);

      final Graphics2D g2D = null;

      new Expectations()
      {
         {
            effect1.setup(g2D);
            effect2.setup(g2D);
            effectSuperClass.setup(g2D);
         }
      };

      compositeEffect.setup(g2D);
   }
}
