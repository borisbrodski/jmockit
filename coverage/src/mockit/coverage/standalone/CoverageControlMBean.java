/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.standalone;

@SuppressWarnings("UnusedDeclaration")
@Description("Control interface for the JMockit Coverage tool running in this JVM instance")
public interface CoverageControlMBean
{
   @Description("Type of output to be generated: one or more of \"html\", \"serial\", or \"merge\"")
   String getOutput();
   void setOutput(String output);

   @Description("Output directory for the HTML report or the \"coverage.ser\" serialized file")
   String getOutputDir();
   void setOutputDir(String outputDir);

   @Description("Comma-separated list of directories where to search for source files, for the HTML report")
   String getSrcDirs();
   void setSrcDirs(String srcDirs);

   @Description(
      "Regular expression for fully qualified class names, to select those considered for coverage (none by default)")
   String getClasses();
   void setClasses(String classes);

   @Description("Regular expression for fully qualified class names, to select those NOT considered for coverage")
   String getExcludes();
   void setExcludes(String excludes);

   @Description("Code coverage metrics to be gathered: either \"all\" (default) or one or more of \"line\", \"path\"")
   String getMetrics();
   void setMetrics(String metrics);

   @Description("Generates the desired output with the coverage information gathered so far")
   void generateOutput(
      @Description("Indicates whether coverage data gathered so far should be discarded after generating the output")
      boolean resetState);
}