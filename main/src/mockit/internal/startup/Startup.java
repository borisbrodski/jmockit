/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;
import java.lang.instrument.*;

import mockit.external.asm4.*;
import mockit.integration.junit3.internal.*;
import mockit.integration.junit4.internal.*;
import mockit.integration.testng.internal.*;
import mockit.internal.*;
import mockit.internal.expectations.transformation.*;
import mockit.internal.util.*;

/**
 * This is the "agent class" that initializes the JMockit "Java agent". It is not intended for use in client code.
 * It must be public, however, so the JVM can call the {@code premain} method, which as the name implies is called
 * <em>before</em> the {@code main} method.
 *
 * @see #premain(String, Instrumentation)
 */
public final class Startup
{
   static final String javaSpecVersion = System.getProperty("java.specification.version");
   static final boolean jdk6OrLater =
      "1.6".equals(javaSpecVersion) || "1.7".equals(javaSpecVersion) || "1.8".equals(javaSpecVersion);

   private static final String[] NO_STUBBING_FILTERS = {};

   private static Instrumentation instrumentation;
   private static boolean initializedOnDemand;

   private Startup() {}

   public static boolean isJava6OrLater() { return jdk6OrLater; }

   /**
    * This method must only be called by the JVM, to provide the instrumentation object.
    * In order for this to occur, the JVM must be started with "-javaagent:jmockit.jar" as a command line parameter
    * (assuming the jar file is in the current directory).
    * <p/>
    * It is also possible to load other <em>instrumentation tools</em> at this time, according to any agent
    * arguments provided as "-javaagent:jmockit.jar=agentArgs" in the JVM command line.
    * There are two types of instrumentation tools:
    * <ol>
    * <li>A {@link ClassFileTransformer class file transformer}, which will be instantiated and added to the JVM
    * instrumentation service. Such a class must be public and have a public constructor accepting two parameters: the
    * first of type {@code Map&lt;String, byte[]>}, which will receive a map for storing the transformed classes; and
    * the second of type {@code String}, which will receive any tool arguments.</li>
    * <li>An <em>external mock</em>, which can be any class with a public no-args constructor.
    * Such a class will be used to redefine one or more real classes.
    * The real classes can be specified in one of two ways: by providing a regular expression matching class names as
    * the tool arguments, or by annotating the external mock class with {@link mockit.MockClass}.</li>
    * </ol>
    *
    * @param agentArgs zero or more <em>instrumentation tool specifications</em> (separated by semicolons if more than
    *                  one); each tool specification must be expressed as "&lt;tool class name>[=tool arguments]", with
    *                  fully qualified class names for classes available in the classpath; tool arguments are optional
    * @param inst      the instrumentation service provided by the JVM
    */
   public static void premain(String agentArgs, Instrumentation inst) throws Exception
   {
      initialize(true, inst);
   }

   @SuppressWarnings("UnusedDeclaration")
   public static void agentmain(String agentArgs, Instrumentation inst) throws Exception
   {
      initialize(false, inst);
   }

   private static void initialize(boolean initializeTestNG, Instrumentation inst) throws IOException
   {
      instrumentation = inst;

      StartupConfiguration config = new StartupConfiguration();

      MockingBridge.preventEventualClassLoadingConflicts();
      loadInternalStartupMocksForJUnitIntegration();

      if (initializeTestNG) {
         try { setUpInternalStartupMock(MockTestNG.class); } catch (Error ignored) {}
      }

      for (String toolSpec : config.externalTools) {
         loadExternalTool(config, toolSpec);
      }

      stubOutClassesIfSpecifiedInSystemProperty(config.classesToBeStubbedOut);
      setUpStartupMocks(config.mockClasses);

      inst.addTransformer(new JMockitTransformer());
      inst.addTransformer(new ExpectationsTransformer(inst));
   }

   private static void loadInternalStartupMocksForJUnitIntegration()
   {
      if (setUpInternalStartupMock(TestSuiteDecorator.class)) {
         try {
            setUpInternalStartupMock(JUnitTestCaseDecorator.class);
         }
         catch (VerifyError ignore) {
            // For some reason, this error occurs when running TestNG tests from Maven.
         }

         setUpInternalStartupMock(RunNotifierDecorator.class);
         setUpInternalStartupMock(JUnit4TestRunnerDecorator.class);
      }
   }

   private static boolean setUpInternalStartupMock(Class<?> mockClass)
   {
      try {
         new RedefinitionEngine(null, mockClass).setUpStartupMock();
         return true;
      }
      catch (TypeNotPresentException ignore) {
         // OK, ignore the startup mock if the necessary third-party class files are not in the classpath.
         return false;
      }
   }

   private static void loadExternalTool(StartupConfiguration config, String toolSpec)
   {
      config.extractClassNameAndArgumentsFromToolSpecification(toolSpec);

      ClassReader cr;

      try {
         cr = ClassFile.readClass(config.toolClassName);
      }
      catch (IOException ignore) {
         System.out.println("JMockit: external tool class \"" + config.toolClassName + "\" not available in classpath");
         return;
      }

      loadExternalTool(config, cr);
   }

   private static void loadExternalTool(StartupConfiguration config, ClassReader cr)
   {
      ToolLoader toolLoader = new ToolLoader(config.toolClassName, config.toolArguments);

      try {
         cr.accept(toolLoader, ClassReader.SKIP_DEBUG);
      }
      catch (IllegalStateException ignore) {
         return;
      }

      System.out.println("JMockit: loaded external tool " + config);
   }

   private static void stubOutClassesIfSpecifiedInSystemProperty(Iterable<String> classesToStubOut)
   {
      for (String stubbing : classesToStubOut) {
         int p = stubbing.indexOf('#');
         String realClassName = stubbing;
         String[] filters = NO_STUBBING_FILTERS;

         if (p > 0) {
            realClassName = stubbing.substring(0, p);
            filters = stubbing.substring(p + 1).split("\\|");
         }

         Class<?> realClass = Utilities.loadClass(realClassName.trim());
         new RedefinitionEngine(realClass, true, filters).stubOutAtStartup();
      }
   }

   private static void setUpStartupMocks(Iterable<String> mockClasses)
   {
      for (String mockClassName : mockClasses) {
         Class<?> mockClass = Utilities.loadClass(mockClassName);
         new RedefinitionEngine(null, mockClass).setUpStartupMock();
      }
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

   public static boolean initializeIfNeeded()
   {
      if (instrumentation == null) {
         try {
            new AgentInitialization().initializeAccordingToJDKVersion();
            return true;
         }
         catch (RuntimeException e) {
            e.printStackTrace(); // makes sure the exception gets printed at least once
            throw e;
         }
      }

      return false;
   }

   public static void initializeIfPossible()
   {
      if (jdk6OrLater) {
         initializeIfNeeded();
      }
   }
}
