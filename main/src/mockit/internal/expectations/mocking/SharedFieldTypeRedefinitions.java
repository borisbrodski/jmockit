/*
 * JMockit Expectations
 * Copyright (c) 2006-2011 Rogério Liesenfeld
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
      TestRun.enterNoMockingZone();

      try {
         Class<?> testClass = parentObject.getClass();
         targetClasses.clear();
         redefineFieldTypes(testClass, true);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   @Override
   protected void redefineTypeForMockField()
   {
      TypeRedefinition typeRedefinition = new TypeRedefinition(parentObject, typeMetadata);

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

      targetClasses.add(typeRedefinition.targetClass);
   }

   public void assignNewInstancesToMockFields(Object target)
   {
      TestRun.getExecutingTest().clearInjectableMocks();
      TestRun.getExecutingTest().clearNonStrictMocks();

      for (Entry<MockedType, InstanceFactory> metadataAndFactory : mockInstanceFactories.entrySet()) {
         typeMetadata = metadataAndFactory.getKey();
         InstanceFactory instanceFactory = metadataAndFactory.getValue();

         Object mock = assignNewInstanceToMockField(target, instanceFactory);
         registerMock(mock);
      }

      obtainAndRegisterInstancesOfFinalFields(target);
   }

   private void obtainAndRegisterInstancesOfFinalFields(Object target)
   {
      for (MockedType metadata : finalMockFields) {
         Object mock = Utilities.getFieldValue(metadata.field, target);
         typeMetadata = metadata;

         if (mock == null) {
            registerMockedClassIfNonStrict();
         }
         else {
            registerMock(mock);
         }
      }
   }

   private Object assignNewInstanceToMockField(Object target, InstanceFactory instanceFactory)
   {
      Field mockField = typeMetadata.field;
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

         if (typeMetadata.getMaxInstancesToCapture() > 0) {
            getCaptureOfNewInstances().resetCaptureCount(mockField);
         }
      }

      return mock;
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

   public void resetCapturingOfNewInstances()
   {
      if (captureOfNewInstances != null) {
         getCaptureOfNewInstances().reset();
      }
   }
}
