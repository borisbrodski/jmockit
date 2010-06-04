/*
 * JMockit Annotations
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

import static org.junit.Assert.*;

public final class JMockitAnnotationsExampleTest
{
   @Test
   public void testDoOperationAbc()
   {
      Mockit.setUpMocks(MockDependencyXyz.class);

      // In ServiceAbc#doOperationAbc(String s): "new DependencyXyz().doSomething(s);"
      Object result = new ServiceAbc().doOperationAbc("test");

      assertNotNull(result);
   }

   @MockClass(realClass = DependencyXyz.class)
   public static class MockDependencyXyz
   {
      @Mock(invocations = 1)
      public int doSomething(String value)
      {
         assertEquals("test", value);
         return 123;
      }
   }

   @Test // same as the previous test, but using an "in-line" (anonymous) mock class
   public void testDoOperationAbc_inlineVersion()
   {
      new MockUp<DependencyXyz>()
      {
         @Mock(invocations = 1)
         int doSomething(String value)
         {
            assertEquals("test", value);
            return 123;
         }
      };

      Object result = new ServiceAbc().doOperationAbc("test");

      assertNotNull(result);
   }
}
