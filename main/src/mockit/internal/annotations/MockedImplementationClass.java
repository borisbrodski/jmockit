/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;

import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.util.*;

public final class MockedImplementationClass<T>
{
   private final Object mockInstance;

   public MockedImplementationClass(Object mockInstance) { this.mockInstance = mockInstance; }

   public MockedImplementationClass(Class<?> mockClass, Object mockInstance)
   {
      this(mockInstance == null ? ConstructorReflection.newInstance(mockClass) : mockInstance);
   }

   public T generate(Class<T> interfaceToBeMocked, ParameterizedType typeToMock)
   {
      if (!isPublic(interfaceToBeMocked.getModifiers())) {
         T proxy = EmptyProxy.Impl.newEmptyProxy(interfaceToBeMocked.getClassLoader(), interfaceToBeMocked);
         new MockClassSetup(proxy.getClass(), null, mockInstance, null).redefineMethods();
         return proxy;
      }

      ImplementationClass<T> implementationClass = new ImplementationClass<T>(interfaceToBeMocked) {
         @Override
         protected ClassVisitor createMethodBodyGenerator(ClassReader typeReader, String className)
         {
            return new InterfaceImplementationGenerator(typeReader, className);
         }
      };

      Class<T> generatedClass = implementationClass.generateNewMockImplementationClassForInterface();
      byte[] generatedBytecode = implementationClass.getGeneratedBytecode();

      T proxy = ConstructorReflection.newInstanceUsingDefaultConstructor(generatedClass);

      MockClassSetup setup = new MockClassSetup(generatedClass, typeToMock, mockInstance, generatedBytecode);
      setup.redefineMethods();

      return proxy;
   }
}
