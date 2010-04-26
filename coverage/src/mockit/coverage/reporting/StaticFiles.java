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
package mockit.coverage.reporting;

import java.io.*;

final class StaticFiles
{
   void copyToOutputDir(String outputDir, boolean forSourceFilePages) throws IOException
   {
      String pathToThisJar =
         getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
      long timeOfCoverageJar = new File(pathToThisJar).lastModified();

      copyFile(outputDir, "coverage.css", timeOfCoverageJar);
      copyFile(outputDir, "coverage.js", timeOfCoverageJar);

      if (forSourceFilePages) {
         copyFile(outputDir, "prettify.css", timeOfCoverageJar);
         copyFile(outputDir, "prettify.js", timeOfCoverageJar);
      }
   }

   private void copyFile(String outputDir, String fileName, long timeOfCoverageJar)
      throws IOException
   {
      File outputFile = new File(outputDir, fileName);

      if (outputFile.exists() && timeOfCoverageJar < outputFile.lastModified()) {
         return;
      }

      OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
      InputStream input = new BufferedInputStream(StaticFiles.class.getResourceAsStream(fileName));

      try {
         int b;

         while ((b = input.read()) != -1) {
            output.write(b);
         }
      }
      finally {
         try {
            input.close();
         }
         finally {
            output.close();
         }
      }
   }
}