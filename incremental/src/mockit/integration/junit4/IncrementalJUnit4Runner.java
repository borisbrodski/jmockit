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
package mockit.integration.junit4;

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import java.io.*;

import org.junit.internal.runners.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import org.junit.runners.*;
import org.junit.runners.model.*;
import org.junit.*;

import mockit.*;
import mockit.incremental.*;
import mockit.internal.util.*;

/**
 * JUnit 4 test runner which ignores tests not affected by current local changes in production code.
 * <p/>
 * This allows incremental execution of tests according to the changes made to production code,
 * instead of running the full suite of tests covering such code every time.
 */
@MockClass(realClass = ParentRunner.class)
public final class IncrementalJUnit4Runner
{
   private static final Method shouldRunMethod;

   static
   {
      try {
         shouldRunMethod = ParentRunner.class.getDeclaredMethod("shouldRun", Object.class);
      }
      catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }

      shouldRunMethod.setAccessible(true);
   }

   private final Properties coverageMap = new Properties();
   public ParentRunner<?> it;
   private RunNotifier runNotifier;
   private final Map<Method, Boolean> testMethods = new HashMap<Method, Boolean>();

   public IncrementalJUnit4Runner()
   {
      File testRunFile = new File("testRun.properties");

      if (testRunFile.exists() && !testRunFile.canWrite()) {
         throw new IllegalStateException();
      }
   }

   @Mock(reentrant = true)
   public void run(RunNotifier notifier)
   {
      runNotifier = notifier;

      CoverageInfoFile coverageFile = new CoverageInfoFile(coverageMap);
      it.run(notifier);
      coverageFile.saveToFile();
   }

   @Mock(reentrant = true)
   public boolean shouldRun(Object m)
   {
      Method testMethod = null;

      if (!coverageMap.isEmpty()) {
         if (m instanceof JUnit38ClassRunner) {
            JUnit38ClassRunner runner = (JUnit38ClassRunner) m;
            Description testClassDescription = runner.getDescription();
            Class<?> testClass = testClassDescription.getTestClass();

            Iterator<Description> itr = testClassDescription.getChildren().iterator();

            while (itr.hasNext()) {
               Description testDescription = itr.next();
               String testMethodName = testDescription.getMethodName();
               testMethod = Utilities.findPublicVoidMethod(testClass, testMethodName);

               Boolean shouldRun = shouldRunTestInCurrentTestRun(null, testMethod);

               if (shouldRun != null && !shouldRun) {
                  itr.remove();
               }
            }

            if (testClassDescription.getChildren().isEmpty()) {
               return false;
            }
         }
         else if (m instanceof FrameworkMethod) {
            testMethod = ((FrameworkMethod) m).getMethod();
            Boolean shouldRun = shouldRunTestInCurrentTestRun(Test.class, testMethod);

            if (shouldRun != null) {
               return shouldRun;
            }
         }
      }

      Boolean shouldRun = Utilities.invoke(it, shouldRunMethod, m);

      if (testMethod != null) {
         testMethods.put(testMethod, shouldRun);
      }

      return shouldRun;
   }

   private Boolean shouldRunTestInCurrentTestRun(
      Class<? extends Annotation> testAnnotation, Method testMethod)
   {
      Boolean shouldRun = testMethods.get(testMethod);

      if (shouldRun != null) {
         return shouldRun;
      }

      if (isTestNotApplicableInCurrentTestRun(testAnnotation, testMethod)) {
         reportTestAsNotApplicableInCurrentTestRun(testMethod);
         testMethods.put(testMethod, false);
         return false;
      }

      return null;
   }

   private boolean isTestNotApplicableInCurrentTestRun(
      Class<? extends Annotation> testAnnotation, Method testMethod)
   {
      return 
         (testAnnotation == null || testMethod.getAnnotation(testAnnotation) != null) &&
         new TestFilter(coverageMap).shouldIgnoreTestInCurrentTestRun(testMethod);
   }

   private void reportTestAsNotApplicableInCurrentTestRun(Method method)
   {
      Class<?> testClass = method.getDeclaringClass();
      Description testDescription = Description.createTestDescription(testClass, method.getName());

      runNotifier.fireTestStarted(testDescription);
      runNotifier.fireTestFinished(testDescription);
   }
}
