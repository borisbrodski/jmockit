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
package mockit.internal.state;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.expectations.*;
import mockit.internal.expectations.mocking.*;

public final class ExecutingTest
{
   private RecordAndReplayExecution currentRecordAndReplay;
   private RecordAndReplayExecution recordAndReplayForLastTestMethod;
   private boolean creatingNewRecordAndReplayInstance;

   private int mockParametersDeclared;
   private CaptureOfNewInstancesForParameters captureOfNewInstancesForParameters;

   private final List<Object> nonStrictMocks = new ArrayList<Object>();
   private final List<Object> strictMocks = new ArrayList<Object>();

   RecordAndReplayExecution getRecordAndReplay(boolean createIfUndefined)
   {
      if (currentRecordAndReplay == null && createIfUndefined) {
         setUpNewRecordAndReplay();
      }

      return currentRecordAndReplay;
   }

   private void setUpNewRecordAndReplay()
   {
      RecordAndReplayExecution previous = setRecordAndReplay(null);
      setRecordAndReplay(new RecordAndReplayExecution(previous));
   }

   public RecordAndReplayExecution setRecordAndReplay(RecordAndReplayExecution newRecordAndReplay)
   {
      recordAndReplayForLastTestMethod = null;
      RecordAndReplayExecution previous = currentRecordAndReplay;
      currentRecordAndReplay = newRecordAndReplay;
      creatingNewRecordAndReplayInstance = newRecordAndReplay == null;
      return previous;
   }

   public boolean isCreatingNewRecordAndReplayInstance()
   {
      return creatingNewRecordAndReplayInstance;
   }

   public RecordAndReplayExecution getRecordAndReplayForVerifications()
   {
      if (currentRecordAndReplay != null) {
         return currentRecordAndReplay;
      }
      else if (recordAndReplayForLastTestMethod != null) {
         return recordAndReplayForLastTestMethod;
      }
      else {
         // This should only happen if no expectation at all were created by the whole test, but
         // there is a (probably empty) verification block.
         setUpNewRecordAndReplay();
         return currentRecordAndReplay;
      }
   }

   public int getMockParametersDeclared()
   {
      return mockParametersDeclared;
   }

   public void setMockParametersDeclared(int mockParametersDeclared)
   {
      this.mockParametersDeclared = mockParametersDeclared;
   }

   public CaptureOfNewInstancesForParameters getCaptureOfNewInstancesForParameters()
   {
      return captureOfNewInstancesForParameters;
   }

   public void setCaptureOfNewInstancesForParameters(CaptureOfNewInstancesForParameters capture)
   {
      captureOfNewInstancesForParameters = capture;
   }

   public void addNonStrictMock(Class<?> mockedClass)
   {
      String mockedClassDesc = mockedClass.getName().replace('.', '/');
      String uniqueClassDesc = mockedClassDesc.intern();

      if (!containsNonStrictMock(null, uniqueClassDesc)) {
         nonStrictMocks.add(uniqueClassDesc);
      }
   }

   public void addNonStrictMock(Object mock)
   {
      nonStrictMocks.add(mock);

      if (!(mock instanceof Proxy)) {
         Class<?> mockedClass = mock.getClass();
         String mockedClassDesc = mockedClass.getName().replace('.', '/');
         nonStrictMocks.add(mockedClassDesc.intern());
      }
   }

   public void addNonStrictMocks(List<Object> mocks)
   {
      for (Object mock : mocks) {
         addNonStrictMock(mock);
      }
   }

   public void addStrictMock(Object mock, String mockClassDesc)
   {
      addStrictMock(mock);

      if (mockClassDesc != null) {
         String uniqueMockClassDesc = mockClassDesc.intern();

         if (!containsStrictMock(uniqueMockClassDesc)) {
            strictMocks.add(uniqueMockClassDesc);
         }
      }
   }

   private void addStrictMock(Object mock)
   {
      if (mock != null && !containsStrictMock(mock)) {
         strictMocks.add(mock);
      }
   }

   private boolean containsStrictMock(Object mockOrClass)
   {
      for (Object strictMock : strictMocks) {
         if (mockOrClass == strictMock) {
            return true;
         }
      }

      return false;
   }

   public boolean containsNonStrictMock(Object mock, String mockClassDesc)
   {
      for (Object nonStrictMock : nonStrictMocks) {
         if (nonStrictMock == mock || nonStrictMock == mockClassDesc) {
            return true;
         }
      }

      return false;
   }

   public boolean containsStrictMockForRunningTest(Object mock, String mockClassDesc)
   {
      for (Object strictMock : strictMocks) {
         if (strictMock == mock) {
            return true;
         }
         else if (strictMock == mockClassDesc) {
            addStrictMock(mock);
            return true;
         }
      }

      return false;
   }

   // TODO: this should probably be eliminated, since it can't work for pre-created instances
   public void substituteMock(Object previousInstance, Object newInstance)
   {
      for (Object strictMock : strictMocks) {
         if (strictMock == previousInstance) {
            strictMocks.add(newInstance);
            return;
         }
      }
   }

   public void clearNonStrictMocks()
   {
      nonStrictMocks.clear();
   }

   void finishExecution()
   {
      recordAndReplayForLastTestMethod = currentRecordAndReplay;
      currentRecordAndReplay = null;
      mockParametersDeclared = 0;

      if (captureOfNewInstancesForParameters != null) {
         captureOfNewInstancesForParameters.cleanUp();
         captureOfNewInstancesForParameters = null;
      }

      nonStrictMocks.clear();
      strictMocks.clear();
   }
}
