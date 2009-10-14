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
package org.jmock.samples.parentChild;

import org.junit.*;
import org.junit.runner.*;
import mockit.integration.junit4.*;
import mockit.*;

/**
 * Notice how much simpler the equivalent test is with JMockit.
 */
@RunWith(JMockit.class)
public final class Child_JMockit_Test
{
   @Mocked Parent parent;
   Child child;

   @Before
   public void createChildOfParent()
   {
      // Expectations can be recorded here, in expectation blocks.
      // If they aren't, all mock invocations during setup will be allowed.

      // Creating the child adds it to the parent.
      child = new Child(parent);
   }

   @Test
   public void removesItselfFromOldParentWhenAssignedNewParent(final Parent newParent)
   {
      new Expectations()
      {{
         parent.removeChild(child);
         newParent.addChild(child);
      }};

      child.reparent(newParent);
   }
}
