/*
 * JMockit Core
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
package mockit.internal.startup;

import java.io.*;
import java.net.*;
import java.security.*;

public final class AgentInitialization
{
   public void initializeAccordingToJDKVersion()
   {
      String jarFilePath = discoverPathToJarFile();

      if (Startup.jdk6OrLater) {
         try {
            Class.forName("com.sun.tools.attach.VirtualMachine");

            // The JDK6AgentLoader won't be loaded if this line is not executed, at least in
            // the Sun JDKs.
            new JDK6AgentLoader(jarFilePath).loadAgent();
         }
         catch (ClassNotFoundException e) {
            throw new IllegalStateException(
               "Please add <jdk_dir>/lib/tools.jar to the classpath, or specify -javaagent:" +
               jarFilePath + " in the command line for starting the JVM.", e);
         }
      }
      else if ("1.5".equals(Startup.javaSpecVersion)) {
         throw new IllegalStateException(
            "JMockit has not been initialized. Check that your Java 5 VM has been started " +
            "with the -javaagent:" + jarFilePath + " command line option.");
      }
      else {
         throw new IllegalStateException("JMockit requires a Java 5 VM or later.");
      }
   }

   private String discoverPathToJarFile()
   {
      CodeSource codeSource = AgentInitialization.class.getProtectionDomain().getCodeSource();

      if (codeSource == null) {
         return findPathToJarFileFromClasspath();
      }

      URI jarFileURI; // URI is needed to deal with spaces and non-ASCII characters

      try {
         jarFileURI = codeSource.getLocation().toURI();
      }
      catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }

      return new File(jarFileURI).getPath();
   }

   private String findPathToJarFileFromClasspath()
   {
      String[] classPath = System.getProperty("java.class.path").split(File.pathSeparator);

      for (String cpEntry : classPath) {
         if (cpEntry.matches(".*jmockit[-.\\d]*.jar")) {
            return cpEntry;
         }
      }

      return null;
   }
}
