/*
 * JMockit Expectations & Verifications
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
package mockit.internal.expectations.mocking;

import java.util.*;

import mockit.internal.capturing.*;
import mockit.internal.state.*;

class TypeRedefinitions
{
   protected final Object parentObject;
   protected MockedType typeMetadata;
   protected int typesRedefined;
   protected final List<Class<?>> targetClasses;
   protected CaptureOfImplementations captureOfNewInstances;

   protected TypeRedefinitions(Object parentObject)
   {
      this.parentObject = parentObject;
      targetClasses = new ArrayList<Class<?>>(2);
   }

   public final int getTypesRedefined() { return typesRedefined; }
   public final List<Class<?>> getTargetClasses() { return targetClasses; }
   public CaptureOfImplementations getCaptureOfNewInstances() { return captureOfNewInstances; }

   protected final void registerMock(Object mock)
   {
      if (typeMetadata.injectable) {
         TestRun.getExecutingTest().addInjectableMock(mock);
      }

      if (typeMetadata.nonStrict) {
         TestRun.getExecutingTest().addNonStrictMock(mock);
      }
   }

   public final void cleanUp()
   {
      if (captureOfNewInstances != null) {
         captureOfNewInstances.cleanUp();
         captureOfNewInstances = null;
      }
   }
}
