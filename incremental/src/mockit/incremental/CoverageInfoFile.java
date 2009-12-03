/*
 * JMockit Incremental Testing
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
package mockit.incremental;

import java.io.*;
import java.util.Map.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.data.*;

public final class CoverageInfoFile
{
   private final Properties coverageMap;
   private String currentTime;

   public CoverageInfoFile(Properties coverageMap)
   {
      this.coverageMap = coverageMap;
      loadCoverageDataForPreviousTestRun();
      CoverageData.instance().setWithCallPoints(true);
   }

   private void loadCoverageDataForPreviousTestRun()
   {
      File coverageFile = new File("testRun.properties");

      if (coverageFile.exists()) {
         try {
            loadCoverageMapFromPropertiesFile(coverageFile);
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void loadCoverageMapFromPropertiesFile(File coverageFile) throws IOException
   {
      InputStream input = new FileInputStream(coverageFile);

      try {
         coverageMap.load(input);
      }
      finally {
         input.close();
      }
   }

   public void saveToFile()
   {
      CoverageData coverageData = CoverageData.instance();
      currentTime = System.currentTimeMillis() + ",";

      for (Entry<String, FileCoverageData> entry : coverageData.getFileToFileDataMap().entrySet()) {
         String sourceFile = entry.getKey();
         FileCoverageData fileCoverageData = entry.getValue();

         updateInfoForSourceFile(sourceFile, fileCoverageData);
      }

      try {
         createPropertiesFile();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void updateInfoForSourceFile(String sourceFile, FileCoverageData fileCoverageData)
   {
      for (LineCoverageData lineData : fileCoverageData.getLineToLineData().values()) {
         updateInfoForLineOfCode(sourceFile, lineData);
      }
   }

   private void updateInfoForLineOfCode(String sourceFile, LineCoverageData lineData)
   {
      for (CallPoint callPoint : lineData.getCallPoints()) {
         StackTraceElement ste = callPoint.getStackTraceElement();
         String testName = ste.getClassName() + '.' + ste.getMethodName();

         updateCoverageMapForTest(sourceFile, testName);
      }
   }

   private void updateCoverageMapForTest(String sourceFile, String testName)
   {
      String coverageInfo = coverageMap.getProperty(testName);

      if (coverageInfo == null) {
         coverageInfo = currentTime + sourceFile;
      }
      else {
         coverageInfo = currentTime + coverageInfo.substring(coverageInfo.indexOf(',') + 1);

         if (!coverageInfo.contains(sourceFile)) {
            coverageInfo += ',' + sourceFile;
         }
      }

      coverageMap.setProperty(testName, coverageInfo);
   }

   private void createPropertiesFile() throws IOException
   {
      FileOutputStream output = new FileOutputStream("testRun.properties");

      try {
         coverageMap.store(output, "JMockit Incremental Testing: test run info");
      }
      finally {
         output.close();
      }
   }
}
