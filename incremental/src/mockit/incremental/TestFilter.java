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

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public final class TestFilter
{
   private final Properties coverageMap;
   private long lastTestRun;

   public TestFilter(Properties coverageMap)
   {
      this.coverageMap = coverageMap;
   }

   public boolean shouldIgnoreTestInCurrentTestRun(Method testMethod)
   {
      String testClassName = testMethod.getDeclaringClass().getName();
      String testName = testClassName + '.' + testMethod.getName();
      String coverageInfo = coverageMap.getProperty(testName);

      if (coverageInfo != null && coverageInfo.indexOf(',') > 0) {
         String[] lastRunAndSourcesCovered = coverageInfo.split(",");
         lastTestRun = Long.parseLong(lastRunAndSourcesCovered[0]);
         String testClassFileName = testClassName.replace('.', '/') + ".class";

         if (
            hasClassFileChangedSinceLastTestRun(testClassFileName) ||
            hasTestedCodeChangedSinceLastTestRun(lastRunAndSourcesCovered)
         ) {
            coverageMap.remove(testName);
            return false;
         }

         return true;
      }

      return false;
   }

   private boolean hasClassFileChangedSinceLastTestRun(String sourceFileName)
   {
      String classFileName = sourceFileName.replace(".java", ".class");
      String classFilePath = getClass().getResource("/" + classFileName).getPath();
      File classFile = new File(classFilePath);

      return classFile.lastModified() > lastTestRun;
   }

   private boolean hasTestedCodeChangedSinceLastTestRun(String[] sourceFilesCovered)
   {
      for (int i = 1; i < sourceFilesCovered.length; i++) {
         if (hasClassFileChangedSinceLastTestRun(sourceFilesCovered[i])) {
            return true;
         }
      }

      return false;
   }
}
