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
package mockit.coverage.data;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import mockit.coverage.*;
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
   public final Map<String, Boolean> staticFieldsData = new LinkedHashMap<String, Boolean>();
   public final Map<String, List<Integer>> instanceFieldsData =
      new LinkedHashMap<String, List<Integer>>();

   // Used to track the last time the ".class" file was modified, to decide if merging can be done:
   long lastModified;

   // Computed on demand, the first time the coverage percentage is requested:
   private transient int totalSegments;
   private transient int coveredSegments;
   private transient int totalPaths;
   private transient int coveredPaths;
   private transient int coveredDataItems = -1;

   public void addField(String className, String fieldName, boolean isStatic)
   {
      String classAndFieldNames = className + '.' + fieldName;

      if (isStatic) {
         staticFieldsData.put(classAndFieldNames, null);
      }
      else {
         instanceFieldsData.put(classAndFieldNames, new LinkedList<Integer>());
      }
   }

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

   public int getTotalDataItems()
   {
      return staticFieldsData.size() + instanceFieldsData.size();
   }

   public int getCoveredDataItems()
   {
      if (coveredDataItems >= 0) {
         return coveredDataItems;
      }

      coveredDataItems = 0;

      for (Boolean withUnreadValue : staticFieldsData.values()) {
         if (withUnreadValue == Boolean.TRUE) {
            coveredDataItems++;
         }
      }

      for (List<Integer> instancesWithUnreadValue : instanceFieldsData.values()) {
         coveredDataItems += instancesWithUnreadValue.size();
      }

      return coveredDataItems;
   }

   public int getDataCoveragePercentage()
   {
      int totalFields = getTotalDataItems();

      if (totalFields == 0) {
         return -1;
      }

      return CoveragePercentage.calculate(coveredDataItems, totalFields);
   }

   void mergeWithDataFromPreviousTestRun(FileCoverageData previousData)
   {
      mergeLineCoverageData(previousData.lineToLineData);
      mergePathCoverageData(previousData.firstLineToMethodData);
   }

   private void mergeLineCoverageData(Map<Integer, LineCoverageData> previousData)
   {
      for (Entry<Integer, LineCoverageData> lineAndData : lineToLineData.entrySet()) {
         Integer line = lineAndData.getKey();
         LineCoverageData previousLineData = previousData.get(line);

         if (previousLineData != null) {
            LineCoverageData lineData = lineAndData.getValue();
            lineData.addCountsFromPreviousTestRun(previousLineData);
         }
      }

      for (Entry<Integer, LineCoverageData> lineAndData : previousData.entrySet()) {
         Integer line = lineAndData.getKey();

         if (!lineToLineData.containsKey(line)) {
            lineToLineData.put(line, lineAndData.getValue());
         }
      }
   }

   private void mergePathCoverageData(Map<Integer, MethodCoverageData> previousData)
   {
      for (Entry<Integer, MethodCoverageData> firstLineAndData : firstLineToMethodData.entrySet()) {
         Integer firstLine = firstLineAndData.getKey();
         MethodCoverageData previousPathData = previousData.get(firstLine);

         if (previousPathData != null) {
            MethodCoverageData pathData = firstLineAndData.getValue();
            pathData.addCountsFromPreviousTestRun(previousPathData);
         }
      }

      for (Entry<Integer, MethodCoverageData> firstLineAndData : previousData.entrySet()) {
         Integer firstLine = firstLineAndData.getKey();

         if (!firstLineToMethodData.containsKey(firstLine)) {
            firstLineToMethodData.put(firstLine, firstLineAndData.getValue());
         }
      }
   }
}
