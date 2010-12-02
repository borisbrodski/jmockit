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
package mockit.internal.expectations.invocation;

import java.util.*;

import mockit.internal.expectations.mocking.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class MockedTypeCascade
{
   private final Map<String, Object> cascadedTypesAndMocks = new HashMap<String, Object>(4);

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
         TestRun.getExecutingTest().addCascadingType(returnTypeName);
      }
      else {
         mock = TestRun.mockFixture().getNewInstanceForMockedType(mock.getClass());
      }

      TestRun.getExecutingTest().addInjectableMock(mock);
      return mock;
   }
}
