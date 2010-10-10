/*
 * JMockit Expectations
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

import java.lang.reflect.*;

import mockit.internal.util.*;

import mockit.external.asm.Type;

final class MockConstructorInfo
{
   private final Class<?>[] superConstructorParameterTypes;
   private final Object[] superConstructorArgs;

   MockConstructorInfo(Object targetObject, MockedType typeMetadata)
   {
      if (typeMetadata != null) {
         String methodName = typeMetadata.getConstructorArgsMethod();

         if (methodName.length() > 0) {
            Method m = findMethodForConstructorArguments(targetObject.getClass(), methodName);
            superConstructorParameterTypes = m.getParameterTypes();
            Object[] defaultArgs = getDefaultArgs();
            superConstructorArgs = Utilities.invoke(targetObject, m, defaultArgs);
            return;
         }
      }

      superConstructorParameterTypes = null;
      superConstructorArgs = null;
   }

   private Method findMethodForConstructorArguments(Class<?> classOfTargetObject, String methodName)
   {
      for (Method m : classOfTargetObject.getDeclaredMethods()) {
         if (m.getName().equals(methodName)) {
            return m;
         }
      }

      throw new IllegalArgumentException(
         "Method for constructor arguments \"" + methodName + "\" not found in class " + classOfTargetObject.getName());
   }

   private Object[] getDefaultArgs()
   {
      Object[] defaultArgs = new Object[superConstructorParameterTypes.length];

      for (int i = 0; i < defaultArgs.length; i++) {
         defaultArgs[i] = DefaultValues.computeForType(superConstructorParameterTypes[i]);
      }

      return defaultArgs;
   }

   boolean isWithSuperConstructor()
   {
      return superConstructorParameterTypes != null;
   }

   Type[] getParameterTypesForSuperConstructor()
   {
      Type[] paramTypes = new Type[superConstructorParameterTypes.length];

      for (int i = 0; i < superConstructorParameterTypes.length; i++) {
         paramTypes[i] = Type.getType(superConstructorParameterTypes[i]);
      }

      return paramTypes;
   }

   Object[] getSuperConstructorArguments()
   {
      return superConstructorArgs;
   }

   Object newInstance(Class<?> mockedClass)
   {
      Constructor<?> constructor = mockedClass.getDeclaredConstructors()[0];

      return
         superConstructorParameterTypes == null ?
            Utilities.invoke(constructor) : Utilities.invoke(constructor, (Object) superConstructorArgs);
   }
}
