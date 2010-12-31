/*
 * JMockit
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
package mockit.internal.startup;

import java.io.*;
import java.lang.instrument.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.integration.junit3.internal.*;
import mockit.integration.junit4.internal.*;
import mockit.integration.testng.internal.*;
import mockit.internal.*;
import mockit.internal.expectations.transformation.*;
import mockit.internal.state.*;

/**
 * This is the "agent class" that initializes the JMockit "Java agent". It is not intended for use
 * in client code. It must be public, however, so the JVM can call the <code>premain</code> method,
 * which as the name implies is called <strong>before</strong> the <code>main</main> method.
 *
 * @see #premain(String, Instrumentation)
 */
public final class Startup
{
   static final String javaSpecVersion = System.getProperty("java.specification.version");
   static final boolean jdk6OrLater = "1.6".equals(javaSpecVersion) || "1.7".equals(javaSpecVersion);

   private static Instrumentation instrumentation;
   private static boolean initializedOnDemand;
   private static final Properties startupTools = new Properties();
   private static final List<String> defaultTools = new ArrayList<String>();

   static
   {
      InputStream properties = Startup.class.getResourceAsStream("/jmockit.properties");

      try {
         startupTools.load(properties);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
      finally {
         try { properties.close(); } catch (IOException ignore) {}
      }

      String[] defaultToolsArray;
      String specifiedTools = System.getProperty("jmockit-tools");

      if (specifiedTools != null) {
         defaultToolsArray = specifiedTools.split(",");
      }
      else {
         defaultToolsArray = startupTools.getProperty("defaultTools", "").split("\\s+");
      }

      Collections.addAll(defaultTools, defaultToolsArray);
   }

   private Startup() {}

   public static boolean isJava6OrLater() { return jdk6OrLater; }

   /**
    * This method must only be called by the JVM, to provide the instrumentation object. In order
    * for this to occur, the JVM must be started with "-javaagent:jmockit.jar" as a command line
    * parameter (assuming the jar file is in the current directory).
    * <p/>
    * It is also possible to load other <strong>instrumentation tools</strong> at this time,
    * according to any agent arguments provided as "-javaagent:jmockit.jar=agentArgs" in the JVM
    * command line. There are two types of instrumentation tools: <ol> <li>A {@link
    * ClassFileTransformer class file transformer}, which will be instantiated and added to the JVM
    * instrumentation service. Such a class must be public and have a public constructor accepting
    * two parameters: the first of type <code>Map&lt;String, byte[]></code>, which will receive a
    * map for storing the transformed classes; and the second of type <code>String</code>, which
    * will receive any tool arguments. </li> <li>An <strong>external mock</strong>, which can be any
    * class with a public no-args constructor. Such a class will be used to redefine one or more
    * real classes. The real classes can be specified in one of two ways: by providing a regular
    * expression matching class names as the tool arguments, or by annotating the external mock
    * class with {@link mockit.MockClass}.</li> </ol>
    * <p/>
    * If you encounter a {@link VerifyError} in some test, try running it with the
    * <code>-noverify</code> JVM parameter.
    *
    * @param agentArgs zero or more <strong>instrumentation tool specifications</strong> (separated
    *                  by semicolons if more than one); each tool specification must be expressed as
    *                  "&lt;tool class name>[=tool arguments]", where the class names are fully
    *                  qualified, and the corresponding class files must be present in the
    *                  classpath; the part between square brackets is optional
    * @param inst      the instrumentation service provided by the JVM
    */
   public static void premain(String agentArgs, Instrumentation inst) throws Exception
   {
      initialize(agentArgs, inst);
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static void agentmain(String agentArgs, Instrumentation inst) throws Exception
   {
      initialize(agentArgs, inst);
   }

   private static void initialize(String agentArgs, Instrumentation inst) throws IOException
   {
      instrumentation = inst;

      prepareMockingBridgeForUse();
      loadInternalStartupMocks();

      if (agentArgs != null && agentArgs.length() > 0) {
         processAgentArgs(agentArgs);
      }

      for (String toolSpec : defaultTools) {
         loadExternalTool(toolSpec, true);
      }

      inst.addTransformer(new ProxyRegistrationTransformer());
      inst.addTransformer(new ExpectationsTransformer(inst));
   }

   private static void prepareMockingBridgeForUse()
   {
      // Register an instance to be recovered in the generated bytecode of a modified JRE class.
      Properties mb = new MockingBridge();
      mb.putAll(System.getProperties());
      System.setProperties(mb);

      // Pre-load certain JMockit classes to avoid NoClassDefFoundError's when mocking certain JRE classes,
      // such as ArrayList.
      try {
         Class.forName("mockit.Delegate");
         Class.forName("mockit.internal.expectations.invocation.InvocationResults");
      }
      catch (ClassNotFoundException ignore) {}
   }

   private static void loadInternalStartupMocks()
   {
      setUpInternalStartupMock(TestSuiteDecorator.class);
      setUpInternalStartupMock(JUnitTestCaseDecorator.class);

      setUpInternalStartupMock(RunNotifierDecorator.class);
      setUpInternalStartupMock(BlockJUnit4ClassRunnerDecorator.class);

      setUpInternalStartupMock(TestNGTestRunnerDecorator.class);

      TestRun.mockFixture().turnRedefinedClassesIntoFixedOnes();
   }

   private static void setUpInternalStartupMock(Class<?> mockClass)
   {
      Object mock;

      //noinspection CatchGenericClass,OverlyBroadCatchBlock
      try {
         //noinspection ClassNewInstance
         mock = mockClass.newInstance();
      }
      catch (Throwable ignored) {
         // OK, simply ignore the internal startup mock if it can't be loaded for some reason,
         // such as not having the necessary third-party class files in the classpath.
         return;
      }

      new RedefinitionEngine(mock, mockClass, true).setUpStartupMock();
   }

   private static void processAgentArgs(String agentArgs) throws IOException
   {
      String[] externalToolSpecs = agentArgs.split("\\s*;\\s*");

      for (String toolSpec : externalToolSpecs) {
         loadExternalTool(toolSpec, false);
      }
   }

   private static void loadExternalTool(String toolSpec, boolean byDefault) throws IOException
   {
      String[] classAndArgs = toolSpec.split("\\s*=\\s*");
      String toolClassName = classAndArgs[0];
      String toolArgs = classAndArgs.length == 1 ? null : classAndArgs[1];

      if (!byDefault) {
         defaultTools.remove(toolClassName);
      }

      String toolKey = "startupTools." + toolClassName;
      
      if (startupTools.containsKey(toolKey)) {
         toolClassName = startupTools.getProperty(toolKey);
      }

      ClassReader cr;

      if (byDefault) {
         try {
            cr = ClassFile.readClass(toolClassName);
         }
         catch (IOException ignore) {
            // OK, don't load if not in the classpath.
            return;
         }
      }
      else {
         cr = ClassFile.readClass(toolClassName);
      }

      loadExternalTool(toolClassName, toolArgs, cr);
   }

   private static void loadExternalTool(String toolClassName, String toolArgs, ClassReader cr)
   {
      ToolLoader toolLoader = new ToolLoader(toolClassName, toolArgs);

      try {
         cr.accept(toolLoader, true);
      }
      catch (IllegalStateException ignore) {
         return;
      }

      String toolArgsDescription = toolArgs == null ? "" : '=' + toolArgs;
      System.out.println("JMockit: loaded external tool " + toolClassName + toolArgsDescription);
   }

   public static Instrumentation instrumentation()
   {
      verifyInitialization();
      return instrumentation;
   }

   public static boolean wasInitializedOnDemand() { return initializedOnDemand; }

   public static void verifyInitialization()
   {
      if (instrumentation == null) {
         new AgentInitialization().initializeAccordingToJDKVersion();
         initializedOnDemand = true;
         System.out.println(
            "WARNING: JMockit was initialized on demand, which may cause certain tests to fail;\n" +
            "please check the documentation for better ways to get it initialized.");
      }
   }

   public static void initializeIfNeeded()
   {
      if (instrumentation == null) {
         try {
            new AgentInitialization().initializeAccordingToJDKVersion();
         }
         catch (RuntimeException e) {
            e.printStackTrace(); // makes sure the exception gets printed at least once
            throw e;
         }
      }
   }
}
