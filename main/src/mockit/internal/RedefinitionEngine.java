/*
 * JMockit
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
package mockit.internal;

import java.lang.instrument.*;
import java.util.*;
import java.util.Map.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.annotations.*;
import mockit.internal.core.*;
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
   private final MockMethods mockMethods;
   private Object mock;

   public RedefinitionEngine()
   {
      this(null, null, null, null);
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

         AnnotatedMockMethods annotatedMocks = new AnnotatedMockMethods(realClass);
         new AnnotatedMockMethodCollector(annotatedMocks).collectMockMethods(mockClass);
         mockMethods = annotatedMocks;

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

   public RedefinitionEngine(
      Class<?> realClass, boolean filtersNotInverted, String[] stubbingFilters)
   {
      this.realClass = realClass;
      mockClass = null;
      instantiation = Instantiation.PerMockInvocation;
      mockMethods = null;
      mockingConfiguration = createMockingConfiguration(stubbingFilters, filtersNotInverted);
   }

   public RedefinitionEngine(Class<?> realClass, Object mock, Class<?> mockClass)
   {
      this(realClass, mockClass, mock, new AnnotatedMockMethods(realClass));
      new AnnotatedMockMethodCollector((AnnotatedMockMethods) mockMethods).collectMockMethods(
         mockClass);
   }

   public RedefinitionEngine(
      Class<?> realClass, Class<?> mockClass, Object mock, MockMethods mockMethods)
   {
      this.realClass = realClass;
      this.mockClass = mockClass;
      this.mock = mock;
      this.mockMethods = mockMethods;

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
   }

   public RedefinitionEngine(
      Class<?> realClass, Object mock, Class<?> mockClass, boolean allowDefaultConstructor)
   {
      this(realClass, mockClass, mock, new MockMethods());
      new MockMethodCollector(mockMethods, allowDefaultConstructor).collectMockMethods(mockClass);
   }

   public RedefinitionEngine(
      Object mock, Class<?> mockClass, boolean ignoreRealClassIfNotInClasspath)
   {
      this(getRealClass(mockClass, ignoreRealClassIfNotInClasspath), mock, mockClass);
   }

   private static Class<?> getRealClass(
      Class<?> specifiedMockClass, boolean ignoreRealClassIfNotInClasspath)
   {
      try {
         MockClass mockClassAnnotation = specifiedMockClass.getAnnotation(MockClass.class);

         if (mockClassAnnotation == null) {
            throw new IllegalArgumentException("Missing @MockClass for " + specifiedMockClass);
         }

         return mockClassAnnotation.realClass();
      }
      catch (TypeNotPresentException e) {
         if (ignoreRealClassIfNotInClasspath) {
            return null;
         }

         throw e;
      }
   }

   public boolean isWithMockClass()
   {
      return mockClass != null;
   }

   public Class<?> getRealClass()
   {
      return realClass;
   }

   public void setRealClass(Class<?> realClass)
   {
      this.realClass = realClass;
   }

   public void setUpStartupMock()
   {
      if (realClass != null) {
         redefineMethods(true);
      }
   }

   public void stubOut()
   {
      ClassReader rcReader = createClassReaderForRealClass();
      ClassWriter rcWriter = new StubOutModifier(rcReader, mockingConfiguration);

      rcReader.accept(rcWriter, false);
      byte[] modifiedClassFile = rcWriter.toByteArray();

      String classDesc = realClass.getName().replace('.', '/');
      redefineMethods(classDesc, modifiedClassFile, true);
   }

   public void redefineMethods()
   {
      redefineMethods(false);
   }

   private void redefineMethods(boolean forStartupMock)
   {
      if (mockMethods.getMethodCount() > 0 || mockingConfiguration != null) {
         byte[] modifiedClassFile = modifyRealClass(forStartupMock);
         redefineMethods(
            mockMethods.getMockClassInternalName(), modifiedClassFile, !forStartupMock);
      }
   }

   private byte[] modifyRealClass(boolean forStartupMock)
   {
      ClassReader rcReader = createClassReaderForRealClass();
      ClassWriter rcWriter;

      if (mockMethods instanceof AnnotatedMockMethods) {
         AnnotationsModifier modifier = new AnnotationsModifier(
            rcReader, realClass, mock, (AnnotatedMockMethods) mockMethods, mockingConfiguration,
            forStartupMock);

         if (mock == null && instantiation == Instantiation.PerMockedInstance) {
            modifier.useOneMockInstancePerMockedInstance(mockClass);
         }

         rcWriter = modifier;
      }
      else {
         rcWriter = new RealClassModifier(rcReader, realClass, mock, mockMethods, forStartupMock);
      }

      return modifyRealClass(rcReader, rcWriter, mockClass.getName());
   }

   private ClassReader createClassReaderForRealClass()
   {
      if (realClass.isInterface() || realClass.isArray()) {
         throw new IllegalArgumentException("Not a modifiable class: " + realClass.getName());
      }

      return new ClassFile(realClass, true).getReader();
   }

   public byte[] modifyRealClass(ClassReader rcReader, ClassWriter rcWriter, String mockClassName)
   {
      rcReader.accept(rcWriter, false);

      List<String> remainingMocks = mockMethods.getMethods();

      if (!remainingMocks.isEmpty()) {
         int p = mockClassName.lastIndexOf('.');
         String constructorName = p > 0 ? mockClassName.substring(p + 1) : mockClassName;
         String mockSignatures =
            new MethodFormatter().friendlyMethodSignatures(constructorName, remainingMocks);
         
         throw new IllegalArgumentException(
            "Matching real methods not found for the following mocks:\n" + mockSignatures);
      }

      return rcWriter.toByteArray();
   }

   public void redefineMethods(
      String mockClassInternalName, byte[] modifiedClassfile, boolean register)
   {
      redefineMethods(modifiedClassfile);

      if (register) {
         addToMapOfRedefinedClasses(mockClassInternalName, modifiedClassfile);
      }
   }

   private void addToMapOfRedefinedClasses(String classInternalName, byte[] modifiedClassfile)
   {
      TestRun.mockFixture().addRedefinedClass(classInternalName, realClass, modifiedClassfile);
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

   public void restoreOriginalDefinition(Class<?> aClass)
   {
      realClass = aClass;
      byte[] realClassFile = new ClassFile(aClass, false).getBytecode();
      redefineMethods(realClassFile);
   }

   public void restoreToDefinition(String className, byte[] definitionToRestore)
   {
      realClass = Utilities.loadClass(className);
      redefineMethods(definitionToRestore);
   }
}
