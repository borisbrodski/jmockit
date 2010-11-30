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

import java.lang.reflect.*;

import sun.reflect.*;

public interface InstanceFactory
{
   Object create();

   final class InterfaceInstanceFactory implements InstanceFactory
   {
      private final Object emptyProxy;

      InterfaceInstanceFactory(Object emptyProxy) { this.emptyProxy = emptyProxy; }

      public Object create() { return emptyProxy; }
   }

   @SuppressWarnings({"UseOfSunClasses"})
   final class ClassInstanceFactory implements InstanceFactory
   {
      private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();
      private static final Constructor<?> OBJECT_CONSTRUCTOR;
      static
      {
         try { OBJECT_CONSTRUCTOR = Object.class.getConstructor(); }
         catch (NoSuchMethodException e) { throw new RuntimeException(e); }
      }

      private final Constructor<?> fakeConstructor;

      ClassInstanceFactory(Class<?> concreteClass)
      {
         fakeConstructor = REFLECTION_FACTORY.newConstructorForSerialization(concreteClass, OBJECT_CONSTRUCTOR);
      }

      public Object create()
      {
         try { return fakeConstructor.newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
      }
   }

   final class EnumInstanceFactory implements InstanceFactory
   {
      private final Object anEnumValue;

      EnumInstanceFactory(Class<?> enumClass) { anEnumValue = enumClass.getEnumConstants()[0]; }

      public Object create() { return anEnumValue; }
   }
}
