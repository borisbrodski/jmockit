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
      String typeInternalName = typeDesc.substring(1, typeDesc.length() - 1);

      if (typeInternalName.startsWith("java/lang/") && !typeInternalName.contains("/Process")) {
         return null;
      }
      
      return typeInternalName;
   }

   private Object getCascadedMock(String returnTypeInternalName)
   {
      Class<?> returnType = cascadedTypesAndMocks.get(returnTypeInternalName);

      if (returnType == null) {
         String className = returnTypeInternalName.replace('/', '.');
         returnType = Utilities.loadClass(className);

         cascadedTypesAndMocks.put(returnTypeInternalName, returnType);
         TestRun.getExecutingTest().addCascadingType(returnTypeInternalName, false);
      }

      Object mock = TestRun.mockFixture().getNewInstanceForMockedType(returnType);

      if (mock == null) {
         mock = new CascadingTypeRedefinition(returnType).redefineType();
      }

      TestRun.getExecutingTest().addInjectableMock(mock);
      return mock;
   }
}
