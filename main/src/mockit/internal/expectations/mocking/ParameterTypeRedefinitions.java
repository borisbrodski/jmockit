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

import java.lang.annotation.*;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class ParameterTypeRedefinitions extends TypeRedefinitions
{
   private final Type[] paramTypes;
   private final Annotation[][] paramAnnotations;
   private final Object[] paramValues;
   private final Class<?>[] testedClasses;
   private final MockedType[] mockParameters;
   private final List<Object> injectableMocks;
   private final List<Object> nonStrictMocks;

   public ParameterTypeRedefinitions(Object owner, Method testMethod)
   {
      super(owner);

      TestRun.enterNoMockingZone();

      try {
         paramTypes = testMethod.getGenericParameterTypes();
         paramAnnotations = testMethod.getParameterAnnotations();
         int n = paramTypes.length;
         paramValues = new Object[n];
         testedClasses = new Class<?>[n];
         mockParameters = new MockedType[n];
         injectableMocks = new ArrayList<Object>(n);
         nonStrictMocks = new ArrayList<Object>(n);

         boolean hasTestedClass = false;

         for (int i = 0; i < n; i++) {
            hasTestedClass |= discoverTestedOrMockedTypeAssociatedWithSpecialParameter(i);
         }

         if (hasTestedClass) {
            redefineAndInstantiateAllTestedClasses();
         }
         else {
            redefineAndInstantiateAllMockedTypes();
         }
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }

   private boolean discoverTestedOrMockedTypeAssociatedWithSpecialParameter(int paramIndex)
   {
      Type paramType = paramTypes[paramIndex];
      Annotation[] annotationsOnParameter = paramAnnotations[paramIndex];

      if (Utilities.getAnnotation(annotationsOnParameter, Tested.class) != null) {
         testedClasses[paramIndex] = (Class<?>) paramType;
         return true;
      }

      MockedType typeMetadata = new MockedType(paramIndex, paramType, annotationsOnParameter);

      if (typeMetadata.isMockParameter()) {
         mockParameters[paramIndex] = typeMetadata;
      }

      return false;
   }

   private void redefineAndInstantiateAllTestedClasses()
   {
      List<MockedType> mockedTypes = new ArrayList<MockedType>(mockParameters.length);

      for (MockedType mockParameter : mockParameters) {
         if (mockParameter != null) {
            mockedTypes.add(mockParameter);
         }
      }

      for (int i = 0; i < testedClasses.length; i++) {
         Class<?> testedClass = testedClasses[i];

         if (testedClass != null) {
            paramValues[i] = redefineAndInstantiateTestedClass(testedClass, mockedTypes);
         }
      }
   }

   private Object redefineAndInstantiateTestedClass(Class<?> testedClass, List<MockedType> mockedTypes)
   {
      Object tested = Utilities.newInstanceUsingDefaultConstructor(testedClass);

      ClassReader cr = ClassFile.createClassFileReader(testedClass.getName());
      TestedClassModifier modifier = new TestedClassModifier(cr, mockedTypes);
      cr.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      ClassDefinition classDefinition = new ClassDefinition(testedClass, modifiedClass);
      RedefinitionEngine.redefineClasses(classDefinition);

      return tested;
   }

   private void redefineAndInstantiateAllMockedTypes()
   {
      for (int i = 0; i < mockParameters.length; i++) {
         MockedType typeMetadata = mockParameters[i];

         if (typeMetadata != null) {
            paramValues[i] = redefineAndInstantiateMockedType(typeMetadata);
         }
      }
   }

   private Object redefineAndInstantiateMockedType(MockedType typeMetadata)
   {
      TypeRedefinition typeRedefinition = new TypeRedefinition(parentObject, typeMetadata);
      Object mock = typeRedefinition.redefineType();

      if (typeMetadata.injectable) {
         injectableMocks.add(mock);
      }

      if (typeMetadata.nonStrict) {
         nonStrictMocks.add(mock);
      }

      if (typeMetadata.getMaxInstancesToCapture() > 0) {
         registerCaptureOfNewInstances(typeMetadata);
      }

      targetClasses.add(typeRedefinition.targetClass);
      typesRedefined++;
      
      return mock;
   }

   private void registerCaptureOfNewInstances(MockedType typeMetadata)
   {
      CaptureOfNewInstancesForParameters capture = getCaptureOfNewInstances();

      if (capture == null) {
         capture = new CaptureOfNewInstancesForParameters();
         captureOfNewInstances = capture;
      }

      capture.registerCaptureOfNewInstances(typeMetadata);

      Class<?> paramClass = typeMetadata.getClassType();
      capture.makeSureAllSubtypesAreModified(paramClass, typeMetadata.capturing);
   }

   @Override
   public CaptureOfNewInstancesForParameters getCaptureOfNewInstances()
   {
      return (CaptureOfNewInstancesForParameters) captureOfNewInstances;
   }

   public Object[] getParameterValues() { return paramValues; }
   public List<Object> getInjectableMocks() { return injectableMocks; }
   public List<Object> getNonStrictMocks() { return nonStrictMocks; }
}
