/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.*;
import java.lang.instrument.*;
import java.util.*;
import java.util.Map.*;

import mockit.*;
import mockit.external.asm4.*;
import mockit.internal.annotations.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class RedefinitionEngine
{
   private Class<?> realClass;
   private final Class<?> mockClass;
   private final Instantiation instantiation;
   private final MockingConfiguration mockingConfiguration;
   private final AnnotatedMockMethods mockMethods;
   private Object mock;

   public RedefinitionEngine()
   {
      mockClass = null;
      instantiation = Instantiation.PerMockInvocation;
      mockingConfiguration = null;
      mockMethods = null;
   }

   public RedefinitionEngine(Class<?> realOrMockClass)
   {
      MockClass metadata = realOrMockClass.getAnnotation(MockClass.class);

      if (metadata == null) {
         realClass = realOrMockClass;
         mockClass = null;
         instantiation = Instantiation.PerMockInvocation;
         mockingConfiguration = null;
         mockMethods = null;
      }
      else {
         realClass = metadata.realClass();
         mockClass = realOrMockClass;
         instantiation = metadata.instantiation();
         mockingConfiguration = createMockingConfiguration(metadata);

         mockMethods = new AnnotatedMockMethods(realClass);
         new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);

         createMockInstanceAccordingToInstantiation();
      }
   }

   private MockingConfiguration createMockingConfiguration(MockClass metadata)
   {
      return createMockingConfiguration(metadata.stubs(), !metadata.inverse());
   }

   private MockingConfiguration createMockingConfiguration(String[] filters, boolean notInverted)
   {
      return filters.length == 0 ? null : new MockingConfiguration(filters, notInverted);
   }

   private void createMockInstanceAccordingToInstantiation()
   {
      if (mock == null && instantiation == Instantiation.PerMockSetup) {
         mock = Utilities.newInstance(mockClass);
      }
   }

   public RedefinitionEngine(Class<?> realClass, boolean filtersNotInverted, String... stubbingFilters)
   {
      this.realClass = realClass;
      mockClass = null;
      instantiation = Instantiation.PerMockInvocation;
      mockMethods = null;
      mockingConfiguration = createMockingConfiguration(stubbingFilters, filtersNotInverted);
   }

   public RedefinitionEngine(Class<?> realClass, Object mock, Class<?> mockClass)
   {
      this.realClass = realClass;
      this.mockClass = mockClass;
      mockMethods = new AnnotatedMockMethods(realClass);
      this.mock = mock;

      if (mockClass == null || !mockClass.isAnnotationPresent(MockClass.class)) {
         instantiation = Instantiation.PerMockInvocation;
         mockingConfiguration = null;
      }
      else {
         MockClass metadata = mockClass.getAnnotation(MockClass.class);
         instantiation = metadata.instantiation();
         createMockInstanceAccordingToInstantiation();
         mockingConfiguration = createMockingConfiguration(metadata);
      }

      new AnnotatedMockMethodCollector(mockMethods).collectMockMethods(mockClass);
   }

   public RedefinitionEngine(Object mock, Class<?> mockClass)
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

   public boolean isWithMockClass() { return mockClass != null; }

   public Class<?> getRealClass() { return realClass; }
   public void setRealClass(Class<?> realClass) { this.realClass = realClass; }

   public void setUpStartupMock()
   {
      if (realClass != null) {
         redefineMethods(true);
      }
   }

   public void stubOut()
   {
      byte[] modifiedClassFile = stubOutClass();
      String classDesc = Type.getInternalName(realClass);
      redefineMethods(classDesc, modifiedClassFile);
   }

   private byte[] stubOutClass()
   {
      ClassReader rcReader = createClassReaderForRealClass();
      ClassVisitor rcWriter = new StubOutModifier(rcReader, mockingConfiguration);
      rcReader.accept(rcWriter, 0);
      return rcWriter.toByteArray();
   }

   public void stubOutAtStartup()
   {
      byte[] modifiedClassFile = stubOutClass();
      redefineMethods(modifiedClassFile);
   }

   public void redefineMethods()
   {
      redefineMethods(false);
   }

   private void redefineMethods(boolean forStartupMock)
   {
      if (mockMethods.getMethodCount() > 0 || mockingConfiguration != null) {
         byte[] modifiedClassFile = modifyRealClass(forStartupMock);
         redefineMethods(modifiedClassFile);

         if (forStartupMock) {
            TestRun.mockFixture().addFixedClass(realClass.getName(), modifiedClassFile);
         }
         else {
            addToMapOfRedefinedClasses(mockMethods.getMockClassInternalName(), modifiedClassFile);
         }
      }
   }

   private byte[] modifyRealClass(boolean forStartupMock)
   {
      ClassReader rcReader = createClassReaderForRealClass();

      AnnotationsModifier modifier =
         new AnnotationsModifier(rcReader, realClass, mock, mockMethods, mockingConfiguration, forStartupMock);

      if (mock == null && instantiation == Instantiation.PerMockedInstance) {
         modifier.useOneMockInstancePerMockedInstance(mockClass);
      }

      rcReader.accept(modifier, 0);
      validateThatAllMockMethodsWereApplied();
      return modifier.toByteArray();
   }

   private ClassReader createClassReaderForRealClass()
   {
      if (realClass.isInterface() || realClass.isArray()) {
         throw new IllegalArgumentException("Not a modifiable class: " + realClass.getName());
      }

      return new ClassFile(realClass, true).getReader();
   }

   private void validateThatAllMockMethodsWereApplied()
   {
      if (mockMethods.getMethodCount() > 0) {
         String classDesc = mockMethods.getMockClassInternalName();
         List<String> remainingMocks = mockMethods.getMethods();
         String mockSignatures = new MethodFormatter(classDesc).friendlyMethodSignatures(remainingMocks);

         throw new IllegalArgumentException(
            "Matching real methods not found for the following mocks:\n" + mockSignatures);
      }
   }

   public static void redefineClasses(ClassDefinition... definitions)
   {
      redefineMethods(definitions);

      MockFixture mockFixture = TestRun.mockFixture();
      
      for (ClassDefinition def : definitions) {
         mockFixture.addRedefinedClass(def.getDefinitionClass(), def.getDefinitionClassFile());
      }
   }

   public void redefineMethods(String mockClassInternalName, byte[] modifiedClassfile)
   {
      redefineMethods(modifiedClassfile);
      addToMapOfRedefinedClasses(mockClassInternalName, modifiedClassfile);
   }

   private void addToMapOfRedefinedClasses(String mockClassInternalName, byte[] modifiedClassfile)
   {
      TestRun.mockFixture().addRedefinedClass(mockClassInternalName, realClass, modifiedClassfile);
   }

   private void redefineMethods(byte[] modifiedClassfile)
   {
      redefineMethods(new ClassDefinition(realClass, modifiedClassfile));
   }

   public static void redefineMethods(ClassDefinition... classDefs)
   {
      try {
         Startup.instrumentation().redefineClasses(classDefs);
      }
      catch (ClassNotFoundException e) {
         // should never happen
         throw new RuntimeException(e);
      }
      catch (UnmodifiableClassException e) {
         throw new RuntimeException(e);
      }
   }

   public void redefineMethods(Map<Class<?>, byte[]> modifiedClassfiles)
   {
      ClassDefinition[] classDefs = new ClassDefinition[modifiedClassfiles.size()];
      int i = 0;

      for (Entry<Class<?>, byte[]> classAndBytecode : modifiedClassfiles.entrySet()) {
         realClass = classAndBytecode.getKey();
         byte[] modifiedClassfile = classAndBytecode.getValue();

         classDefs[i++] = new ClassDefinition(realClass, modifiedClassfile);
         addToMapOfRedefinedClasses(null, modifiedClassfile);
      }

      redefineMethods(classDefs);
   }

   public void restoreDefinition(Class<?> aClass, byte[] previousDefinition)
   {
      if (previousDefinition == null) {
         restoreOriginalDefinition(aClass);
      }
      else {
         restoreToDefinition(aClass, previousDefinition);
      }
   }

   public void restoreOriginalDefinition(Class<?> aClass)
   {
      realClass = aClass;
      byte[] realClassFile = new ClassFile(aClass, false).getBytecode();
      redefineMethods(realClassFile);
   }

   public void restoreToDefinitionBeforeStartup(Class<?> aClass) throws IOException
   {
      realClass = aClass;
      byte[] realClassFile = ClassFile.readClass(aClass).b;
      redefineMethods(realClassFile);
   }

   private void restoreToDefinition(Class<?> aClass, byte[] definitionToRestore)
   {
      realClass = aClass;
      redefineMethods(definitionToRestore);
   }

   public void restoreToDefinition(String className, byte[] definitionToRestore)
   {
      Class<?> aClass = Utilities.loadClass(className);
      restoreToDefinition(aClass, definitionToRestore);
   }
}
