/*
 * JMockit
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
      if (capturing == null) { // occurs when using @Mocked#capture
         this.baseType = baseType;
         classNameFilters = null;
         inverseFilters = false;
      }
      else {
         classNameFilters = capturing.classNames();
         inverseFilters = capturing.inverse();

         Class<?> specifiedBaseType = capturing.baseType();

         if (specifiedBaseType == Void.class) {
            if (baseType == null) {
               throw new IllegalArgumentException(
                  "Missing base type in @Capturing annotation at class level");
            }

            this.baseType = baseType;
         }
         else {
            this.baseType = specifiedBaseType;
         }
      }
   }

   boolean isToBeCaptured(Class<?> aClass)
   {
      return
         aClass != baseType && !Proxy.isProxyClass(aClass) && baseType.isAssignableFrom(aClass) &&
         isToBeCaptured(aClass.getName());
   }
   
   boolean isToBeCaptured(String className)
   {
      if (Utilities.isGeneratedSubclass(className)) {
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
