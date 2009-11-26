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
import java.util.Map.*;

import mockit.coverage.*;

class CoverageReport
{
   private final String outputDir;
   private final List<File> sourceDirs;
   private final Map<String, FileCoverageData> filesToFileData;
   private final Map<String, List<String>> packagesToFiles;
   private final boolean withCallPoints;

   protected CoverageReport(
      String outputDir, String[] sourceDirs, CoverageData coverageData, boolean withCallPoints)
   {
      this.outputDir = outputDir.length() > 0 ? outputDir : "coverage-report";

      boolean useSpecifiedSrcDirs = sourceDirs.length > 0;
      this.sourceDirs = useSpecifiedSrcDirs ? asFiles(sourceDirs) : allSrcSubDirs();

      if (this.sourceDirs.isEmpty()) {
         if (useSpecifiedSrcDirs) {
            throw new IllegalStateException("None of the specified source directories exist");
         }
         else {
            throw new IllegalStateException(
               "No \"src\" directories found under \"" + new File("").getAbsolutePath() + '\"');
         }
      }

      filesToFileData = coverageData.getFileToFileDataMap();
      packagesToFiles = new HashMap<String, List<String>>();
      this.withCallPoints = withCallPoints;
   }

   private List<File> asFiles(String[] dirs)
   {
      List<File> result = new ArrayList<File>(dirs.length);

      for (String dir : dirs) {
         File srcDir = new File(dir);

         if (srcDir.isDirectory()) {
            result.add(srcDir);
         }
      }

      return result;
   }

   private List<File> allSrcSubDirs()
   {
      List<File> srcSubDirs = new ArrayList<File>();
      addSrcSubDirs(new File("."), srcSubDirs);

      return srcSubDirs;
   }

   private void addSrcSubDirs(File dir, List<File> srcSubDirs)
   {
      for (File subDir : dir.listFiles()) {
         if (subDir.isDirectory()) {
            if ("src".equals(subDir.getName())) {
               srcSubDirs.add(subDir);
            }
            else {
               addSrcSubDirs(subDir, srcSubDirs);
            }
         }
      }
   }

   public final void generate() throws IOException
   {
      if (filesToFileData.isEmpty()) {
         return;
      }

      createOutputDirIfNotExists();

      File outputFile = new File(outputDir, "index.html");

      if (outputFile.exists() && !outputFile.canWrite()) {
         System.out.println(
            "JMockit: " + outputFile.getCanonicalPath() +
            " is read-only; report generation canceled");
         return;
      }

      if (sourceDirs.size() > 1) {
         System.out.println("JMockit: Coverage source dirs: " + sourceDirs);
      }

      generateFileCoverageReportsWhileBuildingPackageLists();
      new IndexPage(outputFile).generate(filesToFileData, packagesToFiles);
      OutputFile.copyCSSFile(outputDir);

      System.out.println(
         "JMockit: Coverage report written to " + new File(outputDir).getCanonicalPath());
   }

   private void createOutputDirIfNotExists()
   {
      File outDir = new File(outputDir);

      if (!outDir.exists()) {
         boolean dirCreated = outDir.mkdir();
         assert dirCreated : "Failed to create output dir: " + outputDir;
      }
   }

   private void generateFileCoverageReportsWhileBuildingPackageLists() throws IOException
   {
      Set<Entry<String, FileCoverageData>> files = filesToFileData.entrySet();

      for (Entry<String, FileCoverageData> fileAndFileData : files) {
         String file = fileAndFileData.getKey();
         FileCoverageData fileData = fileAndFileData.getValue();

         FileCoverageReport fileReport =
            new FileCoverageReport(outputDir, sourceDirs, file, fileData, withCallPoints);

         if (fileReport.inputFile.wasFileFound()) {
            fileReport.generate();
            addFileToPackageFileList(file);
         }
      }
   }

   private void addFileToPackageFileList(String file)
   {
      int p = file.lastIndexOf('/');
      String filePackage = p < 0 ? "" : file.substring(0, p);
      List<String> filesInPackage = packagesToFiles.get(filePackage);

      if (filesInPackage == null) {
         filesInPackage = new ArrayList<String>();
         packagesToFiles.put(filePackage, filesInPackage);
      }

      filesInPackage.add(file);
   }
}
