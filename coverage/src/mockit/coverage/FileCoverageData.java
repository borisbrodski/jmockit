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
package mockit.coverage;

import java.io.*;
import java.util.*;

/**
 * Coverage data gathered for the lines and branches of a single source file.
 */
public final class FileCoverageData implements Serializable
{
   private static final long serialVersionUID = 3508592808457531011L;

   long lastModified;
   private final SortedMap<Integer, LineCoverageData> lineToLineData =
      new TreeMap<Integer, LineCoverageData>();

   LineCoverageData addLine(int line)
   {
      if (lineToLineData.containsKey(line)) {
         return lineToLineData.get(line);
      }
      else {
         LineCoverageData lineData = new LineCoverageData();
         lineToLineData.put(line, lineData);
         return lineData;
      }
   }

   void incrementLineCount(int line, CallPoint callPoint)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(callPoint);
   }

   void registerBranchExecution(int line, int branchIndex, boolean jumped, CallPoint callPoint)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(branchIndex, jumped, callPoint);
   }

   public SortedMap<Integer, LineCoverageData> getLineToLineData()
   {
      return Collections.unmodifiableSortedMap(lineToLineData);
   }

   public int getCoveragePercentage()
   {
      if (lineToLineData.isEmpty()) {
         return 100;
      }

      Collection<LineCoverageData> lines = lineToLineData.values();
      int result = 0;

      for (LineCoverageData line : lines) {
         result += line.getCoveragePercentage();
      }

      return (int) ((double) result / lines.size() + 0.5);
   }

   void addCountsFromPreviousMeasurement(FileCoverageData previousData)
   {
      for (Map.Entry<Integer, LineCoverageData> lineAndLineData : lineToLineData.entrySet()) {
         Integer line = lineAndLineData.getKey();
         LineCoverageData lineData = lineAndLineData.getValue();
         LineCoverageData previousLineData = previousData.lineToLineData.get(line);

         lineData.addCountsFromPreviousMeasurement(previousLineData);
      }
   }
}
