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

import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.InstanceFactory.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

class TypeRedefinition
{
   private final Object objectWithInitializerMethods;
   private final MockedType typeMetadata;
   MockingConfiguration mockingCfg;
   MockConstructorInfo mockConstructorInfo;
   protected Class<?> targetClass;
   protected InstanceFactory instanceFactory;

   TypeRedefinition(Object objectWithInitializerMethods, MockedType typeMetadata)
   {
      this.objectWithInitializerMethods = objectWithInitializerMethods;
      this.typeMetadata = typeMetadata;
      targetClass = typeMetadata.getClassType();
   }

   final void redefineTypeForFinalField()
   {
      buildMockingConfigurationFromSpecifiedMetadata();
      adjustTargetClassIfRealClassNameSpecified();

      if (targetClass == null || targetClass.isInterface()) {
         throw new IllegalArgumentException(
            "Final mock field must be of a class type, or otherwise the real class must be " +
            "specified through the @Mocked annotation:\n" + typeMetadata.mockId);
      }

      redefineMethodsAndConstructorsInTargetType();
   }

   final Object redefineType()
   {
      buildMockingConfigurationFromSpecifiedMetadata();
      adjustTargetClassIfRealClassNameSpecified();

      Object mock;

      if (targetClass == null || targetClass.isInterface()) {
         mock = newRedefinedEmptyProxy();
      }
      else {
         mock = createNewInstanceOfTargetClass();
      }

      TestRun.mockFixture().addInstanceForMockedType(targetClass, instanceFactory);
      
      return mock;
   }

   private void buildMockingConfigurationFromSpecifiedMetadata()
   {
      boolean filterResultWhenMatching = !typeMetadata.hasInverseFilters();
      mockingCfg = new MockingConfiguration(typeMetadata.getFilters(), filterResultWhenMatching);
      mockConstructorInfo = new MockConstructorInfo(objectWithInitializerMethods, typeMetadata);
   }

   private void adjustTargetClassIfRealClassNameSpecified()
   {
      String realClassName = typeMetadata.getRealClassName();

      if (realClassName.length() > 0) {
         targetClass = Utilities.loadClass(realClassName);
      }
   }

   private Object newRedefinedEmptyProxy()
   {
      Object mock = Mockit.newEmptyProxy(typeMetadata.declaredType);
      targetClass = mock.getClass();

      redefineMethodsAndConstructorsInTargetType();

      instanceFactory = new InterfaceInstanceFactory(mock);

      return newInstanceOfInterface(mock);
   }

   Object newInstanceOfInterface(Object mock)
   {
      return mock;
   }

   private ExpectationsModifier redefineMethodsAndConstructorsInTargetType()
   {
      return redefineClassAndItsSuperClasses(targetClass);
   }

   private ExpectationsModifier redefineClassAndItsSuperClasses(Class<?> realClass)
   {
      ExpectationsModifier modifier = redefineClass(realClass);
      Class<?> superClass = realClass.getSuperclass();

      if (superClass != Object.class && superClass != Proxy.class) {
         redefineClassAndItsSuperClasses(superClass);
      }

      return modifier;
   }

   private ExpectationsModifier redefineClass(Class<?> realClass)
   {
      MockConstructorInfo constructorInfoToUse =
         isAbstract(targetClass.getModifiers()) ? null : mockConstructorInfo;

      ClassReader classReader = createClassReaderForFieldType(realClass);
      ExpectationsModifier modifier =
         new ExpectationsModifier(
            realClass.getClassLoader(), classReader, mockingCfg, constructorInfoToUse);
      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      new RedefinitionEngine(realClass).redefineMethods(null, modifiedClass, true);
      return modifier;
   }

   private ClassReader createClassReaderForFieldType(Class<?> realClass)
   {
      return new ClassFile(realClass, true).getReader();
   }

   private Object createNewInstanceOfTargetClass()
   {
      ExpectationsModifier modifier = redefineMethodsAndConstructorsInTargetType();
      TestRun.exitNoMockingZone();

      try {
         if (isAbstract(targetClass.getModifiers())) {
            generateConcreteSubclassForAbstractType();
            instanceFactory = new AbstractClassInstanceFactory(mockConstructorInfo, targetClass);
            return newInstanceOfAbstractClass();
         }
         else if (!targetClass.isEnum()) {
            String constructorDesc = modifier.getRedefinedConstructorDesc();
            instanceFactory = new ConcreteClassInstanceFactory(targetClass, constructorDesc);
            return newInstanceOfConcreteClass(constructorDesc);
         }
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

      return null;
   }

   private void generateConcreteSubclassForAbstractType()
   {
      String subclassName =
         objectWithInitializerMethods.getClass().getPackage().getName() + '.' +
         Utilities.GENERATED_SUBCLASS_PREFIX + typeMetadata.mockId;

      ClassReader classReader = createClassReaderForFieldType(targetClass);
      SubclassGenerationModifier modifier =
         new SubclassGenerationModifier(
            mockConstructorInfo, mockingCfg, classReader, subclassName);
      classReader.accept(modifier, false);
      final byte[] modifiedClass = modifier.toByteArray();

      targetClass = new ClassLoader()
      {
         @Override
         protected Class<?> findClass(String name)
         {
            return defineClass(name, modifiedClass, 0, modifiedClass.length);
         }
      }.findClass(subclassName);
   }

   Object newInstanceOfAbstractClass()
   {
      return mockConstructorInfo.newInstance(targetClass);
   }

   Object newInstanceOfConcreteClass(String constructorDesc)
   {
      Object[] initArgs = null;

      if (constructorDesc == null) {
         Constructor<?> constructor = targetClass.getDeclaredConstructors()[0];
         return Utilities.invoke(constructor, initArgs);
      }
      else {
         Class<?>[] constructorParamTypes = Utilities.getParameterTypes(constructorDesc);
         return Utilities.newInstance(targetClass, constructorParamTypes, initArgs);
      }
   }
}
