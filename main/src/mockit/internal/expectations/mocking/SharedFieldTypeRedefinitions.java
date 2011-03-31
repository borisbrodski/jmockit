/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.util.*;
import java.util.Map.*;
import java.lang.reflect.*;

import mockit.internal.state.*;
import mockit.internal.util.*;

public final class SharedFieldTypeRedefinitions extends FieldTypeRedefinitions
{
   private TestedClassRedefinitions testedClassRedefinitions;
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
         testedClassRedefinitions = new TestedClassRedefinitions();
         testedClassRedefinitions.redefineTestedClasses(parentObject);

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
      TestRun.getExecutingTest().clearInjectableAndNonStrictMocks();

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
         catch (NoClassDefFoundError e) {
            Utilities.filterStackTrace(e);
            e.printStackTrace();
            throw e;
         }
         catch (ExceptionInInitializerError e) {
            Utilities.filterStackTrace(e);
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

   public TestedClassRedefinitions getTestedClassRedefinitions() { return testedClassRedefinitions; }
}
