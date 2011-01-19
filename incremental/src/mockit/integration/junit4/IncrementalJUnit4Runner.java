/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import java.io.*;

import org.junit.internal.runners.*;
import org.junit.runner.Description;
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
@MockClass(realClass = ParentRunner.class, instantiation = Instantiation.PerMockSetup)
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
   }

   private final Properties coverageMap;
   private final Map<Method, Boolean> testMethods;
   private RunNotifier runNotifier;
   private Method testMethod;
   public ParentRunner<?> it;

   public IncrementalJUnit4Runner()
   {
      File testRunFile = new File("testRun.properties");

      if (testRunFile.exists() && !testRunFile.canWrite()) {
         throw new IllegalStateException();
      }

      coverageMap = new Properties();
      testMethods = new HashMap<Method, Boolean>();
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
      testMethod = null;

      if (!coverageMap.isEmpty()) {
         if (m instanceof JUnit38ClassRunner) {
            boolean noTestsToRun = verifyTestMethodsInJUnit38TestClassThatShouldRun((JUnit38ClassRunner) m);

            if (noTestsToRun) {
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

   private boolean verifyTestMethodsInJUnit38TestClassThatShouldRun(JUnit38ClassRunner runner)
   {
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

      return testClassDescription.getChildren().isEmpty();
   }

   private Boolean shouldRunTestInCurrentTestRun(Class<? extends Annotation> testAnnotation, Method testMethod)
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

   private boolean isTestNotApplicableInCurrentTestRun(Class<? extends Annotation> testAnnotation, Method testMethod)
   {
      return 
         (testAnnotation == null || testMethod.getAnnotation(testAnnotation) != null) &&
         new TestFilter(coverageMap).shouldIgnoreTestInCurrentTestRun(testMethod);
   }

   private static final class TestNotApplicable extends RuntimeException
   {
      private TestNotApplicable() { super("unaffected by changes since last test run"); }
      @Override public void printStackTrace(PrintWriter s) {}
   }

   private static final Throwable NOT_APPLICABLE = new TestNotApplicable();

   private void reportTestAsNotApplicableInCurrentTestRun(Method method)
   {
      Class<?> testClass = method.getDeclaringClass();
      Description testDescription = Description.createTestDescription(testClass, method.getName());

      runNotifier.fireTestAssumptionFailed(new Failure(testDescription, NOT_APPLICABLE));
   }
}
