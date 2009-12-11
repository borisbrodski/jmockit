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

import java.util.*;
import java.util.Map.*;
import java.lang.reflect.*;

import mockit.internal.state.*;
import mockit.internal.util.*;
import mockit.*;

public final class SharedFieldTypeRedefinitions extends FieldTypeRedefinitions
{
   private final Map<MockedType, InstanceFactory> mockInstanceFactories;
   private final List<MockedType> finalMockFields;

   public SharedFieldTypeRedefinitions(Object objectWithMockFields)
   {
      super(objectWithMockFields);
      mockInstanceFactories = new HashMap<MockedType, InstanceFactory>();
      finalMockFields = new ArrayList<MockedType>();
   }

   public void redefineTypesForTestClass()
   {
      Class<?> testClass = parentObject.getClass();
      redefineFieldTypes(testClass, !Expectations.class.isAssignableFrom(testClass));
   }

   @Override
   protected TypeRedefinition redefineTypeForMockField()
   {
      TypeRedefinition typeRedefinition = new SharedTypeRedefinition(parentObject, typeMetadata);

      if (finalField) {
         typeRedefinition.redefineTypeForFinalField();
         finalMockFields.add(typeMetadata);
      }
      else {
         typeRedefinition.redefineType();
         InstanceFactory factory = typeRedefinition.instanceFactory;

         if (factory != null) {
            mockInstanceFactories.put(typeMetadata, factory);
         }
         else {
            finalMockFields.add(typeMetadata);
         }
      }

      return typeRedefinition;
   }

   public void assignNewInstancesToMockFields(Object target)
   {
      TestRun.getExecutingTest().clearNonStrictMocks();

      for (
         Entry<MockedType, InstanceFactory> metadataAndFactory : mockInstanceFactories.entrySet()
      ) {
         MockedType metadata = metadataAndFactory.getKey();
         InstanceFactory instanceFactory = metadataAndFactory.getValue();

         assignNewInstanceToMockField(target, metadata, instanceFactory);
      }

      for (MockedType metadata : finalMockFields) {
         if (metadata.nonStrict) {
            TestRun.getExecutingTest().addNonStrictMock(metadata.getClassType());
         }
      }
   }

   private void assignNewInstanceToMockField(
      Object target, MockedType metadata, InstanceFactory instanceFactory)
   {
      Field mockField = metadata.field;
      Object mock = Utilities.getFieldValue(mockField, target);

      if (mock == null) {
         try {
            mock = instanceFactory.create();
         }
         catch (ExceptionInInitializerError e) {
            Utilities.filterStackTrace(e);
            Utilities.filterStackTrace(e.getCause());
            e.printStackTrace();
            throw e;
         }

         Utilities.setFieldValue(mockField, target, mock);

         if (metadata.getMaxInstancesToCapture() > 0) {
            getCaptureOfNewInstances().resetCaptureCount(mockField);
         }
      }

      if (metadata.nonStrict) {
         TestRun.getExecutingTest().addNonStrictMock(mock);
      }
   }

   @Override
   public boolean captureNewInstanceForApplicableMockField(Object mock)
   {
      if (captureOfNewInstances == null) {
         return false;
      }

      Object fieldOwner = TestRun.getCurrentTestInstance();

      return getCaptureOfNewInstances().captureNewInstanceForApplicableMockField(fieldOwner, mock);
   }
}
