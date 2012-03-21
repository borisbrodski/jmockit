/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.standalone;

import java.lang.management.*;
import javax.management.*;

import mockit.coverage.*;

public final class CoverageControl implements CoverageControlMBean
{
   private static final String PROPERTY_PREFIX = "jmockit-coverage-";

   static void create(CodeCoverage coverage)
   {
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
      CoverageControl mxBean = new CoverageControl(coverage);

      try {
         mbeanServer.registerMBean(mxBean, new ObjectName("mockit.coverage.standalone:type=CoverageControl"));
      }
      catch (JMException e) {
         throw new RuntimeException(e);
      }
   }

   private final CodeCoverage coverage;

   public CoverageControl(CodeCoverage coverage) { this.coverage = coverage; }

   public String getOutput() { return getConfigurationProperty("output"); }
   public void setOutput(String output) { setConfigurationProperty("output", output); }

   public String getOutputDir() { return getConfigurationProperty("outputDir"); }
   public void setOutputDir(String outputDir) { setConfigurationProperty("outputDir", outputDir); }

   public String getSrcDirs() { return getConfigurationProperty("srcDirs"); }
   public void setSrcDirs(String srcDirs) { setConfigurationProperty("srcDirs", srcDirs); }

   public String getClasses() { return getConfigurationProperty("classes"); }
   public void setClasses(String classes) { setConfigurationProperty("classes", classes); }

   public String getExcludes() { return getConfigurationProperty("excludes"); }
   public void setExcludes(String excludes) { setConfigurationProperty("excludes", excludes); }

   public String getMetrics() { return getConfigurationProperty("metrics"); }
   public void setMetrics(String metrics) { setConfigurationProperty("metrics", metrics); }

   private String getConfigurationProperty(String propertyName)
   {
      return System.getProperty(PROPERTY_PREFIX + propertyName);
   }

   private void setConfigurationProperty(String propertyName, String propertyValue)
   {
      System.setProperty(PROPERTY_PREFIX + propertyName, propertyValue);
   }

   public void generateOutput(boolean resetState)
   {
      coverage.generateOutput();
   }
}
