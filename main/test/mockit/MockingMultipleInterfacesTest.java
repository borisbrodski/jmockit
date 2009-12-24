/*
 * JMockit Expectations & Verifications
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

import java.io.*;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.MockingMultipleInterfacesTest.Dependency;

public final class MockingMultipleInterfacesTest<MultiMock extends Dependency & Runnable>
{
   interface Dependency
   {
      String doSomething(boolean b);
   }

   @Mocked MultiMock multiMock;

   @Test
   public void mockFieldWithTwoInterfaces()
   {
      new NonStrictExpectations()
      {
         {
            multiMock.doSomething(false); returns("test");
         }
      };

      multiMock.run();
      assertEquals("test", multiMock.doSomething(false));

      new Verifications()
      {
         {
            multiMock.run();
         }
      };
   }

   @Test
   public <M extends Dependency & Serializable> void mockParameterWithTwoInterfaces(final M mock)
   {
      new Expectations()
      {
         {
            mock.doSomething(true); returns("");
         }
      };

      assertEquals("", mock.doSomething(true));
   }
}