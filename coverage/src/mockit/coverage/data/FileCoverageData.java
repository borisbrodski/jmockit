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
package mockit.coverage.data;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import mockit.coverage.*;
import mockit.coverage.data.dataItems.*;
import mockit.coverage.paths.*;

/**
 * Coverage data gathered for the lines and branches of a single source file.
 */
public final class FileCoverageData implements Serializable
{
   private static final long serialVersionUID = 3508592808457531011L;

   public final SortedMap<Integer, LineCoverageData> lineToLineData =
      new TreeMap<Integer, LineCoverageData>();
   public final Map<Integer, MethodCoverageData> firstLineToMethodData =
      new LinkedHashMap<Integer, MethodCoverageData>();
   public final DataCoverageInfo dataCoverageInfo = new DataCoverageInfo();

   // Used to track the last time the ".class" file was modified, to decide if merging can be done:
   long lastModified;

   // Computed on demand, the first time the coverage percentage is requested:
   private transient int totalSegments;
   private transient int coveredSegments;
   private transient int totalPaths;
   private transient int coveredPaths;

   public void addMethod(MethodCoverageData methodData)
   {
      firstLineToMethodData.put(methodData.getFirstLineInBody(), methodData);
   }

   public LineCoverageData addLine(int line)
   {
      LineCoverageData lineData = lineToLineData.get(line);

      if (lineData == null) {
         lineData = new LineCoverageData();
         lineToLineData.put(line, lineData);
      }

      return lineData;
   }

   public void incrementLineCount(int line, CallPoint cp)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(cp);
   }

   public void registerBranchExecution(int line, int branchIndex, boolean jumped, CallPoint cp)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(branchIndex, jumped, cp);
   }

   public SortedMap<Integer, LineCoverageData> getLineToLineData()
   {
      return lineToLineData;
   }

   public Collection<MethodCoverageData> getMethods()
   {
      return firstLineToMethodData.values();
   }

   public int getTotalSegments()
   {
      return totalSegments;
   }

   public int getCoveredSegments()
   {
      return coveredSegments;
   }

   public int getTotalPaths()
   {
      return totalPaths;
   }

   public int getCoveredPaths()
   {
      return coveredPaths;
   }

   public int getCodeCoveragePercentage()
   {
      if (lineToLineData.isEmpty()) {
         return -1;
      }

      if (totalSegments == 0) {
         Collection<LineCoverageData> lines = lineToLineData.values();

         for (LineCoverageData line : lines) {
            totalSegments += line.getNumberOfSegments();
            coveredSegments += line.getNumberOfCoveredSegments();
         }
      }

      return CoveragePercentage.calculate(coveredSegments, totalSegments);
   }

   public int getPathCoveragePercentage()
   {
      if (firstLineToMethodData.isEmpty()) {
         return -1;
      }

      if (totalPaths == 0) {
         for (MethodCoverageData method : firstLineToMethodData.values()) {
            totalPaths += method.paths.size();
            coveredPaths += method.getCoveredPaths();
         }
      }

      return CoveragePercentage.calculate(coveredPaths, totalPaths);
   }

   void mergeWithDataFromPreviousTestRun(FileCoverageData previousInfo)
   {
      mergeLineCoverageInformation(previousInfo.lineToLineData);
      mergePathCoverageInformation(previousInfo.firstLineToMethodData);
      dataCoverageInfo.mergeInformation(previousInfo.dataCoverageInfo);
   }

   private void mergeLineCoverageInformation(Map<Integer, LineCoverageData> previousInfo)
   {
      for (Entry<Integer, LineCoverageData> lineAndInfo : lineToLineData.entrySet()) {
         Integer line = lineAndInfo.getKey();
         LineCoverageData previousLineInfo = previousInfo.get(line);

         if (previousLineInfo != null) {
            LineCoverageData lineInfo = lineAndInfo.getValue();
            lineInfo.addCountsFromPreviousTestRun(previousLineInfo);
         }
      }

      for (Entry<Integer, LineCoverageData> lineAndInfo : previousInfo.entrySet()) {
         Integer line = lineAndInfo.getKey();

         if (!lineToLineData.containsKey(line)) {
            lineToLineData.put(line, lineAndInfo.getValue());
         }
      }
   }

   private void mergePathCoverageInformation(Map<Integer, MethodCoverageData> previousInfo)
   {
      for (Entry<Integer, MethodCoverageData> firstLineAndInfo : firstLineToMethodData.entrySet()) {
         Integer firstLine = firstLineAndInfo.getKey();
         MethodCoverageData previousPathInfo = previousInfo.get(firstLine);

         if (previousPathInfo != null) {
            MethodCoverageData pathInfo = firstLineAndInfo.getValue();
            pathInfo.addCountsFromPreviousTestRun(previousPathInfo);
         }
      }

      for (Entry<Integer, MethodCoverageData> firstLineAndInfo : previousInfo.entrySet()) {
         Integer firstLine = firstLineAndInfo.getKey();

         if (!firstLineToMethodData.containsKey(firstLine)) {
            firstLineToMethodData.put(firstLine, firstLineAndInfo.getValue());
         }
      }
   }
}
