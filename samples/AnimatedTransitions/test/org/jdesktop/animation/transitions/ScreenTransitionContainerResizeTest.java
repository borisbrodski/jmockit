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
package org.jdesktop.animation.transitions;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static mockit.Deencapsulation.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import static org.junit.Assert.*;

@UsingMocksAndStubs({Animator.class, PropertySetter.class})
public final class ScreenTransitionContainerResizeTest
{
   @Mocked("createImage(int, int)") final JComponent container = new JPanel();
   @NonStrict BufferedImage transitionImage;
   @NonStrict AnimationManager manager;

   final Dimension newSize = new Dimension(100, 80);
   ScreenTransition transition;

   @Test
   public void resizeTransitionContainerOnce()
   {
      new CreationOfTransitionImage();

      ComponentListener containerSizeListener = createTransition();

      // Exercise the code under test:
      assertResizingOfContainer(containerSizeListener);
   }

   final class CreationOfTransitionImage extends Expectations
   {
      {
         container.createImage(newSize.width, newSize.height); result = transitionImage;
         manager.recreateImage();
      }
   }

   private ComponentListener createTransition()
   {
      // Creates ScreenTransition, which adds itself as listener to the container:
      transition = new ScreenTransition(container, null, 100);

      // Get the internal listener:
      ComponentListener containerSizeListener = container.getListeners(ComponentListener.class)[0];

      // Remove the listener from the container, so it won't get called on "setSize":
      container.removeComponentListener(containerSizeListener);
      
      return containerSizeListener;
   }

   private void assertResizingOfContainer(ComponentListener containerSizeListener)
   {
      container.setSize(newSize);
      containerSizeListener.componentResized(null);
      assertSame(transitionImage, transition.getTransitionImage());
   }

   @Test
   public void changeWidthOfTransitionContainerAlreadyWithTransitionImage()
   {
      ComponentListener containerSizeListener = createTransition();
      setField(transition, transitionImage);

      new CreationOfTransitionImage();

      assertResizingOfContainer(containerSizeListener);
   }

   @Test
   public void changeHeightOfTransitionContainerAlreadyWithTransitionImage()
   {
      ComponentListener containerSizeListener = createTransition();
      setField(transition, transitionImage);

      new Expectations() {{ transitionImage.getWidth(); result = newSize.width; }};
      new CreationOfTransitionImage();

      assertResizingOfContainer(containerSizeListener);
   }
}
