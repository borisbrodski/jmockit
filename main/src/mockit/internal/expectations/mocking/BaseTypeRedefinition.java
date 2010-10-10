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

import java.lang.instrument.*;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;

import static java.lang.reflect.Modifier.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.InstanceFactory.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

abstract class BaseTypeRedefinition
{
   private static final class MockedClass
   {
      final InstanceFactory instanceFactory;
      final ClassDefinition[] mockedClassDefinitions;

      MockedClass(InstanceFactory instanceFactory, ClassDefinition[] classDefinitions)
      {
         this.instanceFactory = instanceFactory;
         mockedClassDefinitions = classDefinitions;
      }

      void redefineClasses()
      {
         RedefinitionEngine.redefineClasses(mockedClassDefinitions);
      }
   }

   private static final Map<Integer, MockedClass> mockedClasses = new HashMap<Integer, MockedClass>();
   private static final Map<Class<?>, Class<?>> mockInterfaces = new HashMap<Class<?>, Class<?>>();

   Class<?> targetClass;
   MockedType typeMetadata;
   InstanceFactory instanceFactory;
   MockingConfiguration mockingCfg;
   MockConstructorInfo mockConstructorInfo;
   private List<ClassDefinition> mockedClassDefinitions;

   BaseTypeRedefinition(Class<?> mockedType) { targetClass = mockedType; }

   final Object redefineType(Type typeToMock)
   {
      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);

      Object mock;

      try {
         if (targetClass == null || targetClass.isInterface()) {
            createMockedInterfaceImplementation(typeToMock);
            mock = instanceFactory.create();
         }
         else {
            mock = createNewInstanceOfTargetClass();
         }
      }
      finally {
         TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(false);
      }

      TestRun.mockFixture().addInstanceForMockedType(targetClass, instanceFactory);

      return mock;
   }

   private void createMockedInterfaceImplementation(Type typeToMock)
   {
      Class<?> mockedInterface = interfaceToMock(typeToMock);

      if (mockedInterface == null) {
         createMockInterfaceImplementationUsingStandardProxy(typeToMock);
         return;
      }

      Class<?> mockClass = mockInterfaces.get(mockedInterface);

      if (mockClass != null) {
         targetClass = mockClass;

         if (typeMetadata != null && typeMetadata.fieldFromTestClass) {
            instanceFactory = TestRun.mockFixture().getMockedTypesAndInstances().get(mockClass);
         }
         else {
            createNewMockInstanceFactoryForInterface();
         }

         return;
      }

      generateNewMockImplementationClassForInterface(mockedInterface);
      createNewMockInstanceFactoryForInterface();

      mockInterfaces.put(mockedInterface, targetClass);
   }

   private Class<?> interfaceToMock(Type typeToMock)
   {
      if (typeToMock instanceof Class<?>) {
         Class<?> theInterface = (Class<?>) typeToMock;

         if (isPublic(theInterface.getModifiers()) && !theInterface.isAnnotation()) {
            return theInterface;
         }
      }

      return null;
   }

   private void createMockInterfaceImplementationUsingStandardProxy(Type typeToMock)
   {
      Object mock = Mockit.newEmptyProxy(typeToMock);
      targetClass = mock.getClass();

      redefineMethodsAndConstructorsInTargetType();

      instanceFactory = new InterfaceInstanceFactory(mock);
   }

   private void generateNewMockImplementationClassForInterface(Class<?> mockedInterface)
   {
      ClassReader interfaceReader = ClassFile.createClassFileReader(mockedInterface.getName());
      String mockClassName = Utilities.GENERATED_IMPLCLASS_PREFIX + mockedInterface.getSimpleName();
      ClassWriter modifier = new InterfaceImplementationGenerator(interfaceReader, mockClassName);
      interfaceReader.accept(modifier, true);
      final byte[] generatedClass = modifier.toByteArray();

      targetClass = new ClassLoader()
      {
         @Override
         protected Class<?> findClass(String name)
         {
            return defineClass(name, generatedClass, 0, generatedClass.length);
         }
      }.findClass(mockClassName);
   }

   private void createNewMockInstanceFactoryForInterface()
   {
      Object mock = Utilities.newInstanceUsingDefaultConstructor(targetClass);
      instanceFactory = new InterfaceInstanceFactory(mock);
   }

   final ExpectationsModifier redefineMethodsAndConstructorsInTargetType()
   {
      return redefineClassAndItsSuperClasses(targetClass);
   }

   private ExpectationsModifier redefineClassAndItsSuperClasses(Class<?> realClass)
   {
      ClassReader classReader = createClassReader(realClass);
      ExpectationsModifier modifier = createModifier(realClass, classReader);

      redefineClass(realClass, classReader, modifier);

      Class<?> superClass = realClass.getSuperclass();

      if (superClass != Object.class && superClass != Proxy.class) {
         redefineClassAndItsSuperClasses(superClass);
      }

      return modifier;
   }

   abstract ExpectationsModifier createModifier(Class<?> realClass, ClassReader classReader);

   private void redefineClass(Class<?> realClass, ClassReader classReader, ClassWriter modifier)
   {
      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      ClassDefinition classDefinition = new ClassDefinition(realClass, modifiedClass);
      RedefinitionEngine.redefineClasses(classDefinition);

      if (mockedClassDefinitions != null) {
         mockedClassDefinitions.add(classDefinition);
      }
   }

   private ClassReader createClassReader(Class<?> realClass)
   {
      return new ClassFile(realClass, true).getReader();
   }

   private Object createNewInstanceOfTargetClass()
   {
      createInstanceFactoryForRedefinedClass();

      TestRun.exitNoMockingZone();

      try {
         return instanceFactory.create();
      }
      catch (ExceptionInInitializerError e) {
         Utilities.filterStackTrace(e);
         Utilities.filterStackTrace(e.getCause());
         e.printStackTrace();
         throw e;
      }
      finally {
         TestRun.enterNoMockingZone();
      }
   }

   private void createInstanceFactoryForRedefinedClass()
   {
      Integer mockedClassId = redefineClassesFromCache();

      if (mockedClassId == null) {
         return;
      }

      if (isAbstract(targetClass.getModifiers())) {
         redefineMethodsAndConstructorsInTargetType();
         Class<?> subclass = generateConcreteSubclassForAbstractType();
         instanceFactory = new AbstractClassInstanceFactory(mockConstructorInfo, subclass);
      }
      else if (targetClass.isEnum()) {
         instanceFactory = new EnumInstanceFactory(targetClass);
         redefineMethodsAndConstructorsInTargetType();
      }
      else {
         ExpectationsModifier modifier = redefineMethodsAndConstructorsInTargetType();
         String constructorDesc = modifier.getRedefinedConstructorDesc();
         instanceFactory = new ConcreteClassInstanceFactory(targetClass, constructorDesc);
      }

      storeRedefinedClassesInCache(mockedClassId);
   }

   final Integer redefineClassesFromCache()
   {
      Integer mockedClassId = typeMetadata != null ? typeMetadata.hashCode() : targetClass.hashCode();
      MockedClass mockedClass = mockedClasses.get(mockedClassId);

      if (mockedClass != null) {
         mockedClass.redefineClasses();
         instanceFactory = mockedClass.instanceFactory;
         return null;
      }

      mockedClassDefinitions = new ArrayList<ClassDefinition>();
      return mockedClassId;
   }

   final void storeRedefinedClassesInCache(Integer mockedClassId)
   {
      MockedClass mockedClass =
         new MockedClass(
            instanceFactory, mockedClassDefinitions.toArray(new ClassDefinition[mockedClassDefinitions.size()]));

      mockedClasses.put(mockedClassId, mockedClass);
   }

   private Class<?> generateConcreteSubclassForAbstractType()
   {
      String subclassName = getNameForConcreteSubclassToCreate();

      ClassReader classReader = createClassReader(targetClass);
      SubclassGenerationModifier modifier =
         new SubclassGenerationModifier(mockConstructorInfo, mockingCfg, targetClass, classReader, subclassName);
      classReader.accept(modifier, false);
      final byte[] modifiedClass = modifier.toByteArray();

      return new ClassLoader()
      {
         @Override
         protected Class<?> findClass(String name)
         {
            return defineClass(name, modifiedClass, 0, modifiedClass.length);
         }
      }.findClass(subclassName);
   }

   abstract String getNameForConcreteSubclassToCreate();
}