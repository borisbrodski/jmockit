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

import mockit.coverage.data.*;
import mockit.coverage.paths.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class TestRun
{
   private static final ThreadLocal<Boolean> executingCall = new ThreadLocal<Boolean>()
   {
      @Override
      protected Boolean initialValue() { return false; }
   };

   private TestRun() {}

   public static void lineExecuted(String file, int line)
   {
      if (executingCall.get()) {
         return;
      }

      executingCall.set(true);

      CoverageData coverageData = CoverageData.instance();
      CallPoint callPoint =
         coverageData.isWithCallPoints() ? CallPoint.create(new Throwable()) : null;

      FileCoverageData fileData = coverageData.getFileData(file);
      fileData.incrementLineCount(line, callPoint);

      executingCall.set(false);
   }

   public static void jumpTargetExecuted(String file, int line, int segment)
   {
      if (executingCall.get()) {
         return;
      }

      executingCall.set(true);

      CoverageData coverageData = CoverageData.instance();
      CallPoint callPoint =
         coverageData.isWithCallPoints() ? CallPoint.create(new Throwable()) : null;

      FileCoverageData fileData = coverageData.getFileData(file);
      fileData.registerBranchExecution(line, segment, true, callPoint);

      executingCall.set(false);
   }

   public static void noJumpTargetExecuted(String file, int line, int segment)
   {
      if (executingCall.get()) {
         return;
      }

      executingCall.set(true);

      CoverageData coverageData = CoverageData.instance();
      CallPoint callPoint =
         coverageData.isWithCallPoints() ? CallPoint.create(new Throwable()) : null;

      FileCoverageData fileData = coverageData.getFileData(file);
      fileData.registerBranchExecution(line, segment, false, callPoint);

      executingCall.set(false);
   }

   public static void nodeReached(String file, int firstLineInMethodBody, int node)
   {
      if (executingCall.get()) {
         return;
      }

      executingCall.set(true);

      FileCoverageData fileData = CoverageData.instance().getFileData(file);
      MethodCoverageData methodData = fileData.firstLineToMethodData.get(firstLineInMethodBody);

      if (methodData != null) {
         methodData.markNodeAsReached(node);
      }

      executingCall.set(false);
   }

   public static void fieldAssigned(String file, String classAndFieldNames)
   {
      FileCoverageData fileData = CoverageData.instance().getFileData(file);
      fileData.dataCoverageInfo.registerAssignmentToStaticField(classAndFieldNames);
   }

   public static void fieldRead(String file, String classAndFieldNames)
   {
      FileCoverageData fileData = CoverageData.instance().getFileData(file);
      fileData.dataCoverageInfo.registerReadOfStaticField(classAndFieldNames);
   }

   public static void fieldAssigned(Object instance, String file, String classAndFieldNames)
   {
      FileCoverageData fileData = CoverageData.instance().getFileData(file);
      fileData.dataCoverageInfo.registerAssignmentToInstanceField(instance, classAndFieldNames);
   }

   public static void fieldRead(Object instance, String file, String classAndFieldNames)
   {
      FileCoverageData fileData = CoverageData.instance().getFileData(file);
      fileData.dataCoverageInfo.registerReadOfInstanceField(instance, classAndFieldNames);
   }
}
