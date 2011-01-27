/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import junit.framework.*;
import static mockit.multicore.Utilities.*;
import org.junit.internal.runners.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import org.junit.runners.*;
import org.junit.runners.model.TestClass;

@SuppressWarnings({"deprecation"})
final class JUnitTestClassRunnerTask extends TestClassRunnerTask
{
   private final RunNotifier runNotifier;
   private final Runner classRunner;

   JUnitTestClassRunnerTask(RunNotifier runNotifier, Runner classRunner)
   {
      this.runNotifier = runNotifier;
      this.classRunner = classRunner;
   }

   @Override
   String getTestClassName()
   {
      if (classRunner instanceof ParentRunner<?>) {
         TestClass testClassInfo = ((ParentRunner<?>) classRunner).getTestClass();
         return testClassInfo.getJavaClass().getName();
      }
      else if (classRunner instanceof JUnit38ClassRunner) {
         TestSuite testClassInfo = invoke(classRunner, "getTest");
         return testClassInfo.getName();
      }
      else if (classRunner instanceof JUnit4ClassRunner) {
         org.junit.internal.runners.TestClass testClassInfo = invoke(classRunner, "getTestClass");
         return testClassInfo.getJavaClass().getName();
      }

      return null;
   }

   @Override
   void prepareCopyOfTestClassForExecution(Class<?> testClass) throws Exception
   {
      if (classRunner instanceof ParentRunner<?>) {
         setField(ParentRunner.class, classRunner, "fTestClass", new TestClass(testClass));
      }
      else if (classRunner instanceof JUnit38ClassRunner) {
         TestSuite testSuite = new TestSuite(testClass.asSubclass(TestCase.class));
         setField(classRunner, "fTest", testSuite);
      }
      else {
         org.junit.internal.runners.TestClass testClassInfo = new org.junit.internal.runners.TestClass(testClass);
         setField(classRunner, "fTestClass", testClassInfo);
         setField(classRunner, "fTestMethods", testClassInfo.getTestMethods());
      }
   }

   @Override
   void executeTestClass()
   {
      classRunner.run(runNotifier);
   }
}
