/*
 * JMockit Annotations
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
package mockit.internal.annotations;

import java.util.*;

import mockit.internal.state.*;

/**
 * A container for the mock methods and constructors "collected" from a mock class, separated in two
 * sets: one with all the mock methods, and another with just the subset of static methods.
 */
public final class AnnotatedMockMethods
{
   String mockClassInternalName;
   private boolean isInnerMockClass;
   private boolean withItField;
   private boolean withInvocationParameter;

   /**
    * The set of public mock methods and constructors in a mock class. Each one is represented by
    * the concatenation of its name ("&lt;init>" in the case of a constructor) with the internal JVM
    * description of its parameters and return type.
    */
   final List<String> methods = new ArrayList<String>(20);

   /**
    * The subset of static methods between the {@link #methods mock methods} in a mock class. This
    * is needed when generating calls for the mock methods.
    */
   private final Collection<String> staticMethods = new ArrayList<String>(20);

   final Class<?> realClass;
   private MockClassState mockStates;
   private int indexForMockExpectations = -1;

   public AnnotatedMockMethods(Class<?> realClass)
   {
      this.realClass = realClass;
   }

   public String addMethod(boolean fromSuperclass, String name, String desc, boolean isStatic)
   {
      if (fromSuperclass && isMethodAlreadyAdded(name, desc)) {
         return null;
      }

      String nameAndDesc = name + desc;

      if (isStatic) {
         staticMethods.add(nameAndDesc);
      }

      methods.add(nameAndDesc);
      return nameAndDesc;
   }

   private boolean isMethodAlreadyAdded(String name, String desc)
   {
      int p = desc.lastIndexOf(')');
      String nameAndParams = name + desc.substring(0, p + 1);

      for (String method : methods) {
         if (method.startsWith(nameAndParams)) {
            return true;
         }
      }

      return false;
   }

   void addMockState(MockState mockState)
   {
      AnnotatedMockStates annotatedMockStates = TestRun.getMockClasses().getMockStates();

      if (mockStates == null) {
         mockStates = annotatedMockStates.addClassState(mockClassInternalName);
      }

      mockStates.addMockState(mockState);

      if (mockState.isWithExpectations()) {
         annotatedMockStates.registerMockStatesWithExpectations(mockState);
      }
   }

   public boolean containsMethod(String name, String desc)
   {
      boolean mockFound = hasMethod(name, desc);

      if (mockFound && mockStates != null) {
         indexForMockExpectations = mockStates.findMockState(name + desc);
      }

      return mockFound;
   }

   /**
    * Verifies if a mock method with the same signature of a given real method was previously
    * collected from the mock class. This operation can be performed only once for any given mock
    * method in this container, so that after the last real method is processed there should be no
    * mock methods left in the container.
    */
   private boolean hasMethod(String name, String desc)
   {
      withInvocationParameter = false;
      int n = name.length();

      for (int i = 0; i < methods.size(); i++) {
         String methodNameAndDesc = methods.get(i);

         if (methodNameAndDesc.startsWith(name) && methodNameAndDesc.charAt(n) == '(') {
            if (methodNameAndDesc.endsWith(desc)) {
               methods.remove(i);
               return true;
            }
            else if (
               methodNameAndDesc.contains("(Lmockit/Invocation;") &&
               methodNameAndDesc.substring(n + 20).endsWith(desc.substring(1))
            ) {
               withInvocationParameter = true;
               methods.remove(i);
               return true;
            }
         }
      }

      return false;
   }

   boolean isReentrant()
   {
      return indexForMockExpectations >= 0 && mockStates.getMockState(indexForMockExpectations).isReentrant();
   }

   public int getIndexForMockExpectations() { return indexForMockExpectations; }

   public boolean containsStaticMethod(String name, String desc)
   {
      return staticMethods.remove(name + desc);
   }

   public String getMockClassInternalName() { return mockClassInternalName; }
   public void setMockClassInternalName(String mockClassInternalName)
   {
      this.mockClassInternalName = mockClassInternalName;
   }

   public boolean isInnerMockClass() { return isInnerMockClass; }
   public void setInnerMockClass(boolean innerMockClass) { isInnerMockClass = innerMockClass; }

   public boolean isWithItField() { return withItField; }
   public void setWithItField(boolean withItField) { this.withItField = withItField; }

   public int getMethodCount() { return methods.size(); }
   public List<String> getMethods() { return methods; }

   public boolean isWithInvocationParameter() { return withInvocationParameter; }
}
