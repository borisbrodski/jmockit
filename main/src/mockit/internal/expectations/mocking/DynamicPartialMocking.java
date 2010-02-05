/*
 * JMockit Expectations
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
import java.util.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.util.*;

public final class DynamicPartialMocking
{
   private static final List<MockFilter> exclusionFiltersForMockObject = new ArrayList<MockFilter>()
   {{
      add(new MockFilter()
      {
         public boolean matches(String name, String desc) { return "<init>".equals(name); }
      });
   }};

   private final List<Class<?>> targetClasses;
   private final Map<Class<?>, byte[]> modifiedClassfiles;
   private MockingConfiguration mockingCfg;

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
         mockingCfg = null;
         redefineClass(targetClass);
      }
      else {
         targetClass = classOrInstance.getClass();
         validateTargetClassType(targetClass);
         mockingCfg = new MockingConfiguration(exclusionFiltersForMockObject, false);
         redefineClassAndItsSuperClasses(targetClass);
      }

      targetClasses.add(targetClass);
   }

   private void validateTargetClassType(Class<?> targetClass)
   {
      if (
         targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isArray() ||
         targetClass.isPrimitive() || Utilities.isWrapperOfPrimitiveType(targetClass)
      ) {
         throw new IllegalArgumentException("Invalid type for dynamic mocking: " + targetClass);
      }
   }

   private void redefineClassAndItsSuperClasses(Class<?> realClass)
   {
      redefineClass(realClass);
      Class<?> superClass = realClass.getSuperclass();

      if (superClass != Object.class && superClass != Proxy.class) {
         redefineClassAndItsSuperClasses(superClass);
      }
   }

   private void redefineClass(Class<?> realClass)
   {
      ClassReader classReader = new ClassFile(realClass, false).getReader();

      ExpectationsModifier modifier =
         new ExpectationsModifier(realClass.getClassLoader(), classReader, mockingCfg, null);
      modifier.enableExecutionOfRealImplementation();

      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      modifiedClassfiles.put(realClass, modifiedClass);
   }
}
