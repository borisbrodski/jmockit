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
package mockit.coverage.reporting.pathCoverage;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

public final class PathCoverageOutput
{
   private final PrintWriter output;
   private final PathCoverageFormatter pathFormatter;
   private final Iterator<MethodCoverageData> nextMethod;

   // Helper fields:
   private MethodCoverageData currentMethod;

   public PathCoverageOutput(PrintWriter output, Collection<MethodCoverageData> methods)
   {
      this.output = output;
      pathFormatter = new PathCoverageFormatter(output);
      nextMethod = methods.iterator();
      moveToNextMethod();
   }

   private void moveToNextMethod()
   {
      currentMethod = nextMethod.hasNext() ? nextMethod.next() : null;
   }

   public void writePathCoverageInfoIfLineStartsANewMethodOrConstructor(int lineNumber)
   {
      if (currentMethod != null && lineNumber == currentMethod.getFirstLineInBody()) {
         writePathCoverageInformationForMethod();
         moveToNextMethod();
      }
   }

   private void writePathCoverageInformationForMethod()
   {
      if (currentMethod.paths.size() > 1) {
         writeHeaderForAllPaths();
         pathFormatter.writeInformationForEachPath(currentMethod.paths);
         writeFooterForAllPaths();
      }
   }

   private void writeHeaderForAllPaths()
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

   private void writeFooterForAllPaths()
   {
      output.println("      </td>");
      output.println("    </tr>");
   }
}
