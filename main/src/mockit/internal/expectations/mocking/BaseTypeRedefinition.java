/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
         createNewMockInstanceFactoryForInterface();
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

      targetClass = newImplementationClass(mockedInterface, modifier, mockClassName);
   }

   private Class<?> newImplementationClass(Class<?> interfaceOrAbstractClass, ClassWriter generator, String implName)
   {
      final byte[] generatedBytecode = generator.toByteArray();
      ClassLoader parentLoader = interfaceOrAbstractClass.getClassLoader();

      if (parentLoader == null) {
         parentLoader = getClass().getClassLoader();
      }

      return new ClassLoader(parentLoader)
      {
         @Override
         protected Class<?> findClass(String name)
         {
            return defineClass(name, generatedBytecode, 0, generatedBytecode.length);
         }
      }.findClass(implName);
   }

   private void createNewMockInstanceFactoryForInterface()
   {
      Object mock = Utilities.newInstanceUsingDefaultConstructor(targetClass);
      instanceFactory = new InterfaceInstanceFactory(mock);
   }

   final void redefineMethodsAndConstructorsInTargetType()
   {
      redefineClassAndItsSuperClasses(targetClass);
   }

   private void redefineClassAndItsSuperClasses(Class<?> realClass)
   {
      ClassReader classReader = createClassReader(realClass);
      ExpectationsModifier modifier = createModifier(realClass, classReader);

      redefineClass(realClass, classReader, modifier);

      Class<?> superClass = realClass.getSuperclass();

      if (superClass != null && superClass != Object.class && superClass != Proxy.class) {
         redefineClassAndItsSuperClasses(superClass);
      }
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

      if (targetClass.isEnum()) {
         instanceFactory = new EnumInstanceFactory(targetClass);
         redefineMethodsAndConstructorsInTargetType();
      }
      else if (isAbstract(targetClass.getModifiers())) {
         redefineMethodsAndConstructorsInTargetType();
         Class<?> subclass = generateConcreteSubclassForAbstractType();
         instanceFactory = new ClassInstanceFactory(subclass);
      }
      else {
         redefineMethodsAndConstructorsInTargetType();
         instanceFactory = new ClassInstanceFactory(targetClass);
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
      ClassDefinition[] classDefs = mockedClassDefinitions.toArray(new ClassDefinition[mockedClassDefinitions.size()]);
      MockedClass mockedClass = new MockedClass(instanceFactory, classDefs);

      mockedClasses.put(mockedClassId, mockedClass);
   }

   private Class<?> generateConcreteSubclassForAbstractType()
   {
      String subclassName = getNameForConcreteSubclassToCreate();

      ClassReader classReader = createClassReader(targetClass);
      SubclassGenerationModifier modifier =
         new SubclassGenerationModifier(typeMetadata.mockingCfg, targetClass, classReader, subclassName);
      classReader.accept(modifier, false);

      return newImplementationClass(targetClass, modifier, subclassName);
   }

   abstract String getNameForConcreteSubclassToCreate();
}