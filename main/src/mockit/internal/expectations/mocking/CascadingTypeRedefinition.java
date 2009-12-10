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

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.InstanceFactory.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

import static java.lang.reflect.Modifier.*;

public final class CascadingTypeRedefinition // TODO: extract new base subclass
{
   private Class<?> targetClass;
   private InstanceFactory instanceFactory;

   public CascadingTypeRedefinition(Class<?> mockedType)
   {
      targetClass = mockedType;
   }

   public Object redefineType()
   {
      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);
      Object mock;

      try {
         if (targetClass.isInterface()) {
            mock = newRedefinedEmptyProxy();
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

   private Object newRedefinedEmptyProxy()
   {
      Object mock = Mockit.newEmptyProxy(targetClass);
      targetClass = mock.getClass();

      redefineMethodsAndConstructorsInTargetType();

      instanceFactory = new InterfaceInstanceFactory(mock);

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
      ClassReader classReader = createClassReaderForFieldType(realClass);
      ExpectationsModifier modifier =
         new ExpectationsModifier(realClass.getClassLoader(), classReader);

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
            instanceFactory = new AbstractClassInstanceFactory(null, targetClass);
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
      String subclassName = Utilities.GENERATED_SUBCLASS_PREFIX + targetClass.getSimpleName();

      ClassReader classReader = createClassReaderForFieldType(targetClass);
      SubclassGenerationModifier modifier =
         new SubclassGenerationModifier(null, null, classReader, subclassName);
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

   private Object newInstanceOfAbstractClass()
   {
      Constructor<?> constructor = targetClass.getDeclaredConstructors()[0];
      return Utilities.invoke(constructor);
   }

   private Object newInstanceOfConcreteClass(String constructorDesc)
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