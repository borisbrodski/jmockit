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

import mockit.external.asm.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class CascadingTypeRedefinition extends BaseTypeRedefinition
{
   public CascadingTypeRedefinition(Class<?> mockedType)
   {
      super(mockedType);
   }

   public Object redefineType()
   {
      TestRun.getExecutingTest().setShouldIgnoreMockingCallbacks(true);
      Object mock;

      try {
         if (targetClass.isInterface()) {
            mock = newRedefinedEmptyProxy(targetClass);
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

   @Override
   ExpectationsModifier createModifier(Class<?> realClass, ClassReader classReader)
   {
      return new ExpectationsModifier(realClass.getClassLoader(), classReader);
   }

   @Override
   String getNameForConcreteSubclassToCreate()
   {
      return Utilities.GENERATED_SUBCLASS_PREFIX + targetClass.getSimpleName();
   }

   @Override
   Object newInstanceOfAbstractClass()
   {
      Constructor<?> constructor = targetClass.getDeclaredConstructors()[0];
      return Utilities.invoke(constructor);
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