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
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import mockit.internal.util.*;

/**
 * Coverage data captured for all source files exercised during a test run.
 */
public final class CoverageData implements Serializable
{
   private static final long serialVersionUID = -4860004226098360259L;

   private static final CoverageData instance = new CoverageData();

   public static CoverageData instance()
   {
      return instance;
   }

   boolean withCallPoints;
   private final Map<String, FileCoverageData> fileToFileData =
      new ConcurrentHashMap<String, FileCoverageData>();

   public void setWithCallPoints(boolean withCallPoints)
   {
      this.withCallPoints = withCallPoints;
   }

   public Map<String, FileCoverageData> getFileToFileDataMap()
   {
      return Collections.unmodifiableMap(fileToFileData);
   }

   FileCoverageData addFile(String file)
   {
      FileCoverageData fileData = getFileData(file);

      if (fileData == null) {
         fileData = new FileCoverageData();
         fileToFileData.put(file, fileData);
      }

      return fileData;
   }

   FileCoverageData getFileData(String file)
   {
      return fileToFileData.get(file);
   }

   void fillLastModifiedTimesForAllClassFiles()
   {
      for (Map.Entry<String, FileCoverageData> fileAndFileData : fileToFileData.entrySet()) {
         File coveredClassFile = getClassFile(fileAndFileData.getKey());
         fileAndFileData.getValue().lastModified = coveredClassFile.lastModified();
      }
   }

   private File getClassFile(String sourceFilePath)
   {
      String sourceFilePathNoExt = sourceFilePath.substring(0, sourceFilePath.length() - 5);
      Class<?> coveredClass = Utilities.loadClass(sourceFilePathNoExt.replace('/', '.'));
      CodeSource codeSource = coveredClass.getProtectionDomain().getCodeSource();
      String pathToClassFile = codeSource.getLocation().getPath() + sourceFilePathNoExt + ".class";

      return new File(pathToClassFile);
   }

   static CoverageData readDataFromFile(File dataFile) throws IOException, ClassNotFoundException
   {
      ObjectInputStream input = new ObjectInputStream(new FileInputStream(dataFile));

      try {
         return (CoverageData) input.readObject();
      }
      finally {
         input.close();
      }
   }

   void writeDataToFile(File dataFile) throws IOException
   {
      ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(dataFile));

      try {
         output.writeObject(this);
      }
      finally {
         output.close();
      }
   }

   void merge(CoverageData previousData)
   {
      withCallPoints |= previousData.withCallPoints;

      for (
         Map.Entry<String, FileCoverageData> previousFileAndFileData :
            previousData.fileToFileData.entrySet()
      ) {
         String previousFile = previousFileAndFileData.getKey();
         FileCoverageData previousFileData = previousFileAndFileData.getValue();
         FileCoverageData fileData = fileToFileData.get(previousFile);

         if (fileData == null) {
            fileToFileData.put(previousFile, previousFileData);
         }
         else if (previousFileData.lastModified == fileData.lastModified) {
            fileData.addCountsFromPreviousMeasurement(previousFileData);
         }
      }
   }
}
