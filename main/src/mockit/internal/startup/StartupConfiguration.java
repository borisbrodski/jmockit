/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

final class StartupConfiguration
{
   private static final String DEFAULT_TOOLS_KEY = "defaultTools";
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

      loadJMockitPropertiesFilesFromClasspath();
      loadJMockitPropertiesIntoSystemProperties();

      defaultTools = new ArrayList<String>();
      fillListOfDefaultTools();

      classesToBeStubbedOut = System.getProperty("jmockit-stubs", "").split(SEPARATOR_REGEX);
      mockClasses = System.getProperty("jmockit-mocks", "").split(SEPARATOR_REGEX);
   }

   private void loadJMockitPropertiesFilesFromClasspath() throws IOException
   {
      Enumeration<URL> allFiles = Thread.currentThread().getContextClassLoader().getResources("jmockit.properties");
      int numFiles = 0;

      while (allFiles.hasMoreElements()) {
         URL url = allFiles.nextElement();
         InputStream propertiesFile = url.openStream();

         if (numFiles == 0) {
            try { startupTools.load(propertiesFile); } finally { propertiesFile.close(); }
         }
         else {
            Properties properties = new Properties();
            try { properties.load(propertiesFile); } finally { propertiesFile.close(); }
            addPropertyValues(properties);
         }

         numFiles++;
      }
   }

   private void addPropertyValues(Properties propertiesToAdd)
   {
      for (Entry<?, ?> propertyToAdd : propertiesToAdd.entrySet()) {
         Object key = propertyToAdd.getKey();
         String valueToAdd = (String) propertyToAdd.getValue();
         String existingValue = (String) startupTools.get(key);
         String newValue;

         if (existingValue == null || existingValue.length() == 0) {
            newValue = valueToAdd;
         }
         else {
            newValue = existingValue + ' ' + valueToAdd;
         }

         startupTools.put(key, newValue);
      }
   }

   private void loadJMockitPropertiesIntoSystemProperties()
   {
      for (Entry<?, ?> prop : startupTools.entrySet()) {
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
         defaultToolsArray = startupTools.getProperty(DEFAULT_TOOLS_KEY).split("\\s+");
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
