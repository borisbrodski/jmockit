/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
