/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4.internal;

import org.junit.runner.notification.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.internal.*;
import mockit.internal.state.*;

/**
 * Startup mock which works in conjunction with {@link JUnit4TestRunnerDecorator} to provide JUnit 4.5+ integration.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
@MockClass(realClass = RunNotifier.class, instantiation = Instantiation.PerMockSetup)
public final class RunNotifierDecorator
{
   public RunNotifier it;

   @Mock(reentrant = true)
   public void fireTestRunFinished(Result result)
   {
      TestRun.enterNoMockingZone();

      try {
         TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
         it.fireTestRunFinished(result);
      }
      finally {
         TestRun.exitNoMockingZone();
      }
   }
}
