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
   private final Map<String, Object> cascadedTypesAndMocks;

   public MockedTypeCascade(boolean mockFieldFromTestClass)
   {
      this.mockFieldFromTestClass = mockFieldFromTestClass;
      cascadedTypesAndMocks = new HashMap<String, Object>(4);
   }

   static Object getMock(String mockedTypeDesc, Object mockInstance, String returnTypeDesc)
   {
      String returnTypeName = getReturnTypeNameIfCascadingSupportedForIt(returnTypeDesc);

      if (returnTypeName == null) {
         return null;
      }

      MockedTypeCascade cascade = TestRun.getExecutingTest().getMockedTypeCascade(mockedTypeDesc, mockInstance);

      return cascade == null ? null : cascade.getCascadedMock(returnTypeName);
   }

   private static String getReturnTypeNameIfCascadingSupportedForIt(String typeDesc)
   {
      String typeName = typeDesc.substring(1, typeDesc.length() - 1);

      if (typeName.startsWith("java/lang/") && !typeName.contains("/Process")) {
         return null;
      }
      
      return typeName;
   }

   private Object getCascadedMock(String returnTypeName)
   {
      Object mock = cascadedTypesAndMocks.get(returnTypeName);

      if (mock == null) {
         Class<?> returnType = Utilities.loadClass(returnTypeName.replace('/', '.'));

         mock = TestRun.mockFixture().getNewInstanceForMockedType(returnType);

         if (mock == null) {
            mock = new CascadingTypeRedefinition(returnType).redefineType();
         }

         cascadedTypesAndMocks.put(returnTypeName, mock);
         TestRun.getExecutingTest().addCascadingType(returnTypeName, false);
      }
      else {
         mock = TestRun.mockFixture().getNewInstanceForMockedType(mock.getClass());
      }

      TestRun.getExecutingTest().addInjectableMock(mock);
      return mock;
   }
}
