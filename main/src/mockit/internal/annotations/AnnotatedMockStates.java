/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

/**
 * Holds state associated with mock class containing {@linkplain mockit.Mock annotated mocks}.
 */
public final class AnnotatedMockStates
{
   /**
    * For each mock class containing @Mock annotations with at least one invocation expectation
    * specified or at least one reentrant mock, a runtime state will be kept here.
    */
   private final Map<String, MockClassState> classStates;

   /**
    * For each annotated mock method with at least one invocation expectation, its mock state will
    * also be kept here, as an optimization.
    */
   private final Set<MockState> mockStatesWithExpectations;

   public AnnotatedMockStates()
   {
      classStates = new HashMap<String, MockClassState>(8);
      mockStatesWithExpectations = new LinkedHashSet<MockState>(10);
   }

   MockClassState addClassState(String mockClassInternalName)
   {
      MockClassState mockStates = classStates.get(mockClassInternalName);

      if (mockStates == null) {
         mockStates = new MockClassState();
         classStates.put(mockClassInternalName, mockStates);
      }

      return mockStates;
   }

   public void removeClassState(Class<?> redefinedClass, String internalNameForOneOrMoreMockClasses)
   {
      removeMockStates(redefinedClass);

      if (internalNameForOneOrMoreMockClasses != null) {
         if (internalNameForOneOrMoreMockClasses.indexOf(' ') < 0) {
            removeMockStates(internalNameForOneOrMoreMockClasses);
         }
         else {
            String[] mockClassesInternalNames = internalNameForOneOrMoreMockClasses.split(" ");

            for (String mockClassInternalName : mockClassesInternalNames) {
               removeMockStates(mockClassInternalName);
            }
         }
      }
   }

   private void removeMockStates(Class<?> redefinedClass)
   {
      for (Iterator<Map.Entry<String, MockClassState>> itr = classStates.entrySet().iterator(); itr.hasNext(); ) {
         Map.Entry<String, MockClassState> mockClassAndItsState = itr.next();
         MockClassState mockClassState = mockClassAndItsState.getValue();
         MockState mockState = mockClassState.mockStates.get(0);

         if (mockState.getRealClass() == redefinedClass) {
            mockStatesWithExpectations.removeAll(mockClassState.mockStates);
            mockClassState.mockStates.clear();
            itr.remove();
         }
      }
   }

   private void removeMockStates(String mockClassInternalName)
   {
      MockClassState mockStates = classStates.remove(mockClassInternalName);

      if (mockStates != null) {
         mockStatesWithExpectations.removeAll(mockStates.mockStates);
      }
   }

   void registerMockStatesWithExpectations(MockState mockState)
   {
      mockStatesWithExpectations.add(mockState);
   }

   public boolean updateMockState(String mockClassName, int mockIndex)
   {
      MockState mockState = getMockState(mockClassName, mockIndex);

      if (mockState.isOnReentrantCall()) {
         return false;
      }

      mockState.update();
      return true;
   }

   MockState getMockState(String mockClassInternalName, int mockIndex)
   {
      return classStates.get(mockClassInternalName).getMockState(mockIndex);
   }

   public boolean hasStates(String mockClassInternalName)
   {
      return classStates.containsKey(mockClassInternalName);
   }

   public void exitReentrantMock(String mockClassInternalName, int mockIndex)
   {
      MockState mockState = getMockState(mockClassInternalName, mockIndex);
      mockState.exitReentrantCall();
   }

   public void verifyExpectations()
   {
      for (MockState mockState : mockStatesWithExpectations) {
         mockState.verifyExpectations();
      }
   }

   public void resetExpectations()
   {
      for (MockState mockState : mockStatesWithExpectations) {
         mockState.reset();
      }
   }
}
