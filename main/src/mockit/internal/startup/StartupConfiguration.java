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
   private static final String SEPARATOR_REGEX = "\\s*,\\s*|\\s+";

   private final Properties config;

   final String[] externalTools;
   String toolClassName;
   String toolArguments;

   final String[] classesToBeStubbedOut;
   final String[] mockClasses;

   StartupConfiguration() throws IOException
   {
      config = new Properties();

      loadJMockitPropertiesFilesFromClasspath();
      loadJMockitPropertiesIntoSystemProperties();

      externalTools = System.getProperty("jmockit-tools", "").split(SEPARATOR_REGEX);
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
            try { config.load(propertiesFile); } finally { propertiesFile.close(); }
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
         String existingValue = (String) config.get(key);
         String newValue;

         if (existingValue == null || existingValue.length() == 0) {
            newValue = valueToAdd;
         }
         else {
            newValue = existingValue + ' ' + valueToAdd;
         }

         config.put(key, newValue);
      }
   }

   private void loadJMockitPropertiesIntoSystemProperties()
   {
      Properties systemProperties = System.getProperties();

      for (Entry<?, ?> prop : config.entrySet()) {
         String name = (String) prop.getKey();

         if (name.startsWith("jmockit-") && !systemProperties.containsKey(name)) {
            systemProperties.put(name, prop.getValue());
         }
      }
   }

   void extractClassNameAndArgumentsFromToolSpecification(String toolSpec)
   {
      String[] classAndArgs = toolSpec.split("\\s*=\\s*");
      toolClassName = classAndArgs[0];
      toolArguments = classAndArgs.length == 1 ? null : classAndArgs[1];
   }

   @Override
   public String toString()
   {
      String toolArgsDescription = toolArguments == null ? "" : '=' + toolArguments;
      return toolClassName + toolArgsDescription;
   }
}
