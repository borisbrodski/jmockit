/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   @Mocked ComponentState start;
   @Mocked ComponentState end;

   CompositeEffect composite;
   Effect effect1;
   Effect effect2;

   @Before
   public void setUp()
   {
      composite = new CompositeEffect();
      effect1 = new Unchanging();
      effect2 = new Unchanging();
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

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.setStart(start);
            effect2.setStart(start);
            onInstance(composite); effectSuper.setStart(start);
         }
      };

      composite.setStart(start);
   }

   @Test
   public void testSetEnd()
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.setEnd(end);
            effect2.setEnd(end);
            onInstance(composite); effectSuper.setEnd(end);
         }
      };

      composite.setEnd(end);
   }

   @Test
   public void testInit(final Animator animator)
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.init(animator, composite);
            effect2.init(animator, composite);
            effectSuper.init(animator, null);
         }
      };

      composite.init(animator, null);
   }

   @Test
   public void testCleanup(final Animator animator)
   {
      composite.addEffect(effect1);

      new Expectations(Effect.class)
      {
         {
            onInstance(effect1).cleanup(animator);
         }
      };

      composite.cleanup(animator);
   }

   @Test
   public void testSetup()
   {
      composite = new CompositeEffect(effect1);
      composite.addEffect(effect2);

      final Graphics2D g2D = null;

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            onInstance(effect1).setup(g2D);
            onInstance(effect2).setup(g2D);
            onInstance(composite); effectSuper.setup(g2D);
         }
      };

      composite.setup(g2D);
   }
}
