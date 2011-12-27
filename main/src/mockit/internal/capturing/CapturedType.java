/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import java.lang.reflect.*;

import mockit.*;
import mockit.internal.util.*;

final class CapturedType
{
   final Class<?> baseType;
   private final String[] classNameFilters;
   private final boolean inverseFilters;

   CapturedType(Class<?> baseType, Capturing capturing)
   {
      classNameFilters = capturing.classNames();
      inverseFilters = capturing.inverse();
      this.baseType = baseType;
   }

   boolean isToBeCaptured(Class<?> aClass)
   {
      return
         aClass != baseType && !Proxy.isProxyClass(aClass) && baseType.isAssignableFrom(aClass) &&
         isToBeCaptured(aClass.getName());
   }
   
   boolean isToBeCaptured(String className)
   {
      if (Utilities.isGeneratedClass(className)) {
         return false;
      }

      if (classNameFilters == null || classNameFilters.length == 0) {
         return true;
      }

      for (String classNameRegex : classNameFilters) {
         if (className.matches(classNameRegex)) {
            return !inverseFilters;
         }
      }

      return inverseFilters;
   }
}
