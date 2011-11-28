/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;
import java.util.*;

final class StartupConfiguration
{
   private static final String DEFAULT_TOOLS_KEY = "defaultTools";
   private static final String DEFAULT_TOOLS_VALUE =
      "mockit.coverage.CodeCoverage " +
      "mockit.integration.junit4.IncrementalJUnit4Runner " +
      "mockit.emulation.hibernate3.ConfigurationEmul";
   private static final String STARTUP_TOOL_PREFIX = "startupTools.";
   private static final String SEPARATOR_REGEX = "\\s*,\\s*|\\s+";

   private final Properties startupTools;
   final List<String> defaultTools;
   String toolClassName;
   String toolArguments;

   final String[] classesToBeStubbedOut;
   final String[] mockClasses;

   StartupConfiguration() throws IOException
   {
      startupTools = new Properties();
      loadPropertiesFile();
      loadSystemProperties();

      defaultTools = new ArrayList<String>();
      fillListOfDefaultTools();

      classesToBeStubbedOut = System.getProperty("jmockit-stubs", "").split(SEPARATOR_REGEX);
      mockClasses = System.getProperty("jmockit-mocks", "").split(SEPARATOR_REGEX);
   }

   private void loadPropertiesFile() throws IOException
   {
      // TODO: use Thread.currentThread().getContextClassLoader().getResources("/jmockit.properties")
      // to support multiple properties files

      InputStream properties = getClass().getResourceAsStream("/jmockit.properties");

      try {
         startupTools.load(properties);
      }
      finally {
         properties.close();
      }
   }

   private void loadSystemProperties()
   {
      for (Map.Entry<?, ?> prop : startupTools.entrySet()) {
         String name = (String) prop.getKey();

         if (!DEFAULT_TOOLS_KEY.equals(name) && !name.startsWith(STARTUP_TOOL_PREFIX)) {
            addToSystemProperties(name, prop.getValue());
         }
      }
   }

   private void addToSystemProperties(String name, Object value)
   {
      String sysPropName = name.startsWith("jmockit-") ? name : "jmockit-" + name;
      Properties systemProperties = System.getProperties();

      if (!systemProperties.containsKey(sysPropName)) {
         systemProperties.put(sysPropName, value);
      }
   }

   private void fillListOfDefaultTools()
   {
      String specifiedTools = System.getProperty("jmockit-tools");
      String[] defaultToolsArray;

      if (specifiedTools == null) {
         defaultToolsArray = startupTools.getProperty(DEFAULT_TOOLS_KEY, DEFAULT_TOOLS_VALUE).split("\\s+");
      }
      else {
         defaultToolsArray = specifiedTools.split(",");
      }

      Collections.addAll(defaultTools, defaultToolsArray);
   }

   void extractClassNameAndArgumentsFromToolSpecification(String toolSpec, boolean byDefault)
   {
      String[] classAndArgs = toolSpec.split("\\s*=\\s*");
      toolClassName = classAndArgs[0];
      toolArguments = classAndArgs.length == 1 ? null : classAndArgs[1];

      if (!byDefault) {
         defaultTools.remove(toolClassName);
      }

      String toolKey = STARTUP_TOOL_PREFIX + toolClassName;

      if (startupTools.containsKey(toolKey)) {
         toolClassName = startupTools.getProperty(toolKey);
      }
   }

   @Override
   public String toString()
   {
      String toolArgsDescription = toolArguments == null ? "" : '=' + toolArguments;
      return toolClassName + toolArgsDescription;
   }
}
