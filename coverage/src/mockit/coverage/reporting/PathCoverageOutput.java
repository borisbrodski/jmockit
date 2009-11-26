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
package mockit.coverage.reporting;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

final class PathCoverageOutput
{
   private final PrintWriter output;
   private final Iterator<MethodCoverageData> nextMethod;
   private MethodCoverageData currentMethod;
   private int previousMethodEndLine;

   PathCoverageOutput(Collection<MethodCoverageData> methods, PrintWriter output)
   {
      this.output = output;
      nextMethod = methods.iterator();

      if (nextMethod.hasNext()) {
         currentMethod = nextMethod.next();
      }
   }

   void writePathCoverageInfoIfLineStartsANewMethodOrConstructor(int lineNo, String line)
   {
      if (
         currentMethod == null || lineNo <= previousMethodEndLine ||
         lineNo > currentMethod.getFirstLineOfImplementationBody()
      ) {
         return;
      }

      int p = line.indexOf(currentMethod.methodName);

      if (p < 0) {
         return;
      }

      int q = p + currentMethod.methodName.length();

      if (
         (line.length() == q || line.charAt(q) != '(') &&
         (currentMethod.getFirstLineOfImplementationBody() > lineNo ||
          currentMethod.getLastLineOfImplementationBody() > lineNo)
      ) {
         return;
      }

      if (currentMethod.paths.size() > 1) {
         writePathCoverageHeaderForMethod();

         char pathId = 'A';

         for (Path path : currentMethod.paths) {
            writeCoverageInfoForIndividualPath(pathId, path);
            pathId++;
         }

         writePathCoverageFooterForMethod();
      }

      previousMethodEndLine = currentMethod.getLastLineOfImplementationBody();
      currentMethod = nextMethod.hasNext() ? nextMethod.next() : null;
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

   private void writeCoverageInfoForIndividualPath(char pathId, Path path)
   {
      int executionCount = path.getExecutionCount();
      StringBuilder lineIds = new StringBuilder();
      String sep = "";

      for (Node node : path.getNodes()) {
         if (node.line > 0) {
            lineIds.append(sep).append(node.line);
            sep = " ";
         }
      }

      output.write("        <span class='");
      output.write(executionCount == 0 ? "uncovered" : "covered");
      output.write("' onclick=\"showPath(this,'");
      output.print(pathId);
      output.write("','");
      output.write(lineIds.toString());
      output.write("')\">");
      output.print(pathId);
      output.write(": ");
      output.print(executionCount);
      output.println("</span>");
   }

   private void writePathCoverageFooterForMethod()
   {
      output.println("      </td>");
      output.println("    </tr>");
   }
}
