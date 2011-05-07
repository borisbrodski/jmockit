/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.util.*;

import mockit.internal.expectations.*;
import mockit.internal.expectations.invocation.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.util.*;

@SuppressWarnings({"ClassWithTooManyFields"})
public final class ExecutingTest
{
   private RecordAndReplayExecution currentRecordAndReplay;
   private RecordAndReplayExecution recordAndReplayForLastTestMethod;
   private boolean shouldIgnoreMockingCallbacks;

   private ParameterTypeRedefinitions parameterTypeRedefinitions;

   private final Map<MockedType, Object> finalLocalMockFields = new HashMap<MockedType, Object>(4);
   private final List<Object> injectableMocks = new ArrayList<Object>();
   private final Map<Object, Object> originalToCapturedInstance = new IdentityHashMap<Object, Object>(4);
   private final List<Object> nonStrictMocks = new ArrayList<Object>();
   private final List<Object> strictMocks = new ArrayList<Object>();

   private final Map<String, MockedTypeCascade> cascadingTypes = new HashMap<String, MockedTypeCascade>(4);
   public final DefaultResults defaultResults = new DefaultResults();

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

   public boolean isShouldIgnoreMockingCallbacks() { return shouldIgnoreMockingCallbacks; }
   public void setShouldIgnoreMockingCallbacks(boolean flag) { shouldIgnoreMockingCallbacks = flag; }

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

   public ParameterTypeRedefinitions getParameterTypeRedefinitions() { return parameterTypeRedefinitions; }
   public void setParameterTypeRedefinitions(ParameterTypeRedefinitions redefinitions)
   { parameterTypeRedefinitions = redefinitions; }

   public void clearInjectableAndNonStrictMocks()
   {
      injectableMocks.clear();
      clearNonStrictMocks();
      originalToCapturedInstance.clear();
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
      addCapturedInstance(originalInstance, capturedInstance);
   }

   public void addCapturedInstance(Object originalInstance, Object capturedInstance)
   {
      originalToCapturedInstance.put(capturedInstance, originalInstance);
   }

   public boolean isInvokedInstanceEquivalentToCapturedInstance(Object invokedInstance, Object capturedInstance)
   {
      return
         invokedInstance == originalToCapturedInstance.get(capturedInstance) ||
         capturedInstance == originalToCapturedInstance.get(invokedInstance);
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

   public boolean isNonStrictInvocation(Object mock, String mockClassDesc, String mockNameAndDesc)
   {
      boolean instanceMethod = isInstanceMethod(mock, mockNameAndDesc);

      if (instanceMethod && isOverrideOfObjectMethod(mockNameAndDesc)) {
         return true;
      }

      for (Object nonStrictMock : nonStrictMocks) {
         if (!instanceMethod) {
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

   private boolean isInstanceMethod(Object mock, String mockNameAndDesc)
   {
      return mock != null && mockNameAndDesc.charAt(0) != '<';
   }

   private boolean isOverrideOfObjectMethod(String mockNameAndDesc)
   {
      return "equals(Ljava/lang/Object;)Z hashCode()I toString()Ljava/lang/String;".contains(mockNameAndDesc);
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

   public boolean isStrictInvocation(Object mock, String mockClassDesc, String mockNameAndDesc)
   {
      if (isInstanceMethod(mock, mockNameAndDesc) && isOverrideOfObjectMethod(mockNameAndDesc)) {
         return false;
      }

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

   public void addCascadingType(String mockedTypeDesc, boolean mockFieldFromTestClass)
   {
      if (!cascadingTypes.containsKey(mockedTypeDesc)) {
         cascadingTypes.put(mockedTypeDesc, new MockedTypeCascade(mockFieldFromTestClass));
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
      clearNonSharedCascadingTypes();
      defaultResults.clear();
   }

   private void clearNonSharedCascadingTypes()
   {
      Iterator<MockedTypeCascade> itr = cascadingTypes.values().iterator();

      while (itr.hasNext()) {
         MockedTypeCascade cascade = itr.next();

         if (!cascade.mockFieldFromTestClass) {
            itr.remove();
         }
      }
   }
}
