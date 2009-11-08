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
package org.jmock.samples.fruitPicker;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import static java.util.Arrays.*;
import java.util.*;

import mockit.integration.junit4.*;
import mockit.*;

@RunWith(JMockit.class)
public final class FruitPicker_JMockit_Test
{
   @Test
   public void pickFruits(final FruitTree mangoTree)
   {
      final Mango mango1 = new Mango();
      final Mango mango2 = new Mango();

      new Expectations()
      {
         {
            mangoTree.pickFruit((Collection<Fruit>) any);
            returns(new Delegate()
            {
               void pickFruit(Collection<Fruit> fruits)
               {
                  fruits.add(mango1);
                  fruits.add(mango2);
               }
            });
         }
      };

      Collection<Fruit> fruits = new FruitPicker().pickFruits(asList(mangoTree));

      assertEquals(asList(mango1, mango2), fruits);
   }
}
