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

import java.util.*;

import mockit.coverage.data.*;

final class PackageCoverageReport extends ListWithFilesAndPercentages
{
   private final Map<String, FileCoverageData> filesToFileData;
   private final boolean withSourceFiles;

   PackageCoverageReport(
      OutputFile output, Map<String, FileCoverageData> filesToFileData, boolean withSourceFiles)
   {
      super(output, "          ");
      this.filesToFileData = filesToFileData;
      this.withSourceFiles = withSourceFiles;
   }

   @Override
   protected int getTotalSegments(String filePath)
   {
      return filesToFileData.get(filePath).getTotalSegments();
   }

   @Override
   protected int getCoveredSegments(String filePath)
   {
      return filesToFileData.get(filePath).getCoveredSegments();
   }

   @Override
   protected int getCodeCoveragePercentageForFile(String filePath)
   {
      return filesToFileData.get(filePath).getCodeCoveragePercentage();
   }

   @Override
   protected int getTotalPaths(String filePath)
   {
      return filesToFileData.get(filePath).getTotalPaths();
   }

   @Override
   protected int getCoveredPaths(String filePath)
   {
      return filesToFileData.get(filePath).getCoveredPaths();
   }

   @Override
   protected int getPathCoveragePercentageForFile(String filePath)
   {
      return filesToFileData.get(filePath).getPathCoveragePercentage();
   }

   @Override
   protected String getHRefToFile(String filePath)
   {
      return withSourceFiles ? filePath.replace(".java", ".html") : null;
   }

   @Override
   protected String getFileNameForDisplay(String filePath)
   {
      int p1 = filePath.lastIndexOf('/') + 1;
      int p2 = filePath.lastIndexOf('.');

      return filePath.substring(p1, p2);
   }

   @Override
   protected void writeInternalTableForChildren(String filePath)
   {
   }
}