/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

import mockit.internal.state.*;

/**
 * A container for the mock methods "collected" from a mock class, separated in two sets: one with all the mock methods,
 * and another with just the subset of static methods.
 */
public final class AnnotatedMockMethods
{
   private String mockClassInternalName;
   private boolean isInnerMockClass;
   private boolean withItField;
   private boolean withInvocationParameter;

   /**
    * The set of public mock methods in a mock class. Each one is represented by the concatenation of its name with the
    * internal JVM description of its parameters and return type.
    */
   private final List<String> methods;

   /**
    * The subset of static methods between the {@link #methods mock methods} in a mock class.
    * This is needed when generating calls for the mock methods.
    */
   private final Collection<String> staticMethods;

   final Class<?> realClass;
   private MockClassState mockStates;
   private int indexForMockExpectations;

   public AnnotatedMockMethods(Class<?> realClass)
   {
      methods = new ArrayList<String>(20);
      staticMethods = new ArrayList<String>(20);
      this.realClass = realClass;
      indexForMockExpectations = -1;
   }

   public String addMethod(boolean fromSuperClass, String name, String desc, boolean isStatic)
   {
      if (fromSuperClass && isMethodAlreadyAdded(name, desc)) {
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
