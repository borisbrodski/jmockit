/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

public final class Metrics
{
   public static final boolean LINE_COVERAGE;
   public static final boolean PATH_COVERAGE;
   public static final boolean DATA_COVERAGE;

   static
   {
      String metrics = System.getProperty("jmockit-coverage-metrics", "all");
      boolean all = "all".equals(metrics);

      LINE_COVERAGE = all || metrics.contains("line");
      PATH_COVERAGE = all || metrics.contains("path");
      DATA_COVERAGE = all || metrics.contains("data");
   }

   public static boolean withMetric(int metric)
   {
      return
         LINE_COVERAGE && metric == 0 ||
         PATH_COVERAGE && metric == 1 ||
         DATA_COVERAGE && metric == 2;
   }

   public static int amountActive()
   {
      return (LINE_COVERAGE ? 1 : 0) + (PATH_COVERAGE ? 1 : 0) + (DATA_COVERAGE ? 1 : 0);
   }
}
