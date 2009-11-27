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

import java.util.*;

import org.junit.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

public class CoverageTest extends Assert
{
   static final Map<String, FileCoverageData> data = CoverageData.instance().getFileToFileDataMap();
   protected MethodCoverageData methodData;
   private int currentPathIndex;

   protected final MethodCoverageData getMethodData(Class<?> testedClass, String methodNameAndDesc)
   {
      String classFilePath = testedClass.getName().replace('.', '/') + ".java";
      FileCoverageData fileData = data.get(classFilePath);
      return fileData.methods.get(methodNameAndDesc);
   }

   protected final void getMethodData(Object testedInstance, String methodNameAndDesc)
   {
      methodData = getMethodData(testedInstance.getClass(), methodNameAndDesc);
   }

   protected final void assertPath(int expectedNodeCount, int expectedExecutionCount)
   {
      Path path = methodData.paths.get(currentPathIndex++);
      assertEquals("Path node count:", expectedNodeCount, path.getNodes().size());
      assertEquals("Path execution count:", expectedExecutionCount, path.getExecutionCount());
   }

   @After
   public final void verifyThatAllPathsWereAccountedFor()
   {
      if (methodData != null) {
         assertEquals(
            "Path " + currentPathIndex + " was not verified;",
            currentPathIndex, methodData.paths.size());
      }
   }
}