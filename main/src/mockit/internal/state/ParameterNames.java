/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.util.*;

import mockit.external.asm4.*;

public final class ParameterNames
{
   private static final Map<String, Map<String, String[]>> classesToMethodToParameters =
      new HashMap<String, Map<String, String[]>>();

   public static void registerName(String classDesc, String methodName, String methodDesc, int index, String name)
   {
      Map<String, String[]> methodsToParameters = classesToMethodToParameters.get(classDesc);

      if (methodsToParameters == null) {
         methodsToParameters = new HashMap<String, String[]>();
         classesToMethodToParameters.put(classDesc, methodsToParameters);
      }

      String methodKey = methodName + methodDesc;
      String[] parameterNames = methodsToParameters.get(methodKey);

      if (parameterNames == null) {
         parameterNames = new String[Type.getArgumentTypes(methodDesc).length];
         methodsToParameters.put(methodKey, parameterNames);
      }

      if (index < parameterNames.length) {
         parameterNames[index] = name;
      }
   }

   public static String getName(String classDesc, String methodDesc, int index)
   {
      Map<String, String[]> methodsToParameters = classesToMethodToParameters.get(classDesc);

      if (methodsToParameters == null) {
         return null;
      }

      String[] parameterNames = methodsToParameters.get(methodDesc);
      return parameterNames == null ? null : parameterNames[index];
   }
}
