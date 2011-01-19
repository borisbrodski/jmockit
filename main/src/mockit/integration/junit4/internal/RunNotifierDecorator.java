/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import org.junit.runner.notification.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Startup mock which works in conjunction with {@link JUnit4TestRunnerDecorator} to provide JUnit 4.5+ integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = RunNotifier.class, instantiation = Instantiation.PerMockedInstance)
public final class RunNotifierDecorator
{
   public RunNotifier it;

   @Mock(reentrant = true)
   public void fireTestStarted(Description description)
   {
      Class<?> currentTestClass = TestRun.getCurrentTestClass();

      if (currentTestClass != null) {
         String testClassName = getTestClassName(description);

         if (!testClassName.equals(currentTestClass.getName())) {
            Class<?> testClass = Utilities.loadClass(testClassName);
            discardCurrentTestClassIfNoLongerValid(testClass);
         }
      }

      it.fireTestStarted(description);
   }

   private String getTestClassName(Description description)
   {
      String testClassName = description.getDisplayName();
      int p = testClassName.indexOf('(');
      return testClassName.substring(p + 1, testClassName.length() - 1);
   }

   private void discardCurrentTestClassIfNoLongerValid(Class<?> testClass)
   {
      Class<?> currentTestClass = TestRun.getCurrentTestClass();

      if (!currentTestClass.isAssignableFrom(testClass)) {
         TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
         TestRun.setCurrentTestClass(null);
      }
   }
}
