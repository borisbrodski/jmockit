/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import java.lang.reflect.*;

import org.junit.runners.*;
import org.junit.runners.model.*;

import mockit.*;
import mockit.integration.internal.*;
import mockit.internal.state.*;

/**
 * Startup mock which works in conjunction with {@link JUnit4TestRunnerDecorator} to provide JUnit 4.5+ integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = BlockJUnit4ClassRunner.class, instantiation = Instantiation.PerMockSetup)
public final class BlockJUnit4ClassRunnerDecorator
{
   private static final Method createTest;
   private static final Method getTestClass;

   static ThreadLocal<Object> currentTestThreadLocal = new ThreadLocal<Object>();
   static
   {
      Method getTestClassMethod;

      try {
         getTestClassMethod = ParentRunner.class.getDeclaredMethod("getTestClass");
         createTest = BlockJUnit4ClassRunner.class.getDeclaredMethod("createTest");
      }
      catch (NoSuchMethodException e) { throw new RuntimeException(e); }
      
      createTest.setAccessible(true);

      if (getTestClassMethod.isAccessible()) {
         getTestClass = null;
      }
      else {
         getTestClassMethod.setAccessible(true);
         getTestClass = getTestClassMethod;
      }
   }

   public BlockJUnit4ClassRunner it;

   @Mock(reentrant = true)
   public Object createTest() throws Throwable
   {
      TestRun.enterNoMockingZone();

      try {
         TestClass junitTestClass = getTestClass == null ? it.getTestClass() : (TestClass) getTestClass.invoke(it);
         Class<?> newTestClass = junitTestClass.getJavaClass();
         Class<?> currentTestClass = TestRun.getCurrentTestClass();

         if (currentTestClass != null && !currentTestClass.isAssignableFrom(newTestClass)) {
            TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
         }

         Object test = createTest.invoke(it);
         currentTestThreadLocal.set(test);
         return test;
      }
      catch (InvocationTargetException e) {
         throw e.getCause();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
   
   static Object getCurrentTest() {
      return currentTestThreadLocal.get();
   }
}
