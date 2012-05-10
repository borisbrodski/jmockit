/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

import mockit.*;
import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.capturing.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockClassSetup
{
   private Class<?> realClass;
   private final Class<?> mockClass;
   private final Instantiation instantiation;
   private final MockingConfiguration mockingConfiguration;
   private final AnnotatedMockMethods mockMethods;
   private boolean forStartupMock;
   private Object mock;

   public MockClassSetup(Class<?> mockClass, MockClass metadata)
   {
      this.mockClass = mockClass;
      realClass = metadata.realClass();
      instantiation = metadata.instantiation();
      mockingConfiguration = createMockingConfiguration(metadata);

      mockMethods = new AnnotatedMockMethods(realClass);
      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);

      createMockInstanceAccordingToInstantiation();
   }

   private static MockingConfiguration createMockingConfiguration(MockClass metadata)
   {
      return createMockingConfiguration(metadata.stubs(), !metadata.inverse());
   }

   private static MockingConfiguration createMockingConfiguration(String[] filters, boolean notInverted)
   {
      return filters.length == 0 ? null : new MockingConfiguration(filters, notInverted);
   }

   private void createMockInstanceAccordingToInstantiation()
   {
      if (mock == null && instantiation == Instantiation.PerMockSetup) {
         mock = Utilities.newInstance(mockClass);
      }
   }

   public MockClassSetup(Class<?> realClass, Object mock, Class<?> mockClass)
   {
      this.realClass = realClass;
      this.mockClass = mockClass;
      mockMethods = new AnnotatedMockMethods(realClass);
      this.mock = mock;

      if (mockClass.isAnnotationPresent(MockClass.class)) {
         MockClass metadata = mockClass.getAnnotation(MockClass.class);
         instantiation = metadata.instantiation();
         createMockInstanceAccordingToInstantiation();
         mockingConfiguration = createMockingConfiguration(metadata);
      }
      else {
         instantiation = Instantiation.PerMockInvocation;
         mockingConfiguration = null;
      }

      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);
   }

   public MockClassSetup(Object mock, Class<?> mockClass)
   {
      this(getRealClass(mockClass), mock, mockClass);
   }

   private static Class<?> getRealClass(Class<?> specifiedMockClass)
   {
      MockClass mockClassAnnotation = specifiedMockClass.getAnnotation(MockClass.class);

      if (mockClassAnnotation == null) {
         throw new IllegalArgumentException("Missing @MockClass for " + specifiedMockClass);
      }

      return mockClassAnnotation.realClass();
   }

   public MockClassSetup(Class<?> mockClass)
   {
      this(getRealClass(mockClass), null, mockClass);
   }

   public Class<?> getRealClass() { return realClass; }
   public void setRealClass(Class<?> realClass) { this.realClass = realClass; }

   public void setUpStartupMock()
   {
      if (realClass != null) {
         forStartupMock = true;
         redefineMethods();
      }
   }

   public void redefineMethods()
   {
      Class<?> baseType = realClass;

      redefineMethodsInClassHierarchy();
      validateThatAllMockMethodsWereApplied();

      if (mockMethods.withMethodToSelectSubclasses) {
         new CaptureOfSubclasses().makeSureAllSubtypesAreModified(baseType);
      }
   }

   private void redefineMethodsInClassHierarchy()
   {
      while (realClass != null && (mockingConfiguration != null || mockMethods.hasUnusedMocks())) {
         byte[] modifiedClassFile = modifyRealClass();

         if (modifiedClassFile != null) {
            applyClassModifications(modifiedClassFile);
         }

         Class<?> superClass = realClass.getSuperclass();
         realClass = superClass == Object.class ? null : superClass;
      }
   }

   private byte[] modifyRealClass()
   {
      ClassReader rcReader = createClassReaderForRealClass();
      AnnotationsModifier modifier =
         new AnnotationsModifier(rcReader, realClass, mock, mockMethods, mockingConfiguration, forStartupMock);

      if (mock == null && instantiation == Instantiation.PerMockedInstance) {
         modifier.useOneMockInstancePerMockedInstance(mockClass);
      }

      rcReader.accept(modifier, 0);

      return modifier.wasModified() ? modifier.toByteArray() : null;
   }

   private ClassReader createClassReaderForRealClass()
   {
      if (realClass.isInterface() || realClass.isArray()) {
         throw new IllegalArgumentException("Not a modifiable class: " + realClass.getName());
      }

      return new ClassFile(realClass, true).getReader();
   }

   private void applyClassModifications(byte[] modifiedClassFile)
   {
      Startup.redefineMethods(realClass, modifiedClassFile);
      mockMethods.registerMockStates();

      MockFixture fixture = TestRun.mockFixture();

      if (forStartupMock) {
         fixture.addFixedClass(realClass.getName(), modifiedClassFile);
      }
      else {
         fixture.addRedefinedClass(mockMethods.getMockClassInternalName(), realClass, modifiedClassFile);
      }
   }

   private void validateThatAllMockMethodsWereApplied()
   {
      List<String> remainingMocks = mockMethods.getUnusedMockSignatures();

      if (!remainingMocks.isEmpty()) {
         String classDesc = mockMethods.getMockClassInternalName();
         String mockSignatures = new MethodFormatter(classDesc).friendlyMethodSignatures(remainingMocks);

         throw new IllegalArgumentException(
            "Matching real methods not found for the following mocks:\n" + mockSignatures);
      }
   }

   final class CaptureOfSubclasses extends CaptureOfImplementations
   {
      @Override
      protected ClassSelector createClassSelector()
      {
         return new ClassSelector()
         {
            public boolean shouldCapture(String className)
            {
               return true;
            }
         };
      }

      @Override
      protected ClassVisitor createModifier(ClassLoader cl, ClassReader cr, String capturedTypeDesc)
      {
         return new AnnotationsModifier(cl, cr, mock, mockMethods, mockingConfiguration, forStartupMock);
      }
   }
}
