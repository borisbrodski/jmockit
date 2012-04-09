/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

final class MockClassState
{
   final List<MockState> mockStates = new ArrayList<MockState>(4);

   int findMockState(AnnotatedMockMethods.MockMethod mockMethod)
   {
      for (int i = 0; i < mockStates.size(); i++) {
         MockState mockState = mockStates.get(i);

         if (mockState.mockMethod == mockMethod) {
            return i;
         }
      }

      return -1;
   }

   int addMockState(MockState mockState)
   {
      mockStates.add(mockState);
      return mockStates.size() - 1;
   }

   MockState getMockState(int mockIndex) { return mockStates.get(mockIndex); }
}
