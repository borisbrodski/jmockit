/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.util.*;

public final class DynamicPartialMocking
{
   private final List<Class<?>> targetClasses;
   private final Map<Class<?>, byte[]> modifiedClassfiles;

   public DynamicPartialMocking()
   {
      targetClasses = new ArrayList<Class<?>>(2);
      modifiedClassfiles = new HashMap<Class<?>, byte[]>();
   }

   public List<Class<?>> getTargetClasses()
   {
      return targetClasses;
   }

   public void redefineTypes(Object[] classesOrInstancesToBePartiallyMocked)
   {
      for (Object classOrInstance : classesOrInstancesToBePartiallyMocked) {
         redefineTargetType(classOrInstance);
      }

      new RedefinitionEngine().redefineMethods(modifiedClassfiles);
      modifiedClassfiles.clear();
   }

   private void redefineTargetType(Object classOrInstance)
   {
      Class<?> targetClass;

      if (classOrInstance instanceof Class) {
         targetClass = (Class<?>) classOrInstance;
         validateTargetClassType(targetClass);
         redefineClass(targetClass, false);
      }
      else {
         targetClass = classOrInstance.getClass();
         validateTargetClassType(targetClass);
         redefineClassAndItsSuperClasses(targetClass);
      }

      targetClasses.add(targetClass);
   }

   private void validateTargetClassType(Class<?> targetClass)
   {
      if (
         targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isArray() ||
         targetClass.isPrimitive() || Utilities.isWrapperOfPrimitiveType(targetClass) ||
         Utilities.isGeneratedImplementationClass(targetClass)
      ) {
         throw new IllegalArgumentException("Invalid type for dynamic mocking: " + targetClass);
      }
   }

   private void redefineClassAndItsSuperClasses(Class<?> realClass)
   {
      redefineClass(realClass, true);
      Class<?> superClass = realClass.getSuperclass();

      if (superClass != Object.class && superClass != Proxy.class) {
         redefineClassAndItsSuperClasses(superClass);
      }
   }

   private void redefineClass(Class<?> realClass, boolean methodsOnly)
   {
      ClassReader classReader = new ClassFile(realClass, false).getReader();

      ExpectationsModifier modifier = new ExpectationsModifier(realClass.getClassLoader(), classReader, null);
      modifier.useDynamicMocking(methodsOnly);

      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      modifiedClassfiles.put(realClass, modifiedClass);
   }
}
