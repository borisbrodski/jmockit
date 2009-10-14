/*
 * JMockit Core
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
package integrationTests.homepage;

import org.junit.runner.*;
import org.junit.*;
import mockit.integration.junit4.*;
import mockit.*;
import static junit.framework.Assert.assertNotNull;

@RunWith(JMockit.class)
public class JMockitVerificationsTest
{
   @Test
   public void testDoAnotherOperation(final AnotherDependency anotherMock)
   {
      new NonStrictExpectations()
      {
         DependencyXyz mock;

         {
            mock.doSomething("test"); returns(123);
         }
      };

      // ServiceAbc#doAnotherOperationAbc(String) calls "doSomething" on DependencyXyz, but also
      // calls "complexOperation" on AnotherDependency.
      new ServiceAbc().doAnotherOperation("test");

      new Verifications()
      {
         {
            anotherMock.complexOperation(withAny());
         }
      };
   }
}
