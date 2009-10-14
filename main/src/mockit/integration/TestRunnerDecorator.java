/*
 * JMockit
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
package mockit.integration;

import java.lang.reflect.*;

import mockit.internal.expectations.mocking.*;
import mockit.internal.capturing.*;
import mockit.internal.state.*;
import mockit.*;

/**
 * Base class for "test runner decorators", which provide integration between JMockit and specific
 * test runners from JUnit and TestNG.
 */
public class TestRunnerDecorator
{
   protected final void updateTestClassState(Object target, Class<?> testClass)
   {
      try {
         handleSwitchToNewTestClassIfApplicable(testClass);

         if (target != null) {
            handleMockFieldsForWholeTestClass(target);
         }
      }
      catch (Error e) {
         SavePoint.rollbackForTestClass();
         throw e;
      }
      catch (RuntimeException e) {
         SavePoint.rollbackForTestClass();
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
      SavePoint.rollbackForTestClass();
      CaptureOfSubtypes capture = TestRun.getCaptureOfSubtypes();

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
         CaptureOfSubtypes capture = new CaptureOfSubtypes();
         capture.makeSureAllSubtypesAreModified(null, capturingType);
         TestRun.setCaptureOfSubtypes(capture);
      }
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

   protected final Object[] createInstancesForMockParametersIfAny(
      Object target, Method testMethod, Object[] params)
   {
      int numParameters = testMethod.getParameterTypes().length;

      if (numParameters > 0) {
         return new ParameterTypeRedefinitions(target, testMethod).redefineParameterTypes();
      }

      return params;
   }
}
