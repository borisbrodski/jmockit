/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import java.lang.reflect.*;

import org.junit.runners.*;

import mockit.*;
import mockit.integration.internal.*;
import mockit.internal.state.*;

/**
 * Startup mock which works in conjunction with {@link JUnit4TestRunnerDecorator} to provide JUnit 4.5+ integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = BlockJUnit4ClassRunner.class)
public final class BlockJUnit4ClassRunnerDecorator
{
   private static final Method createTestMethod;

   static
   {
      try {
         createTestMethod = BlockJUnit4ClassRunner.class.getDeclaredMethod("createTest");
      }
      catch (NoSuchMethodException e) { throw new RuntimeException(e); }
      
      createTestMethod.setAccessible(true);
   }

   @Mock(reentrant = true)
   public static Object createTest(Invocation invocation) throws Throwable
   {
      TestRun.enterNoMockingZone();

      try {
         BlockJUnit4ClassRunner it = invocation.getInvokedInstance();
         Class<?> newTestClass = it.getTestClass().getJavaClass();
         Class<?> currentTestClass = TestRun.getCurrentTestClass();

         if (currentTestClass != null && !currentTestClass.isAssignableFrom(newTestClass)) {
            TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
         }

         return createTestMethod.invoke(it);
      }
      catch (InvocationTargetException e) {
         throw e.getCause();
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
