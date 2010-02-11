/*
 * JMockit Coverage
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
package integrationTests;

import org.junit.*;

public final class MultiThreadedCodeTest extends CoverageTest
{
   MultiThreadedCode tested;

   @Test
   public void nonBlockingOperation() throws Exception
   {
      Thread worker = tested.nonBlockingOperation();
      worker.join();

      assertLines(7, 17, 7);
      assertLine(7, 1, 1, 1);
      assertLine(11, 1, 1, 1);
      assertLine(13, 1, 1, 1);
      assertLine(16, 1, 1, 1);
      assertLine(17, 1, 1, 1);

      findMethodData(7, "nonBlockingOperation");
      assertPaths(1, 1, 1);
      assertMethodLines(7, 17);
      assertPath(2, 1);
   }
}