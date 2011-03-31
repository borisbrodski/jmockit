/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration;

import java.lang.reflect.*;

import mockit.internal.expectations.mocking.*;
import mockit.internal.capturing.*;
import mockit.internal.state.*;
import mockit.*;
import mockit.internal.util.*;

/**
 * Base class for "test runner decorators", which provide integration between JMockit and specific
 * test runners from JUnit and TestNG.
 */
public class TestRunnerDecorator
{
   private static SavePoint savePointForTest;

   protected final void updateTestClassState(Object target, Class<?> testClass)
   {
      try {
         handleSwitchToNewTestClassIfApplicable(testClass);

         if (target != null) {
            handleMockFieldsForWholeTestClass(target);
         }
      }
      catch (Error e) {
         try {
            SavePoint.rollbackForTestClass();
         }
         catch (Error err) {
            Utilities.filterStackTrace(err);
            throw err;
         }

         throw e;
      }
      catch (RuntimeException e) {
         SavePoint.rollbackForTestClass();
         Utilities.filterStackTrace(e);
         throw e;
      }
   }

   private void handleSwitchToNewTestClassIfApplicable(Class<?> testClass)
   {
      Class<?> currentTestClass = TestRun.getCurrentTestClass();

      if (testClass != currentTestClass) {
         if (currentTestClass == null) {
            SavePoint.registerNewActiveSavePoint();
         }
         else if (!currentTestClass.isAssignableFrom(testClass)) {
            cleanUpMocksFromPreviousTestClass();
            SavePoint.registerNewActiveSavePoint();
         }

         applyClassLevelMockingIfSpecifiedForTestClass(testClass);
         TestRun.setCurrentTestClass(testClass);
      }
   }

   public static void cleanUpMocksFromPreviousTestClass()
   {
      if (savePointForTest != null) {
         savePointForTest.rollback();
         savePointForTest = null;
      }

      SavePoint.rollbackForTestClass();
      CaptureOfImplementationsForTestClass capture = TestRun.getCaptureOfSubtypes();

      if (capture != null) {
         capture.cleanUp();
         TestRun.setCaptureOfSubtypes(null);
      }

      SharedFieldTypeRedefinitions redefinitions = TestRun.getSharedFieldTypeRedefinitions();

      if (redefinitions != null) {
         redefinitions.cleanUp();
         TestRun.setSharedFieldTypeRedefinitions(null);
      }
   }

   private void applyClassLevelMockingIfSpecifiedForTestClass(Class<?> testClass)
   {
      setUpClassLevelMocksAndStubs(testClass);
      setUpClassLevelCapturing(testClass);
   }

   protected final void setUpClassLevelMocksAndStubs(Class<?> testClass)
   {
      UsingMocksAndStubs mocksAndStubs = testClass.getAnnotation(UsingMocksAndStubs.class);

      if (mocksAndStubs != null) {
         Mockit.setUpMocksAndStubs(mocksAndStubs.value());
      }
   }

   private void setUpClassLevelCapturing(Class<?> testClass)
   {
      Capturing capturingType = testClass.getAnnotation(Capturing.class);

      if (capturingType != null) {
         CaptureOfImplementationsForTestClass capture = new CaptureOfImplementationsForTestClass();
         capture.makeSureAllSubtypesAreModified(capturingType);
         TestRun.setCaptureOfSubtypes(capture);
      }
   }

   protected final void prepareForNextTest()
   {
      if (savePointForTest != null) {
         savePointForTest.rollback();
      }

      savePointForTest = new SavePoint();
      TestRun.prepareForNextTest();
   }

   private void handleMockFieldsForWholeTestClass(Object target)
   {
      SharedFieldTypeRedefinitions sharedRedefinitions = TestRun.getSharedFieldTypeRedefinitions();

      if (sharedRedefinitions == null) {
         sharedRedefinitions = new SharedFieldTypeRedefinitions(target);
         sharedRedefinitions.redefineTypesForTestClass();
         TestRun.setSharedFieldTypeRedefinitions(sharedRedefinitions);
      }

      if (target != TestRun.getCurrentTestInstance()) {
         sharedRedefinitions.assignNewInstancesToMockFields(target);
      }
   }

   protected final Object[] createInstancesForMockParametersIfAny(Object target, Method testMethod, Object[] params)
   {
      SharedFieldTypeRedefinitions sharedFields = TestRun.getSharedFieldTypeRedefinitions();

      if (sharedFields == null) {
         return params;
      }

      TestRun.enterNoMockingZone();

      try {
         sharedFields.getTestedClassRedefinitions().assignNewInstancesToTestedFields(target);

         if (testMethod.getParameterTypes().length == 0) {
            return params;
         }

         ParameterTypeRedefinitions redefinitions = new ParameterTypeRedefinitions(target, testMethod);
         TestRun.getExecutingTest().setParameterTypeRedefinitions(redefinitions);

         return redefinitions.getParameterValues();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
