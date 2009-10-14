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

import mockit.internal.util.*;
import mockit.internal.expectations.mocking.SharedFieldTypeRedefinitions.*;

final class SharedTypeRedefinition extends TypeRedefinition
{
   SharedTypeRedefinition(Object objectWithInitializerMethods, MockedType typeMetadata)
   {
      super(objectWithInitializerMethods, typeMetadata);
   }

   @Override
   Object newRedefinedEmptyProxy()
   {
      final Object recordingMock = super.newRedefinedEmptyProxy();

      InstanceFactory instanceFactory = new InstanceFactory()
      {
         public Object create() { return recordingMock; }
      };

      return instanceFactory;
   }

   @Override
   Object newInstanceOfMockFieldAbstractClass()
   {
      final Class<?> subclass = targetClass;
      final MockConstructorInfo mockConstructorInfo = this.mockConstructorInfo;

      InstanceFactory instanceFactory = new InstanceFactory()
      {
         public Object create() { return mockConstructorInfo.newInstance(subclass); }
      };

      return instanceFactory;
   }

   @Override
   Object newInstanceOfMockFieldConcreteClass(final String constructorDesc)
   {
      final Class<?> concreteClass = targetClass;
      InstanceFactory instanceFactory;

      if (constructorDesc == null) {
         instanceFactory = new InstanceFactory()
         {
            public Object create()
            {
               Constructor<?> constructor = concreteClass.getDeclaredConstructors()[0];
               return Utilities.invoke(constructor, (Object[]) null);
            }
         };
      }
      else {
         instanceFactory = new InstanceFactory()
         {
            public Object create()
            {
               Class<?>[] constructorParamTypes = Utilities.getParameterTypes(constructorDesc);
               return Utilities.newInstance(concreteClass, constructorParamTypes, (Object[]) null);
            }
         };
      }

      return instanceFactory;
   }
}
