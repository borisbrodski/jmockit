/*
 * JMockit Expectations
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
import java.util.*;
import java.util.Map.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.invocation.*;
import mockit.internal.filtering.*;

public final class DynamicPartialMocking
{
   private static final List<MockFilter> exclusionFiltersForMockObject = new ArrayList<MockFilter>()
   {{
      add(new MockFilter()
      {
         public boolean matches(String name, String desc) { return "<init>".equals(name); }
      });
   }};

   private final Map<Class<?>, List<MockFilter>> classesAndMockFilters =
      new LinkedHashMap<Class<?>, List<MockFilter>>();
   private final Map<Class<?>, byte[]> modifiedClassfiles = new HashMap<Class<?>, byte[]>();
   private MockingConfiguration mockingCfg;

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

         if (targetClass.isInterface()) {
            throw new IllegalArgumentException(
               "Invalid interface type " + targetClass.getName() + " for partial mocking");
         }

         mockingCfg = null;
         redefineClass(targetClass);
      }
      else {
         mockingCfg = new MockingConfiguration(exclusionFiltersForMockObject, false);
         targetClass = classOrInstance.getClass();
         redefineClassAndItsSuperClasses(targetClass);
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
      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      modifiedClassfiles.put(realClass, modifiedClass);
      classesAndMockFilters.put(realClass, new ArrayList<MockFilter>());
   }

   public void addRecordedInvocation(ExpectedInvocation invocation)
   {
      String targetClassName = invocation.getClassName();

      for (Entry<Class<?>, List<MockFilter>> classAndFilters : classesAndMockFilters.entrySet()) {
         Class<?> targetClass = classAndFilters.getKey();

         if (targetClass.getName().equals(targetClassName)) {
            List<MockFilter> mockFilters = classAndFilters.getValue();
            mockFilters.add(new InternalDescMockFilter(invocation.getMethodNameAndDescription()));
            break;
         }
      }
   }

   public void restoreNonRecordedMethodsAndConstructors()
   {
      for (Entry<Class<?>, List<MockFilter>> classAndFilters : classesAndMockFilters.entrySet()) {
         Class<?> targetClass = classAndFilters.getKey();
         List<MockFilter> mockFilters = classAndFilters.getValue();

         mockingCfg = new MockingConfiguration(mockFilters, true);
         redefineClass(targetClass);
      }

      new RedefinitionEngine().redefineMethods(modifiedClassfiles);
      modifiedClassfiles.clear();
   }

   private static final class InternalDescMockFilter implements MockFilter
   {
      private final String mockNameAndDesc;

      InternalDescMockFilter(String mockNameAndDesc) { this.mockNameAndDesc = mockNameAndDesc; }

      public boolean matches(String name, String desc)
      {
         return mockNameAndDesc.equals(name + desc);
      }
   }
}
