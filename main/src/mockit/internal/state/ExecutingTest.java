/*
 * JMockit Expectations & Verifications
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

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.expectations.*;
import mockit.internal.expectations.invocation.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.util.*;

public final class ExecutingTest
{
   private RecordAndReplayExecution currentRecordAndReplay;
   private RecordAndReplayExecution recordAndReplayForLastTestMethod;
   private boolean shouldIgnoreMockingCallbacks;

   private ParameterTypeRedefinitions parameterTypeRedefinitions;

   private final Map<MockedType, Object> finalLocalMockFields = new HashMap<MockedType, Object>(4);
   private final List<Object> injectableMocks = new ArrayList<Object>();
   private final Map<Object, Object> originalToCapturedInjectableMocks = new IdentityHashMap<Object, Object>(4);
   private final List<Object> nonStrictMocks = new ArrayList<Object>();
   private final List<Object> strictMocks = new ArrayList<Object>();

   private final Map<String, MockedTypeCascade> cascadingTypes = new HashMap<String, MockedTypeCascade>(4);

   RecordAndReplayExecution getRecordAndReplay(boolean createIfUndefined)
   {
      if (currentRecordAndReplay == null && createIfUndefined) {
         setRecordAndReplay(new RecordAndReplayExecution());
      }

      return currentRecordAndReplay;
   }

   public RecordAndReplayExecution getRecordAndReplay()
   {
      recordAndReplayForLastTestMethod = null;
      RecordAndReplayExecution previous = currentRecordAndReplay;
      currentRecordAndReplay = null;
      return previous;
   }

   public void setRecordAndReplay(RecordAndReplayExecution newRecordAndReplay)
   {
      recordAndReplayForLastTestMethod = null;
      currentRecordAndReplay = newRecordAndReplay;
   }

   public boolean isShouldIgnoreMockingCallbacks()
   {
      return shouldIgnoreMockingCallbacks;
   }

   public void setShouldIgnoreMockingCallbacks(boolean flag)
   {
      shouldIgnoreMockingCallbacks = flag;
   }

   public void clearRecordAndReplayForVerifications()
   {
      recordAndReplayForLastTestMethod = null;
   }

   public RecordAndReplayExecution getRecordAndReplayForVerifications()
   {
      if (currentRecordAndReplay == null) {
         if (recordAndReplayForLastTestMethod != null) {
            currentRecordAndReplay = recordAndReplayForLastTestMethod;
         }
         else {
            // This should only happen if no expectations at all were created by the whole test, but
            // there is one (probably empty) verification block.
            currentRecordAndReplay = new RecordAndReplayExecution();
         }
      }

      return currentRecordAndReplay;
   }

   public ParameterTypeRedefinitions getParameterTypeRedefinitions()
   {
      return parameterTypeRedefinitions;
   }

   public void setParameterTypeRedefinitions(ParameterTypeRedefinitions redefinitions)
   {
      parameterTypeRedefinitions = redefinitions;
   }

   public void clearInjectableMocks()
   {
      injectableMocks.clear();
      originalToCapturedInjectableMocks.clear();
   }

   public void addInjectableMock(Object mock)
   {
      if (!isInjectableMock(mock)) {
         injectableMocks.add(mock);
      }
   }

   public boolean isInjectableMock(Object mock)
   {
      for (Object injectableMock : injectableMocks) {
         if (mock == injectableMock) {
            return true;
         }
      }

      return false;
   }

   public void addCapturedInstanceForInjectableMock(Object originalInstance, Object capturedInstance)
   {
      injectableMocks.add(capturedInstance);
      originalToCapturedInjectableMocks.put(capturedInstance, originalInstance);
   }

   public boolean isInjectableInstanceEquivalentToCapturedInstance(Object invokedInstance, Object capturedInstance)
   {
      return
         invokedInstance == originalToCapturedInjectableMocks.get(capturedInstance) ||
         capturedInstance == originalToCapturedInjectableMocks.get(invokedInstance);
   }

   public void discardCascadedMockWhenInjectable(Object oldMock)
   {
      for (int i = 0, n = injectableMocks.size(); i < n; i++) {
         if (injectableMocks.get(i) == oldMock) {
            injectableMocks.remove(i);
            return;
         }
      }
   }

   public void addNonStrictMock(Class<?> mockedClass)
   {
      String mockedClassDesc = mockedClass.getName().replace('.', '/');
      String uniqueClassDesc = mockedClassDesc.intern();

      if (!containsNonStrictMockedClass(uniqueClassDesc)) {
         nonStrictMocks.add(uniqueClassDesc);
      }
   }

   private boolean containsNonStrictMockedClass(Object mockOrClassDesc)
   {
      for (Object nonStrictMock : nonStrictMocks) {
         if (mockOrClassDesc == nonStrictMock) {
            return true;
         }
      }

      return false;
   }

   public void addNonStrictMock(Object mock)
   {
      if (!containsNonStrictMockedClass(mock)) {
         nonStrictMocks.add(mock);
      }

      addNonStrictMock(mock.getClass());
   }

   public void addFinalLocalMockField(Object owner, MockedType typeMetadata)
   {
      finalLocalMockFields.put(typeMetadata, owner);
   }

   public void addStrictMock(Object mock, String mockClassDesc)
   {
      addStrictMock(mock);

      if (mockClassDesc != null) {
         String uniqueMockClassDesc = mockClassDesc.intern();

         if (!containsStrictMock(uniqueMockClassDesc) && !containsNonStrictMockedClass(uniqueMockClassDesc)) {
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

   public boolean containsNonStrictMock(int access, Object mock, String mockClassDesc, String mockNameAndDesc)
   {
      boolean staticMethod = Modifier.isStatic(access);
      boolean notInstanceMethod = staticMethod || mockNameAndDesc.startsWith("<init>");

      for (Object nonStrictMock : nonStrictMocks) {
         if (notInstanceMethod) {
            if (nonStrictMock == mockClassDesc) {
               return true;
            }
         }
         else if (nonStrictMock == mock) {
            return true;
         }
      }

      return false;
   }

   public void registerAdditionalMocksFromFinalLocalMockFieldsIfAny()
   {
      if (!finalLocalMockFields.isEmpty()) {
         for (
            Iterator<Map.Entry<MockedType, Object>> itr = finalLocalMockFields.entrySet().iterator();
            itr.hasNext();
         ) {
            Map.Entry<MockedType, Object> fieldAndOwner = itr.next();
            MockedType typeMetadata = fieldAndOwner.getKey();
            Object mock = Utilities.getFieldValue(typeMetadata.field, fieldAndOwner.getValue());

            // A null field value will occur for invocations executed during initialization of the owner instance.
            if (mock != null) {
               registerMock(typeMetadata, mock);
               itr.remove();
            }
         }
      }
   }

   public void registerMock(MockedType typeMetadata, Object mock)
   {
      if (typeMetadata.injectable) {
         addInjectableMock(mock);
      }

      if (typeMetadata.nonStrict) {
         addNonStrictMock(mock);
      }
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

   public void clearNonStrictMocks()
   {
      finalLocalMockFields.clear();
      nonStrictMocks.clear();
   }

   public Map<String, MockedTypeCascade> getCascadingMockedTypes()
   {
      return cascadingTypes;
   }

   public void addCascadingType(String mockedTypeDesc)
   {
      if (!cascadingTypes.containsKey(mockedTypeDesc)) {
         cascadingTypes.put(mockedTypeDesc, new MockedTypeCascade());
      }
   }
   
   public MockedTypeCascade getMockedTypeCascade(String mockedTypeDesc, Object mockInstance)
   {
      if (cascadingTypes.isEmpty()) {
         return null;
      }

      MockedTypeCascade cascade = cascadingTypes.get(mockedTypeDesc);

      if (cascade != null || mockInstance == null) {
         return cascade;
      }

      return getMockedTypeCascade(mockedTypeDesc, mockInstance.getClass());
   }

   private MockedTypeCascade getMockedTypeCascade(String invokedTypeDesc, Class<?> mockedType)
   {
      Class<?> typeToLookFor = mockedType;

      do {
         String typeDesc = typeToLookFor.getName().replace('.', '/');

         if (invokedTypeDesc.equals(typeDesc)) {
            return null;
         }

         MockedTypeCascade cascade = cascadingTypes.get(typeDesc);

         if (cascade != null) {
            return cascade;
         }

         typeToLookFor = typeToLookFor.getSuperclass();
      }
      while (typeToLookFor != Object.class);
      
      return null;
   }

   void finishExecution()
   {
      recordAndReplayForLastTestMethod = currentRecordAndReplay;
      currentRecordAndReplay = null;

      if (parameterTypeRedefinitions != null) {
         parameterTypeRedefinitions.cleanUp();
         parameterTypeRedefinitions = null;
      }

      clearNonStrictMocks();
      strictMocks.clear();
      cascadingTypes.clear();
   }
}
