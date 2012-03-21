/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.standalone;

public interface CoverageControlMBean
{
   String getOutput();
   void setOutput(String output);

   String getOutputDir();
   void setOutputDir(String outputDir);

   String getSrcDirs();
   void setSrcDirs(String srcDirs);

   String getClasses();
   void setClasses(String classes);

   String getExcludes();
   void setExcludes(String excludes);

   String getMetrics();
   void setMetrics(String metrics);

   void generateOutput(boolean resetState);
}
