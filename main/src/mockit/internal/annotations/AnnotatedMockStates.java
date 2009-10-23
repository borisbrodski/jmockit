/*
 * JMockit Annotations
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
   private final Map<String, MockClassState> classStates = new HashMap<String, MockClassState>(8);

   /**
    * For each annotated mock method with at least one invocation expectation, its mock state will
    * also be kept here, as an optimization.
    */
   private final Set<MockState> mockStatesWithExpectations = new LinkedHashSet<MockState>(10);

   MockClassState addClassState(String mockClassInternalName)
   {
      MockClassState mockStates = classStates.get(mockClassInternalName);

      // TODO: Normally, this will only happen once, but it is possible to use the same mock class
      // in two or more calls to Mockit.redefineMethods or Mockit.setUpMock in the same test. Needs
      // to be tested.
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
      for (
         Iterator<Map.Entry<String, MockClassState>> itr = classStates.entrySet().iterator();
         itr.hasNext();
      ) {
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

   public void verifyExpectations()
   {
      for (MockState mockState : mockStatesWithExpectations) {
         mockState.verifyExpectations();
      }
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

   private MockState getMockState(String mockClassInternalName, int mockIndex)
   {
      return classStates.get(mockClassInternalName).getMockState(mockIndex);
   }

   public void exitReentrantMock(String mockClassInternalName, int mockIndex)
   {
      MockState mockState = getMockState(mockClassInternalName, mockIndex);
      mockState.exitReentrantCall();
   }
}
