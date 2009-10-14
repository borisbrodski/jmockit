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

import mockit.*;
import mockit.integration.junit4.*;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public class JMockitExpectationsExampleTest
{
   // Mock fields can be declared here too.

   @Test
   public void testDoOperationAbc()
   {
      new Expectations()
      {
         // This is a mock field; it can optionally be annotated with @Mocked.
         final DependencyXyz mock = new DependencyXyz();

         {
            mock.doSomething("test"); returns(123);
         }
      };

      // ServiceAbc#doOperationAbc(String) instantiates DependencyXyz and calls "doSomething" on it
      // with the same argument.
      Object result = new ServiceAbc().doOperationAbc("test");

      assertNotNull(result);

      // That all expectations recorded were actually executed in the replay phase is automatically
      // verified at this point, through transparent integration with the JUnit test runner.
   }
}
