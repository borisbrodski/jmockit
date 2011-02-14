/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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