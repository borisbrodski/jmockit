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
package org.jdesktop.animation.transitions;

import java.awt.event.*;
import javax.swing.*;

import mockit.*;
import mockit.integration.junit4.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
@UsingMocksAndStubs(PropertySetter.class)
public final class ScreenTransitionInitializationTest
{
   @NonStrict private JComponent container;
   @Mocked private TransitionTarget target;
   @Mocked private Animator animator;
   @Mocked private final AnimationManager manager = null;
   @Mocked private AnimationLayer layer;

   @Test
   public void createScreenTransitionWithNonRunningAnimator()
   {
      new TransitionInitialization();
      new SettingOfAnimatorField();

      ScreenTransition st = new ScreenTransition(container, target, animator);

      assertSame(animator, st.getAnimator());
   }

   final class TransitionInitialization extends Expectations
   {
      {
         new AnimationManager(container);
         new AnimationLayer(withInstanceOf(ScreenTransition.class));
         layer.setVisible(false);
         onInstance(container).addComponentListener(withInstanceOf(ComponentListener.class));
         onInstance(container).getWidth(); returns(0);
         onInstance(container).getHeight(); returns(0);
      }
   }

   final class SettingOfAnimatorField extends Expectations
   {
      {
         onInstance(animator).isRunning(); returns(false);
         onInstance(animator).addTarget(withInstanceOf(TimingTargetAdapter.class));
      }
   }

   @Test
   public void createScreenTransitionForGivenDuration()
   {
      new TransitionInitialization();

      // Create the animator to be used:
      final int duration = 100;

      new Expectations()
      {
         {
            animator = new Animator(withEqual(duration), withInstanceOf(TimingTargetAdapter.class));
         }
      };

      new SettingOfAnimatorField();

      ScreenTransition st = new ScreenTransition(container, target, duration);
      
      assertNotNull(st.getAnimator());
   }
}
