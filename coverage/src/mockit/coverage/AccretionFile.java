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
package mockit.coverage;

import java.io.*;
import java.util.*;

import mockit.coverage.data.*;

final class AccretionFile
{
   private final File outputFile;
   private final List<File> inputFiles;

   AccretionFile(String outputDir, String[] pathsToInputFiles)
   {
      String parentDir = outputDir.length() == 0 ? null : outputDir;
      outputFile = new File(parentDir, "coverage.ser");

      inputFiles = getListOfInputFilesIfInputPathsAvailable(pathsToInputFiles);
   }

   private List<File> getListOfInputFilesIfInputPathsAvailable(String[] inputPaths)
   {
      if (inputPaths == null) {
         return null;
      }

      List<File> listOfFiles = new ArrayList<File>(inputPaths.length);

      for (String path : inputPaths) {
         addInputFileToList(listOfFiles, path.trim());
      }

      return listOfFiles;
   }

   private void addInputFileToList(List<File> files, String path)
   {
      if (path.length() > 0) {
         File inputFile = new File(path);

         if (inputFile.isDirectory()) {
            inputFile = new File(inputFile, "coverage.ser");
         }

         files.add(inputFile);
      }
   }

   void generate(CoverageData newData) throws ClassNotFoundException, IOException
   {
      newData.fillLastModifiedTimesForAllClassFiles();

      if (inputFiles == null) {
         mergeDataFromExistingFile(newData, outputFile);
      }
      else {
         for (File inputFile : inputFiles) {
            mergeDataFromExistingFile(newData, inputFile);
         }
      }

      newData.writeDataToFile(outputFile);
      System.out.println("JMockit: Coverage data written to " + outputFile.getCanonicalPath());
   }

   private void mergeDataFromExistingFile(CoverageData newData, File dataFile)
      throws IOException, ClassNotFoundException
   {
      if (dataFile.exists()) {
         CoverageData previousData = CoverageData.readDataFromFile(dataFile);
         newData.merge(previousData);
      }
   }
}
