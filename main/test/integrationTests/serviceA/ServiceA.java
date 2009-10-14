/*
 * JMockit: a Java class library for developer testing with "mock methods"
 * Copyright (c) 2006, 2007 Rogerio Liesenfeld
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
package integrationTests.serviceA;

import java.util.*;

import integrationTests.serviceB.*;

public final class ServiceA
{
   private String config;

   public String getConfig()
   {
      return config;
   }

   public boolean doSomethingThatUsesServiceB(int a, String b)
   {
      int x = new ServiceB(b).computeX(a, 5);

      return x > a;
   }

   public void doSomethingElseUsingServiceB(int noOfCallsToServiceB)
   {
      ServiceB serviceB = new ServiceB("config");

      for (int i = 0; i < noOfCallsToServiceB; i++) {
         serviceB.computeX(i, 1);
      }

      config = serviceB.getConfig();
   }

   public String performComplexOperation(List<?> items)
   {
      ServiceB serviceB = new ServiceB(config);
      int i = 1;

      for (Object item : items) {
         serviceB.computeX(i, item.hashCode());
      }

      return serviceB.findItem("ABC", "xyz", "01");
   }
}
