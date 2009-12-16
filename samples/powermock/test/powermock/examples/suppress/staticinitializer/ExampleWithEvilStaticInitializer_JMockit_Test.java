/*
 * JMockit Samples
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
package powermock.examples.suppress.staticinitializer;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.suppress.staticinitializer.ExampleWithEvilStaticInitializer_JMockit_Test.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/DocumentationExamples/src/test/java/powermock/examples/suppress/staticinitializer/ExampleWithEvilStaticInitializerTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(MockExampleWithStaticInitializer.class)
public final class ExampleWithEvilStaticInitializer_JMockit_Test
{
   @MockClass(realClass = ExampleWithEvilStaticInitializer.class, stubs = "<clinit>")
   static class MockExampleWithStaticInitializer {}

   @Test
   public void testSuppressStaticInitializer()
   {
      String message = "myMessage";
      ExampleWithEvilStaticInitializer tested = new ExampleWithEvilStaticInitializer(message);
      assertEquals(message, tested.getMessage());
   }
}
