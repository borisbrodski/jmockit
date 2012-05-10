/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * A container for the mock methods "collected" from a mock class, separated in two sets: one with all the mock methods,
 * and another with just the subset of static methods.
 */
final class AnnotatedMockMethods
{
   private final Class<?> realClass;
   private final List<MockMethod> methods;
   private String mockClassInternalName;
   private boolean isInnerMockClass;
   private boolean withItField;
   private List<MockState> mockStates;
   boolean withMethodToSelectSubclasses;

   final class MockMethod
   {
      final String name;
      final String desc;
      final boolean isStatic;
      final boolean hasInvocationParameter;
      String mockedMethodDesc;
      private int indexForMockState;

      private MockMethod(String nameAndDesc, boolean isStatic)
      {
         int p = nameAndDesc.indexOf('(');
         name = nameAndDesc.substring(0, p);
         desc = nameAndDesc.substring(p);
         this.isStatic = isStatic;
         hasInvocationParameter = desc.startsWith("(Lmockit/Invocation;");
         indexForMockState = -1;
      }

      private boolean isMatch(String name, String desc)
      {
         if (this.name.equals(name)) {
            if (
               !hasInvocationParameter && this.desc.equals(desc) ||
               hasInvocationParameter && this.desc.substring(20).equals(desc.substring(1))
            ) {
               mockedMethodDesc = desc;
               return true;
            }
         }

         return false;
      }

      Class<?> getRealClass() { return realClass; }
      String getMockNameAndDesc() { return name + desc; }
      boolean isForConstructor() { return "$init".equals(name); }
      boolean hasMatchingRealMethod() { return mockedMethodDesc != null; }
      int getIndexForMockState() { return indexForMockState; }

      boolean isReentrant() { return indexForMockState >= 0 && mockStates.get(indexForMockState).isReentrant(); }
      boolean isDynamic() { return isReentrant() || hasInvocationParameter && isForConstructor(); }

      String errorMessage(String quantifier, int numExpectedInvocations, int timesInvoked)
      {
         String nameAndDesc = getMockNameAndDesc();
         return
            "Expected " + quantifier + ' ' + numExpectedInvocations + " invocation(s) of " +
            new MethodFormatter(mockClassInternalName, nameAndDesc) + ", but was invoked " + timesInvoked + " time(s)";
      }

      @Override
      public boolean equals(Object obj)
      {
         MockMethod other = (MockMethod) obj;
         return realClass == other.getRealClass() && name.equals(other.name) && desc.equals(other.desc);
      }

      @Override
      public int hashCode()
      {
         return 31 * (31 * realClass.hashCode() + name.hashCode()) + desc.hashCode();
      }
   }

   AnnotatedMockMethods(Class<?> realClass)
   {
      this.realClass = realClass;
      methods = new ArrayList<MockMethod>();
   }

   MockMethod addMethod(boolean fromSuperClass, String name, String desc, boolean isStatic)
   {
      if (fromSuperClass && isMethodAlreadyAdded(name, desc)) {
         return null;
      }

      String nameAndDesc = name + desc;
      MockMethod mockMethod = new MockMethod(nameAndDesc, isStatic);
      methods.add(mockMethod);
      return mockMethod;
   }

   private boolean isMethodAlreadyAdded(String name, String desc)
   {
      int p = desc.lastIndexOf(')');
      String params = desc.substring(0, p + 1);

      for (MockMethod mockMethod : methods) {
         if (mockMethod.name.equals(name) && mockMethod.desc.startsWith(params)) {
            return true;
         }
      }

      return false;
   }

   void addMockState(MockState mockState)
   {
      if (mockStates == null) {
         mockStates = new ArrayList<MockState>(4);
      }

      mockState.mockMethod.indexForMockState = mockStates.size();
      mockStates.add(mockState);
   }

   /**
    * Verifies if a mock method with the same signature of a given real method was previously collected from the mock
    * class.
    * This operation can be performed only once for any given mock method in this container, so that after the last real
    * method is processed there should be no mock methods left unused in the container.
    */
   MockMethod containsMethod(String name, String desc)
   {
      for (MockMethod mockMethod : methods) {
         if (mockMethod.isMatch(name, desc)) {
            return mockMethod;
         }
      }

      return null;
   }

   String getMockClassInternalName() { return mockClassInternalName; }
   void setMockClassInternalName(String mockClassInternalName) { this.mockClassInternalName = mockClassInternalName; }

   boolean isInnerMockClass() { return isInnerMockClass; }
   void setInnerMockClass(boolean innerMockClass) { isInnerMockClass = innerMockClass; }

   boolean supportsItField(Class<?> mockedClass) { return withItField && mockedClass == realClass; }
   void setWithItField(boolean withItField) { this.withItField = withItField; }

   boolean hasUnusedMocks()
   {
      for (MockMethod method : methods) {
         if (!method.hasMatchingRealMethod()) {
            return true;
         }
      }

      return false;
   }

   List<String> getUnusedMockSignatures()
   {
      List<String> signatures = new ArrayList<String>(methods.size());

      for (MockMethod mockMethod : methods) {
         if (!mockMethod.hasMatchingRealMethod()) {
            signatures.add(mockMethod.getMockNameAndDesc());
         }
      }

      return signatures;
   }

   void registerMockStates()
   {
      if (mockStates != null) {
         AnnotatedMockStates annotatedMockStates = TestRun.getMockClasses().getMockStates();
         annotatedMockStates.addMockClassAndStates(mockClassInternalName, mockStates);
      }
   }
}
