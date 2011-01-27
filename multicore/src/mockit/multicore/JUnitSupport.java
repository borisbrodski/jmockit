/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.*;
import java.util.concurrent.*;

import org.junit.runner.*;
import org.junit.runner.notification.*;
import org.junit.runners.*;

public final class JUnitSupport
{
   private final List<Runner> classRunners;

   public JUnitSupport(Runner runner)
   {
      if (runner instanceof Suite && Runtime.getRuntime().availableProcessors() > 1) {
         List<Runner> runners = Utilities.invoke(runner, "getChildren");

         if (runners.size() > 1) {
            classRunners = runners;
            return;
         }
      }

      classRunners = null;
   }

   public boolean runTestClasses(RunNotifier runNotifier)
   {
      if (classRunners == null) {
         return false;
      }

      List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(classRunners.size());

      for (Runner classRunner : classRunners) {
         tasks.add(new JUnitTestClassRunnerTask(runNotifier, classRunner));
      }

      MultiCoreTestRunner.runUsingAvailableCores(tasks);
      return true;
   }
}
