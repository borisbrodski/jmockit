/*
 * JMockit Expectations
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
package mockit.internal.expectations.mocking;

import java.lang.annotation.*;
import java.lang.reflect.*;

import mockit.internal.filtering.*;
import mockit.internal.state.*;

public final class ParameterTypeRedefinitions extends TypeRedefinitions
{
   private final Type[] paramTypes;
   private final Annotation[][] paramAnnotations;
   private final Object[] paramValues;

   public ParameterTypeRedefinitions(Object objectWithInitializerMethods, Method testMethod)
   {
      super(objectWithInitializerMethods);
      paramTypes = testMethod.getGenericParameterTypes();
      paramAnnotations = testMethod.getParameterAnnotations();
      paramValues = new Object[paramTypes.length];
   }

   public Object[] redefineParameterTypes()
   {
      for (int i = 0; i < paramTypes.length; i++) {
         redefineTypeForMockParameter(i);
      }

      return paramValues;
   }

   private void redefineTypeForMockParameter(int paramIndex)
   {
      Type paramType = paramTypes[paramIndex];
      MockedType typeMetadata = new MockedType(paramIndex, paramType, paramAnnotations[paramIndex]);

      if (!typeMetadata.isMockField()) {
         return;
      }

      TypeRedefinition redefinition = new TypeRedefinition(parentObject, typeMetadata);
      Object mock = redefinition.redefineType();
      paramValues[paramIndex] = mock;

      if (typeMetadata.nonStrict) {
         TestRun.getExecutingTest().addNonStrictMock(mock);
      }

      if (typeMetadata.getMaxInstancesToCapture() > 0) {
         registerCaptureOfNewInstances(typeMetadata, redefinition.mockingCfg);
      }
   }

   private void registerCaptureOfNewInstances(
      MockedType typeMetadata, MockingConfiguration mockingCfg)
   {
      if (captureOfNewInstances == null) {
         captureOfNewInstances = new CaptureOfNewInstancesForParameters();
         TestRun.getExecutingTest().setCaptureOfNewInstances(captureOfNewInstances);
      }

      ((CaptureOfNewInstancesForParameters) captureOfNewInstances).setMockingConfiguration(
         mockingCfg);

      Class<?> paramClass = typeMetadata.getClassType();
      captureOfNewInstances.makeSureAllSubtypesAreModified(paramClass, typeMetadata.capturing);
   }
}
