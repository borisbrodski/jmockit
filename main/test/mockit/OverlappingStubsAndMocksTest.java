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
package mockit;

import org.junit.*;

/**
 * Overlapping mocks/stubbing for the same real class is a misuse of the Annotations API.
 * The mocks/stubs applied at the level of the test class are set-up and restored only once, so
 * there can be no interference from individual tests.
 */
@UsingMocksAndStubs(RealClass.class)
public final class OverlappingStubsAndMocksTest
{
   @MockClass(realClass = RealClass.class)
   static final class TheMockClass
   {
      @Mock(invocations = 1)
      void doSomething() {}
   }

   @Test
   public void firstTest()
   {
      Mockit.setUpMocks(TheMockClass.class);
      RealClass.doSomething();
   }

   @Test(expected = AssertionError.class)
   public void secondTest()
   {
      Mockit.setUpMocks(TheMockClass.class);
      RealClass.doSomething();
      // Fails with an unexpected final invocation count, caused by duplicate internal state for the
      // "doSomething" mock. The duplication, in turn, is caused by "RealClass" not being restored
      // after the first test, since it was stubbed out for the whole test class.
   }
}

final class RealClass
{
   static void doSomething() {}
}

