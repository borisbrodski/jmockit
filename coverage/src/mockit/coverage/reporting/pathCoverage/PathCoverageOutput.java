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
package mockit.coverage.reporting.pathCoverage;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;
import mockit.coverage.reporting.parsing.*;

public final class PathCoverageOutput
{
   private final PrintWriter output;
   private final Iterator<MethodCoverageData> nextMethod;

   // Helper fields:
   private MethodCoverageData currentMethod;
   private final StringBuilder lineIds = new StringBuilder(100);

   public PathCoverageOutput(PrintWriter output, Collection<MethodCoverageData> methods)
   {
      this.output = output;
      nextMethod = methods.iterator();
      moveToNextMethod();
   }

   private void moveToNextMethod()
   {
      currentMethod = nextMethod.hasNext() ? nextMethod.next() : null;
   }

   public void writePathCoverageInfoIfLineStartsANewMethodOrConstructor(LineParser lineParser)
   {
      if (currentMethod != null && lineParser.getLineNo() == currentMethod.getFirstLineInBody()) {
         writePathCoverageInformationForMethod();
         moveToNextMethod();
      }
   }

   private void writePathCoverageInformationForMethod()
   {
      if (currentMethod.paths.size() > 1) {
         writePathCoverageHeaderForMethod();

         char pathId1 = 'A';
         char pathId2 = '\0';

         for (Path path : currentMethod.paths) {
            writeCoverageInfoForIndividualPath(pathId1, pathId2, path);

            if (pathId2 == '\0' && pathId1 < 'Z') {
               pathId1++;
            }
            else if (pathId2 == '\0') {
               pathId1 = 'A';
               pathId2 = 'A';
            }
            else if (pathId2 < 'Z') {
               pathId2++;
            }
            else {
               pathId1++;
               pathId2 = 'A';
            }
         }

         writePathCoverageFooterForMethod();
      }
   }

   private void writePathCoverageHeaderForMethod()
   {
      int coveredPaths = currentMethod.getCoveredPaths();
      int totalPaths = currentMethod.paths.size();
      int coveragePercentage = CoveragePercentage.calculate(coveredPaths, totalPaths);

      output.println("    <tr>");
      output.write("      <td></td><td class='count'>");
      output.print(currentMethod.getExecutionCount());
      output.println("</td>");
      output.println("      <td class='paths'>");
      output.write("        <span style='cursor:default; background-color:#");
      output.write(CoveragePercentage.percentageColor(coveragePercentage));
      output.write("' onclick='hidePath()'>Path coverage: ");
      output.print(coveredPaths);
      output.print('/');
      output.print(totalPaths);
      output.println("</span>");
   }

   private void writeCoverageInfoForIndividualPath(char pathId1, char pathId2, Path path)
   {
      int executionCount = path.getExecutionCount();
      String lineIdsForPath = getIdsForLinesBelongingToThePath(path);

      output.write("        <span class='");
      output.write(executionCount == 0 ? "uncovered" : "covered");
      output.write("' onclick=\"showPath(this,'");
      writePathId(pathId1, pathId2);
      output.write("','");
      output.write(lineIdsForPath);
      output.write("')\">");
      writePathId(pathId1, pathId2);
      output.write(": ");
      output.print(executionCount);
      output.println("</span>");
   }

   private void writePathId(char pathId1, char pathId2)
   {
      output.write(pathId1);

      if (pathId2 != '\0') {
         output.write(pathId2);
      }
   }

   private String getIdsForLinesBelongingToThePath(Path path)
   {
      lineIds.setLength(0);

      int previousLine = 0;
      String sep = "l";

      for (Node node : path.getNodes()) {
         int line = node.line;

         if (line > previousLine) {
            lineIds.append(sep).append(line);
            previousLine = line;
            sep = " l";
         }
      }

      return lineIds.toString();
   }

   private void writePathCoverageFooterForMethod()
   {
      output.println("      </td>");
      output.println("    </tr>");
   }
}
