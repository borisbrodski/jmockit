/*
 * JMockit Coverage
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
package integrationTests;

import org.junit.*;

public final class AbstractClassTest extends CoverageTest
{
   AbstractClassWithNoExecutableLines tested;

   @Before
   public void setUp()
   {
      tested = new AbstractClassWithNoExecutableLines()
      {
         @Override void doSomething(String s, boolean b) {}
         @Override int returnValue() { return 0; }
      };
   }

   @Test
   public void useAbstractClass()
   {
      tested.doSomething("test", true);
      tested.returnValue();

      assertEquals(1, fileData.lineToLineData.size());
      assertLines(3, 3, 1);
      assertEquals(100, fileData.getCodeCoveragePercentage());

      assertEquals(1, fileData.firstLineToMethodData.size());
      findMethodData(3, AbstractClassWithNoExecutableLines.class.getSimpleName());
      assertMethodLines(3, 3);
      assertPaths(1, 1, 1);
      assertPath(2, 1);
      assertEquals(100, fileData.getPathCoveragePercentage());
   }
}