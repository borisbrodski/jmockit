/*
 * JMockit Core/Annotations
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
package mockit;

import java.util.*;

import mockit.internal.util.*;

/**
 * Thrown to indicate an attempt to apply a given mock class to a real/production class when the
 * real class lacks a corresponding method or constructor for one of the mock methods/constructors
 * defined in the mock class.
 * This usually happens after a refactoring operation that changes the signature of the real 
 * method/constructor, so that the original mock isn't valid anymore.
 * <p/>
 * The intention is to alert the developer as quickly as possible about the need to correct the
 * original mock method/constructor, so that it again matches the proper real method/constructor.
 */
public final class RealMethodNotFoundForMockException extends IllegalArgumentException
{
   /**
    * Initializes the exception with a descriptive error message.
    * <p/>
    * This is to be called only from JMockit itself.
    *
    * @param mockClassName fully qualified name of the mock class
    * @param mockMethods internal JVM name + description for each of the mock methods/constructors
    * defined in the mock class
    */
   public RealMethodNotFoundForMockException(String mockClassName, Collection<String> mockMethods)
   {
      super(
         "Corresponding real methods not found for the following mocks:\n" +
         new MethodFormatter().friendlyMethodSignatures(
            getConstructorName(mockClassName), mockMethods));
   }

   private static String getConstructorName(String mockClassName)
   {
      int p = mockClassName.lastIndexOf('.');
      return p > 0 ? mockClassName.substring(p + 1) : mockClassName;
   }
}
