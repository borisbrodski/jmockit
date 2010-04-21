/*
 * JMockit Coverage
 * Copyright (c) 2006-2010 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
