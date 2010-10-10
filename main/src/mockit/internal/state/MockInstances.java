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
package mockit.internal.state;

import java.util.*;

import mockit.internal.util.*;

/**
 * Holds a list of instances of mock classes (either regular classes provided by client code, or
 * startup mock classes provided internally by JMockit or by external jars).
 * <p/>
 * This is needed to allow each redefined real method to call the corresponding mock method on the
 * single global instance for the mock class.
 */
public final class MockInstances
{
   private final List<Object> mocks = new ArrayList<Object>();
   private final Map<Object, Object> mockedInstancesToMocks = new HashMap<Object, Object>();

   public boolean containsInstance(Object mock)
   {
      return mocks.contains(mock);
   }

   public int getInstanceCount()
   {
      return mocks.size();
   }

   public Object getMock(int index)
   {
      return mocks.get(index);
   }

   public Object getMock(Class<?> mockClass, Object mockedInstance)
   {
      Object mock = mockedInstancesToMocks.get(mockedInstance);

      if (mock == null) {
         mock = Utilities.newInstance(mockClass);
         mockedInstancesToMocks.put(mockedInstance, mock);
      }

      return mock;
   }

   public int addMock(Object mock)
   {
      mocks.add(mock);
      return mocks.size() - 1;
   }

   void removeInstances(int fromIndex)
   {
      for (int i = mocks.size() - 1; i >= fromIndex; i--) {
         mocks.remove(i);
      }
   }

   public void discardInstances()
   {
      mocks.clear();
   }
}
