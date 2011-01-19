/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import org.junit.internal.runners.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import org.junit.runners.*;
import org.junit.runners.model.TestClass;

import mockit.*;

final class JUnitTestClassRunnerTask extends TestClassRunnerTask
{
   private final RunNotifier runNotifier;
   private final Runner classRunner;

   JUnitTestClassRunnerTask(RunNotifier runNotifier, Runner classRunner)
   {
      this.runNotifier = runNotifier;
      this.classRunner = classRunner;
   }

   @SuppressWarnings({"deprecation"})
   @Override
   Class<?> getTestClassFromRunner()
   {
      if (classRunner instanceof ParentRunner<?>) {
         TestClass testClassInfo = ((ParentRunner<?>) classRunner).getTestClass();
         return testClassInfo.getJavaClass();
      }
      else if (classRunner instanceof JUnit4ClassRunner) {
         org.junit.internal.runners.TestClass testClassInfo = Deencapsulation.invoke(classRunner, "getTestClass");
         return testClassInfo.getJavaClass();
      }

      return null;
   }

   @Override
   void executeCopyOfTestClass(Class<?> testClass) throws Exception
   {
      Deencapsulation.setField(classRunner, "fTestClass", new TestClass(testClass));
      executeOriginalTestClass();
   }

   @Override
   void executeOriginalTestClass()
   {
      classRunner.run(runNotifier);
   }
}
