/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.lang.instrument.*;

public final class Startup
{
   private static Instrumentation instrumentation;
   private static boolean standalone;

   public static void premain(String agentArgs, Instrumentation inst)
   {
      standalone = true;
      instrumentation = inst;
      inst.addTransformer(new CodeCoverage());
   }

   public static Instrumentation instrumentation()
   {
      if (instrumentation == null) {
         instrumentation = mockit.internal.startup.Startup.instrumentation();
      }

      return instrumentation;
   }

   public static boolean isStandalone() { return standalone; }
}
