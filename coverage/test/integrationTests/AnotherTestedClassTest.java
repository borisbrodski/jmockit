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

import static org.junit.Assert.*;

public final class AnotherTestedClassTest
{
   static final Map<String, FileCoverageData> data = CoverageData.instance().getFileToFileDataMap();
   MethodCoverageData methodData;
   int currentPathIndex;

   AnotherTestedClass tested;

   private MethodCoverageData getMethodData(Class<?> testedClass, String methodNameAndDesc)
   {
      String classFilePath = testedClass.getName().replace('.', '/') + ".java";
      FileCoverageData fileData = data.get(classFilePath);
      return fileData.methods.get(methodNameAndDesc);
   }

   private void getMethodData(Object testedInstance, String methodNameAndDesc)
   {
      methodData = getMethodData(testedInstance.getClass(), methodNameAndDesc);
   }

   private void assertPath(int expectedNodeCount, int expectedExecutionCount)
   {
      Path path = methodData.paths.get(currentPathIndex++);
      assertEquals("Path node count:", expectedNodeCount, path.getNodes().size());
      assertEquals("Path execution count:", expectedExecutionCount, path.getExecutionCount());
   }

   @After
   public void verifyThatAllPathsWereAccountedFor()
   {
      if (methodData != null) {
         assertEquals(
            "Path " + currentPathIndex + " was not verified;",
            currentPathIndex, methodData.paths.size());
      }
   }

   @Before
   public void setUp()
   {
      tested = new AnotherTestedClass();
   }

   @Test
   public void simpleIf()
   {
      tested.simpleIf(true);
      tested.simpleIf(false);

      getMethodData(tested, "simpleIf(Z)V");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(8, methodData.getFirstLineOfImplementationBody());
      assertEquals(11, methodData.getLastLineOfImplementationBody());

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void ifAndElse()
   {
      tested.ifAndElse(true);
      tested.ifAndElse(false);

      getMethodData(tested, "ifAndElse(Z)V");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(15, methodData.getFirstLineOfImplementationBody());
      assertEquals(21, methodData.getLastLineOfImplementationBody());

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void singleLineIf()
   {
      tested.singleLineIf(true);
      tested.singleLineIf(false);

      getMethodData(tested, "singleLineIf(Z)V");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(25, methodData.getFirstLineOfImplementationBody());
      assertEquals(26, methodData.getLastLineOfImplementationBody());

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test
   public void singleLineIfAndElse()
   {
      tested.singleLineIfAndElse(true);
      tested.singleLineIfAndElse(false);

      getMethodData(tested, "singleLineIfAndElse(Z)V");
      assertEquals(2, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(30, methodData.getFirstLineOfImplementationBody());
      assertEquals(31, methodData.getLastLineOfImplementationBody());

      assertPath(3, 1);
      assertPath(3, 1);
   }

   @Test(expected = AssertionError.class)
   public void nonBranchingMethodWithUnreachableLines()
   {
      tested.nonBranchingMethodWithUnreachableLines();
   }

   @Test
   public void branchingMethodWithUnreachableLines_avoidUnreachableCode()
   {
      tested.branchingMethodWithUnreachableLines(0);
   }

   @Test(expected = AssertionError.class)
   public void branchingMethodWithUnreachableLines_hitUnreachableCode()
   {
      tested.branchingMethodWithUnreachableLines(1);
   }

   @Test
   public void methodWithFourDifferentPathsAndSimpleLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSimpleLines(true, 0);
      tested.methodWithFourDifferentPathsAndSimpleLines(false, 1);

      getMethodData(tested, "methodWithFourDifferentPathsAndSimpleLines(ZI)V");
      assertEquals(4, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(52, methodData.getFirstLineOfImplementationBody());
      assertEquals(62, methodData.getLastLineOfImplementationBody());

      assertPath(4, 0); // should there be 5 nodes?
      assertPath(4, 1);
      assertPath(4, 1);
      assertPath(4, 0);
   }

   @Test
   public void methodWithFourDifferentPathsAndSegmentedLines_exerciseTwoOppositePaths()
   {
      tested.methodWithFourDifferentPathsAndSegmentedLines(false, -1);
      tested.methodWithFourDifferentPathsAndSegmentedLines(true, 1);

      getMethodData(tested, "methodWithFourDifferentPathsAndSegmentedLines(ZI)V");
      assertEquals(4, methodData.paths.size());
      assertEquals(2, methodData.getCoveredPaths());
      assertEquals(2, methodData.getExecutionCount());
      assertEquals(66, methodData.getFirstLineOfImplementationBody());
      assertEquals(70, methodData.getLastLineOfImplementationBody());

      assertPath(4, 1);
      assertPath(4, 0);
      assertPath(4, 0);
      assertPath(4, 1);
   }

   @Test
   public void singleLineMethodWithMultiplePaths()
   {
      tested.singleLineMethodWithMultiplePaths(true, false);

      getMethodData(tested, "singleLineMethodWithMultiplePaths(ZZ)Z");
      assertEquals(3, methodData.paths.size());
      assertEquals(1, methodData.getCoveredPaths());
      assertEquals(1, methodData.getExecutionCount());
      assertEquals(74, methodData.getFirstLineOfImplementationBody());
      assertEquals(77, methodData.getLastLineOfImplementationBody());

      assertPath(4, 0);
      assertPath(4, 1);
      assertPath(4, 0);
   }
}