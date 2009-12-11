/*
 * JMockit Expectations & Verifications
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
import java.lang.reflect.Type;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.InstanceFactory.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

import static java.lang.reflect.Modifier.isAbstract;

abstract class BaseTypeRedefinition
{
   Class<?> targetClass;
   InstanceFactory instanceFactory;
   MockingConfiguration mockingCfg;
   MockConstructorInfo mockConstructorInfo;

   BaseTypeRedefinition(Class<?> mockedType)
   {
      targetClass = mockedType;
   }

   final Object newRedefinedEmptyProxy(Type mockedInterface)
   {
      Object mock = Mockit.newEmptyProxy(mockedInterface);
      targetClass = mock.getClass();

      redefineMethodsAndConstructorsInTargetType();

      instanceFactory = new InterfaceInstanceFactory(mock);

      return mock;
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

      new RedefinitionEngine(realClass).redefineMethods(null, modifiedClass, true);
   }

   private ClassReader createClassReader(Class<?> realClass)
   {
      return new ClassFile(realClass, true).getReader();
   }

   final Object createNewInstanceOfTargetClass()
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
      String subclassName = getNameForConcreteSubclassToCreate();

      ClassReader classReader = createClassReader(targetClass);
      SubclassGenerationModifier modifier =
         new SubclassGenerationModifier(mockConstructorInfo, mockingCfg, classReader, subclassName);
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

   abstract String getNameForConcreteSubclassToCreate();
   abstract Object newInstanceOfAbstractClass();
   abstract Object newInstanceOfConcreteClass(String constructorDesc);
}