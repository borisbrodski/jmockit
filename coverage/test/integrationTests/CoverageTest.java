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
   private int currentPathIndex = -1;

   protected final void findMethodData(Class<?> testedClass, String methodName)
   {
      String classFilePath = testedClass.getName().replace('.', '/') + ".java";
      FileCoverageData fileData = data.get(classFilePath);

      for (Map.Entry<String, MethodCoverageData> nameAndData : fileData.methods.entrySet()) {
         if (nameAndData.getKey().startsWith(methodName)) {
            methodData = nameAndData.getValue();
            return;
         }
      }

      fail("No method with name \"" + methodName + "\" found in " + classFilePath);
   }

   protected final void findMethodData(Object testedInstance, String methodName)
   {
      findMethodData(testedInstance.getClass(), methodName);
   }

   protected final void assertMethodLines(int startingLine, int endingLine)
   {
      assertEquals(startingLine, methodData.getFirstLineOfImplementationBody());
      assertEquals(endingLine, methodData.getLastLineOfImplementationBody());
   }

   protected final void assertPath(int expectedNodeCount, int expectedExecutionCount)
   {
      int i = currentPathIndex + 1;
      currentPathIndex = -1;

      Path path = methodData.paths.get(i);
      assertEquals("Path node count:", expectedNodeCount, path.getNodes().size());
      assertEquals("Path execution count:", expectedExecutionCount, path.getExecutionCount());

      currentPathIndex = i;
   }

   @After
   public final void verifyThatAllPathsWereAccountedFor()
   {
      if (methodData != null && currentPathIndex >= 0) {
         assertEquals(
            "Path " + currentPathIndex + " was not verified;",
            currentPathIndex + 1, methodData.paths.size());
      }
   }
}