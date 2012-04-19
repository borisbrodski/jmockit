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
public final class AnnotatedMockMethods
{
   private final Class<?> realClass;
   private final List<MockMethod> methods;
   private String mockClassInternalName;
   private boolean isInnerMockClass;
   private boolean withItField;
   private List<MockState> mockStates;

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
            if (hasInvocationParameter) {
               if (this.desc.substring(20).equals(desc.substring(1))) {
                  return true;
               }
            }
            else if (this.desc.equals(desc)) {
               return true;
            }
         }

         return false;
      }

      Class<?> getRealClass() { return realClass; }
      String getMockNameAndDesc() { return name + desc; }
      boolean isForConstructor() { return "$init".equals(name); }
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
   }

   public AnnotatedMockMethods(Class<?> realClass)
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

   MockMethod containsMethod(String name, String desc)
   {
      MockMethod mockFound = hasMethod(name, desc);

      if (mockFound != null) {
         mockFound.mockedMethodDesc = desc;
      }

      return mockFound;
   }

   /**
    * Verifies if a mock method with the same signature of a given real method was previously collected from the mock
    * class.
    * This operation can be performed only once for any given mock method in this container, so that after the last real
    * method is processed there should be no mock methods left in the container.
    */
   private MockMethod hasMethod(String name, String desc)
   {
      for (int i = 0; i < methods.size(); i++) {
         MockMethod mockMethod = methods.get(i);

         if (mockMethod.isMatch(name, desc)) {
            methods.remove(i);
            return mockMethod;
         }
      }

      return null;
   }

   public String getMockClassInternalName() { return mockClassInternalName; }
   void setMockClassInternalName(String mockClassInternalName) { this.mockClassInternalName = mockClassInternalName; }

   boolean isInnerMockClass() { return isInnerMockClass; }
   void setInnerMockClass(boolean innerMockClass) { isInnerMockClass = innerMockClass; }

   boolean isWithItField() { return withItField; }
   void setWithItField(boolean withItField) { this.withItField = withItField; }

   public int getMethodCount() { return methods.size(); }

   public List<String> getMethods()
   {
      List<String> signatures = new ArrayList<String>(methods.size());

      for (MockMethod mockMethod : methods) {
         signatures.add(mockMethod.getMockNameAndDesc());
      }

      return signatures;
   }

   public void registerMockStates()
   {
      if (mockStates != null) {
         AnnotatedMockStates annotatedMockStates = TestRun.getMockClasses().getMockStates();
         annotatedMockStates.addMockClassAndStates(mockClassInternalName, mockStates);
      }
   }
}
