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

import java.lang.reflect.*;
import java.util.*;

import org.junit.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

public class CoverageTest extends Assert
{
   static final Map<String, FileCoverageData> data = CoverageData.instance().getFileToFileDataMap();
   protected FileCoverageData fileData;
   protected MethodCoverageData methodData;
   private int currentPathIndex = -1;

   @Before
   public void findCoverageData() throws Exception
   {
      Field testedField = getClass().getDeclaredField("tested");
      Class<?> testedClass = testedField.getType();

      String classFilePath = testedClass.getName().replace('.', '/') + ".java";
      fileData = data.get(classFilePath);
      assertNotNull("FileCoverageData not found for " + classFilePath);
   }

   protected final void assertLines(int startingLine, int endingLine, int expectedLinesExecuted)
   {
      SortedMap<Integer, LineCoverageData> lineToLineData = fileData.lineToLineData;
      assertTrue("Starting line not found", lineToLineData.containsKey(startingLine));
      assertTrue("Ending line not found", lineToLineData.containsKey(endingLine));

      int linesExecuted = 0;

      for (int line = startingLine; line <= endingLine; line++) {
         LineCoverageData lineData = lineToLineData.get(line);

         if (lineData != null && lineData.getExecutionCount() > 0) {
            linesExecuted++;
         }
      }

      assertEquals("Unexpected number of lines executed:", expectedLinesExecuted, linesExecuted);
   }

   protected final void assertLine(
      int line, int expectedSegments, int expectedCoveredSegments, int expectedExecutionCount)
   {
      LineCoverageData lineData = fileData.lineToLineData.get(line);
      assertEquals("Segments:", expectedSegments, lineData.getNumberOfSegments());
      assertEquals("Covered segments:", expectedCoveredSegments, lineData.getNumberOfCoveredSegments());
      assertEquals("Execution count:", expectedExecutionCount, lineData.getExecutionCount());
   }

   protected final void findMethodData(int firstLineOfMethodBody, String methodName)
   {
      methodData = fileData.firstLineToMethodData.get(firstLineOfMethodBody);
      assertNotNull("Method not found with first line " + firstLineOfMethodBody, methodData);
      assertEquals(
         "No method with name \"" + methodName + "\" found with first line at " +
         firstLineOfMethodBody, methodName, methodData.methodName);
   }

   protected final void assertPaths(
      int expectedPaths, int expectedCoveredPaths, int expectedExecutionCount)
   {
      assertEquals("Number of paths:", expectedPaths, methodData.paths.size());
      assertEquals("Number of covered paths:", expectedCoveredPaths, methodData.getCoveredPaths());
      assertEquals(
         "Execution count for all paths:", expectedExecutionCount, methodData.getExecutionCount());
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
      int nextPathIndex = currentPathIndex + 1;

      if (methodData != null && nextPathIndex > 0) {
         assertEquals(
            "Path " + nextPathIndex + " was not verified;", nextPathIndex, methodData.paths.size());
      }
   }
}