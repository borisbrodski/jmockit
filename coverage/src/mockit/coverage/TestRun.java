/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
