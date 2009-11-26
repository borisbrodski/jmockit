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

final class InputFile
{
   final File sourceFile;
   final BufferedReader input;

   InputFile(List<File> sourceDirs, String filePath) throws FileNotFoundException
   {
      sourceFile = findSourceFile(sourceDirs, filePath);
      input = sourceFile == null ? null : new BufferedReader(new FileReader(sourceFile));
   }

   private File findSourceFile(List<File> sourceDirs, String filePath)
   {
      int p = filePath.indexOf('/');
      String topLevelPackage = p < 0 ? "" : filePath.substring(0, p);

      for (File sourceDir : sourceDirs) {
         File file = getSourceFile(sourceDir, topLevelPackage, filePath);

         if (file != null) {
            return file;
         }
      }

      return null;
   }

   private File getSourceFile(File sourceDir, String topLevelPackage, String filePath)
   {
      File file = new File(sourceDir, filePath);

      if (file.exists()) {
         return file;
      }

      File[] subDirs = sourceDir.listFiles();

      for (File subDir : subDirs) {
         if (
            subDir.isDirectory() && !subDir.isHidden() && !subDir.getName().equals(topLevelPackage)
         ) {
            file = getSourceFile(subDir, topLevelPackage, filePath);

            if (file != null) {
               return file;
            }
         }
      }

      return null;
   }

   boolean wasFileFound()
   {
      return sourceFile != null;
   }
}
