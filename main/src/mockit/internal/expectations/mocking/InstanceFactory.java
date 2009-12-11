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

import mockit.internal.util.*;

public interface InstanceFactory
{
   Object create();

   final class InterfaceInstanceFactory implements InstanceFactory
   {
      private final Object emptyProxy;

      InterfaceInstanceFactory(Object emptyProxy)
      {
         this.emptyProxy = emptyProxy;
      }

      public Object create()
      {
         return emptyProxy;
      }
   }

   final class AbstractClassInstanceFactory implements InstanceFactory
   {
      private final MockConstructorInfo mockConstructorInfo;
      private final Class<?> subclass;

      AbstractClassInstanceFactory(MockConstructorInfo mockConstructorInfo, Class<?> subclass)
      {
         this.mockConstructorInfo = mockConstructorInfo;
         this.subclass = subclass;
      }

      public Object create()
      {
         if (mockConstructorInfo != null) {
            return mockConstructorInfo.newInstance(subclass);
         }

         Constructor<?> constructor = subclass.getDeclaredConstructors()[0];
         return Utilities.invoke(constructor);
      }
   }

   final class ConcreteClassInstanceFactory implements InstanceFactory
   {
      private final Class<?> concreteClass;
      private final String constructorDesc;

      ConcreteClassInstanceFactory(Class<?> concreteClass, String constructorDesc)
      {
         this.concreteClass = concreteClass;
         this.constructorDesc = constructorDesc;
      }

      public Object create()
      {
         Object[] initArgs = null;

         if (constructorDesc == null) {
            Constructor<?> constructor = concreteClass.getDeclaredConstructors()[0];
            return Utilities.invoke(constructor, initArgs);
         }
         else {
            Class<?>[] constructorParamTypes = Utilities.getParameterTypes(constructorDesc);
            return Utilities.newInstance(concreteClass, constructorParamTypes, initArgs);
         }
      }
   }
}
