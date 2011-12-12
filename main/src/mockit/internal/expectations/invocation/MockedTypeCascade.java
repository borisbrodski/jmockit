/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.*;

import mockit.internal.expectations.mocking.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockedTypeCascade
{
   public final boolean mockFieldFromTestClass;
   private final Map<String, Class<?>> cascadedTypesAndMocks;

   public MockedTypeCascade(boolean mockFieldFromTestClass)
   {
      this.mockFieldFromTestClass = mockFieldFromTestClass;
      cascadedTypesAndMocks = new HashMap<String, Class<?>>(4);
   }

   static Object getMock(String mockedTypeDesc, Object mockInstance, String returnTypeDesc)
   {
      String returnTypeInternalName = getReturnTypeIfCascadingSupportedForIt(returnTypeDesc);

      if (returnTypeInternalName == null) {
         return null;
      }

      MockedTypeCascade cascade = TestRun.getExecutingTest().getMockedTypeCascade(mockedTypeDesc, mockInstance);

      return cascade == null ? null : cascade.getCascadedMock(returnTypeInternalName);
   }

   private static String getReturnTypeIfCascadingSupportedForIt(String typeDesc)
   {
      String typeName = typeDesc.substring(1, typeDesc.length() - 1);

      if (typeName.startsWith("java/lang/") && !(typeName.contains("/Process") || typeName.endsWith("/Runnable"))) {
         return null;
      }
      
      return typeName;
   }

   private Object getCascadedMock(String returnTypeInternalName)
   {
      Class<?> returnType = cascadedTypesAndMocks.get(returnTypeInternalName);

      if (returnType == null) {
         returnType = registerIntermediateCascadingType(returnTypeInternalName);
      }

      return createNewCascadedInstanceOrUseNonCascadedOneIfAvailable(returnType);
   }

   private Class<?> registerIntermediateCascadingType(String returnTypeInternalName)
   {
      Class<?> returnType = Utilities.loadClassByInternalName(returnTypeInternalName);

      cascadedTypesAndMocks.put(returnTypeInternalName, returnType);
      TestRun.getExecutingTest().addCascadingType(returnTypeInternalName, false);
      return returnType;
   }

   private Object createNewCascadedInstanceOrUseNonCascadedOneIfAvailable(Class<?> mockedType)
   {
      InstanceFactory instanceFactory = TestRun.mockFixture().findInstanceFactory(mockedType);

      if (instanceFactory == null) {
         return new CascadingTypeRedefinition(mockedType).redefineType();
      }

      Object lastInstance = instanceFactory.getLastInstance();

      if (lastInstance != null) {
         return lastInstance;
      }

      ExecutingTest executingTest = TestRun.getExecutingTest();
      executingTest.setShouldIgnoreMockingCallbacks(true);
      Object cascadedInstance;

      try {
         cascadedInstance = instanceFactory.create();
      }
      finally {
         executingTest.setShouldIgnoreMockingCallbacks(false);
      }

      instanceFactory.clearLastInstance();
      executingTest.addInjectableMock(cascadedInstance);
      return cascadedInstance;
   }

   public void discardCascadedMocks()
   {
      cascadedTypesAndMocks.clear();
   }
}
