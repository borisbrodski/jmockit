/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.annotation.*;
import java.lang.reflect.*;

import mockit.internal.state.*;

public final class ParameterTypeRedefinitions extends TypeRedefinitions
{
   private final Type[] paramTypes;
   private final Annotation[][] paramAnnotations;
   private final Object[] paramValues;
   private final MockedType[] mockParameters;

   public ParameterTypeRedefinitions(Object owner, Method testMethod)
   {
      super(owner);

      TestRun.enterNoMockingZone();

      try {
         paramTypes = testMethod.getGenericParameterTypes();
         paramAnnotations = testMethod.getParameterAnnotations();
         int n = paramTypes.length;
         paramValues = new Object[n];
         mockParameters = new MockedType[n];

         for (int i = 0; i < n; i++) {
            getMockedTypeFromMockParameterDeclaration(i);
         }

         redefineAndInstantiateMockedTypes();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private void getMockedTypeFromMockParameterDeclaration(int paramIndex)
   {
      Type paramType = paramTypes[paramIndex];
      Annotation[] annotationsOnParameter = paramAnnotations[paramIndex];

      typeMetadata = new MockedType(paramIndex, paramType, annotationsOnParameter);

      if (typeMetadata.isMockParameter()) {
         mockParameters[paramIndex] = typeMetadata;
      }
   }

   private void redefineAndInstantiateMockedTypes()
   {
      for (int i = 0; i < mockParameters.length; i++) {
         typeMetadata = mockParameters[i];

         if (typeMetadata != null) {
            paramValues[i] = redefineAndInstantiateMockedType();
         }
      }
   }

   private Object redefineAndInstantiateMockedType()
   {
      TypeRedefinition typeRedefinition = new TypeRedefinition(parentObject, typeMetadata);
      Object mock = typeRedefinition.redefineType();
      registerMock(mock);

      if (typeMetadata.getMaxInstancesToCapture() > 0) {
         registerCaptureOfNewInstances(mock);
      }

      targetClasses.add(typeRedefinition.targetClass);
      typesRedefined++;
      
      return mock;
   }

   private void registerCaptureOfNewInstances(Object originalInstance)
   {
      CaptureOfNewInstancesForParameters capture = getCaptureOfNewInstances();

      if (capture == null) {
         capture = new CaptureOfNewInstancesForParameters();
         captureOfNewInstances = capture;
      }

      capture.registerCaptureOfNewInstances(typeMetadata, originalInstance);
      capture.makeSureAllSubtypesAreModified(typeMetadata);
   }

   @Override
   public CaptureOfNewInstancesForParameters getCaptureOfNewInstances()
   {
      return (CaptureOfNewInstancesForParameters) captureOfNewInstances;
   }

   public Object[] getParameterValues() { return paramValues; }
}
