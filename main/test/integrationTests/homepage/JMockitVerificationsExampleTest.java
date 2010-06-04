/*
 * JMockit Verifications
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
package integrationTests.homepage;

import org.junit.*;

import mockit.*;

public final class JMockitVerificationsExampleTest
{
   @Test // notice the "mock parameter", whose argument value will be created automatically
   public void testDoAnotherOperation(final AnotherDependency anotherMock)
   {
      new NonStrictExpectations()
      {
         DependencyXyz mock; // mock instance created and assigned automatically

         {
            mock.doSomething("test"); returns(123);
         }
      };

      // In ServiceAbc#doAnotherOperationAbc(String s): "new DependencyXyz().doSomething(s);"
      // and "new AnotherDependency().complexOperation(obj);".
      new ServiceAbc().doAnotherOperation("test");

      new Verifications()
      {
         {
            anotherMock.complexOperation(anyInt, null);
         }
      };
   }
}
