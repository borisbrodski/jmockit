/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import mockit.coverage.standalone.*;

public enum Metrics
{
   LineCoverage
   {
      @Override
      public boolean isActive() { return isActive("line"); }
   },

   PathCoverage
   {
      @Override
      public boolean isActive() { return isActive("path"); }
   },

   DataCoverage
   {
      @Override
      public boolean isActive() { return !Startup.isStandalone() && isActive("data"); }
   };

   public abstract boolean isActive();

   final boolean isActive(String name)
   {
      String metrics = System.getProperty("jmockit-coverage-metrics", "all");
      boolean all = "all".equals(metrics);
      return all || metrics.contains(name);
   }

   public static int amountActive()
   {
      int amount = 0;

      for (Metrics metric : Metrics.values()) {
         if (metric.isActive()) {
            amount++;
         }
      }

      return amount;
   }
}
