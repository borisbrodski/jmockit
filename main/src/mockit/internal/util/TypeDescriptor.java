/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import mockit.external.asm4.Type;

public final class TypeDescriptor
{
   private static final Class<?>[] PRIMITIVE_TYPES = {
      void.class, boolean.class, char.class, byte.class, short.class, int.class, float.class, long.class, double.class
   };

   public static Class<?>[] getParameterTypes(String methodDesc)
   {
      Type[] paramTypes = Type.getArgumentTypes(methodDesc);

      if (paramTypes.length == 0) {
         return ParameterReflection.NO_PARAMETERS;
      }

      Class<?>[] paramClasses = new Class<?>[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
         paramClasses[i] = getClassForType(paramTypes[i]);
      }

      return paramClasses;
   }

   public static Class<?> getReturnType(String methodDesc)
   {
      int p = methodDesc.indexOf('<');

      if (p > 0) {
         //noinspection AssignmentToMethodParameter
         methodDesc = methodDesc.substring(0, p) + ';';
      }

      Type returnType = Type.getReturnType(methodDesc);
      return getClassForType(returnType);
   }

   public static Class<?> getClassForType(Type type)
   {
      int sort = type.getSort();

      if (sort < PRIMITIVE_TYPES.length) {
         return PRIMITIVE_TYPES[sort];
      }

      String className = sort == Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName();
      return ClassLoad.loadClass(className);
   }
}
