/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.*;
import java.util.concurrent.*;

final class MultiCoreTestRunner
{
   static void runUsingAvailableCores(List<Callable<Void>> tasks)
   {
      int numCores = Runtime.getRuntime().availableProcessors();
      ExecutorService executor = Executors.newFixedThreadPool(numCores);

      try { executor.invokeAll(tasks); } catch (InterruptedException ignore) {}

      executor.shutdownNow();
   }
}
