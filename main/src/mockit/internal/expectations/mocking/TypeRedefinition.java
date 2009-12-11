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
import static java.lang.reflect.Modifier.*;

import mockit.external.asm.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

class TypeRedefinition extends BaseTypeRedefinition
{
   private final Object objectWithInitializerMethods;
   private final MockedType typeMetadata;

   TypeRedefinition(Object objectWithInitializerMethods, MockedType typeMetadata)
   {
      super(typeMetadata.getClassType());
      this.objectWithInitializerMethods = objectWithInitializerMethods;
      this.typeMetadata = typeMetadata;
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
         Object emptyProxy = newRedefinedEmptyProxy(typeMetadata.declaredType);
         mock = newInstanceOfInterface(emptyProxy);
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

   Object newInstanceOfInterface(Object mock)
   {
      return mock;
   }

   @Override
   final ExpectationsModifier createModifier(Class<?> realClass, ClassReader classReader)
   {
      MockConstructorInfo constructorInfoToUse =
         isAbstract(targetClass.getModifiers()) ? null : mockConstructorInfo;

      return new ExpectationsModifier(
         realClass.getClassLoader(), classReader, mockingCfg, constructorInfoToUse);
   }

   @Override
   final String getNameForConcreteSubclassToCreate()
   {
      return
         objectWithInitializerMethods.getClass().getPackage().getName() + '.' +
         Utilities.GENERATED_SUBCLASS_PREFIX + typeMetadata.mockId;
   }

   @Override
   Object newInstanceOfAbstractClass()
   {
      return mockConstructorInfo.newInstance(targetClass);
   }

   @Override
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
