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
import java.util.regex.*;

public final class OutputFile extends PrintWriter
{
   private static final Pattern PATH_SEPARATOR = Pattern.compile("/");

   private final String relPathToOutDir;
   private final boolean withPrettyPrint;

   public OutputFile(File file) throws IOException
   {
      super(new FileWriter(file));
      relPathToOutDir = "";
      withPrettyPrint = false;
   }

   public OutputFile(String outputDir, String sourceFilePath) throws IOException
   {
      super(new FileWriter(getOutputFileCreatingDirIfNeeded(outputDir, sourceFilePath)));
      relPathToOutDir = getRelativeSubPathToOutputDir(sourceFilePath);
      withPrettyPrint = true;
   }

   private static File getOutputFileCreatingDirIfNeeded(String outputDir, String sourceFilePath)
   {
      File outputFile = new File(outputDir, sourceFilePath.replace(".java", ".html"));
      File parentDir = outputFile.getParentFile();

      if (!parentDir.exists()) {
         boolean outputDirCreated = parentDir.mkdirs();
         assert outputDirCreated : "Failed to create output dir: " + outputDir;
      }

      return outputFile;
   }

   private static String getRelativeSubPathToOutputDir(String filePath)
   {
      StringBuilder cssRelPath = new StringBuilder();
      int n = PATH_SEPARATOR.split(filePath).length;

      for (int i = 1; i < n; i++) {
         cssRelPath.append("../");
      }

      return cssRelPath.toString();
   }

   public void writeCommonHeader()
   {
      println("<?xml version='1.0' encoding='UTF-8'?>");
      println(
         "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN'" +
         " 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
      println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
      println("<head>");
      println("  <title>JMockit Coverage Report</title>");
      println("  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
      print("  <link rel='stylesheet' type='text/css' href='");
      print(relPathToOutDir);
      println("coverage.css'/>");
      print("  <script type='text/javascript' src='");
      print(relPathToOutDir);
      println("coverage.js'></script>");

      if (withPrettyPrint) {
         print("  <link rel='stylesheet' type='text/css' href='");
         print(relPathToOutDir);
         println("prettify.css'/>");
         print("  <script type='text/javascript' src='");
         print(relPathToOutDir);
         println("prettify.js'></script>");
      }

      println("</head>");

      if (withPrettyPrint) {
         println("<body onload='prettyPrint()'>");
      }
      else {
         println("<body>");
      }
   }

   public void writeCommonFooter()
   {
      println("</body>");
      println("</html>");
   }
}
