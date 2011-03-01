/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import static mockit.Deencapsulation.*;
import static mockit.Mockit.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.internal.state.*;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ClassWithTooManyMethods"})
public final class MockAnnotationsTest
{
   // The "code under test" for the tests in this class ///////////////////////////////////////////////////////////////

   private final CodeUnderTest codeUnderTest = new CodeUnderTest();
   private boolean mockExecuted;

   static class CodeUnderTest
   {
      private final Collaborator dependency = new Collaborator();

      void doSomething()
      {
         dependency.provideSomeService();
      }

      int performComputation(int a, boolean b)
      {
         int i = dependency.getValue();
         List<?> results = dependency.complexOperation(a, i);

         if (b) {
            dependency.setValue(i + results.size());
         }

         return i;
      }
   }

   @SuppressWarnings({"UnusedDeclaration"})
   static class Collaborator
   {
      static Object xyz;
      private int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      private static String doInternal() { return "123"; }

      void provideSomeService()
      {
         throw new RuntimeException("Real provideSomeService() called");
      }

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }

      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         return input1 == null ? Collections.emptyList() : Arrays.asList(otherInputs);
      }

      final void simpleOperation(int a, String b, Date c) {}
   }

   // Mocks without expectations //////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void mockWithNoExpectationsPassingMockClass()
   {
      Mockit.setUpMocks(MockCollaborator1.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaborator1
   {
      @Mock
      void provideSomeService() {}
   }

   @Test
   public void mockWithNoExpectationsPassingMockInstance()
   {
      Mockit.setUpMocks(new MockCollaborator1());

      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMockForSingleClassPassingAnnotatedMockInstance()
   {
      Mockit.setUpMock(new MockCollaborator1());

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   public static class MockCollaborator6
   {
      @Mock
      int getValue() { return 1; }
   }

   @Test
   public void setUpMockForSingleRealClassByPassingTheMockClassLiteral()
   {
      setUpMock(MockCollaborator6.class);

      assertEquals(1, new Collaborator().getValue());
   }

   @Test
   public void setUpMockForSingleRealClassByPassingAMockClassInstance()
   {
      setUpMock(new MockCollaborator6());

      assertEquals(1, new Collaborator().getValue());
   }

   @Test
   public void setUpStubs()
   {
      Mockit.setUpMocksAndStubs(Collaborator.class);

      codeUnderTest.doSomething();
   }

   static class MockCollaborator2
   {
      @Mock
      void provideSomeService() {}
   }

   @Test
   public void setUpMockForGivenRealClass()
   {
      Mockit.setUpMock(Collaborator.class, MockCollaborator2.class);

      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMockForRealClassByName()
   {
      Mockit.setUpMock(Collaborator.class.getName(), MockCollaborator2.class);

      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMockForGivenRealClassPassingMockInstance()
   {
      Mockit.setUpMock(Collaborator.class, new MockCollaborator2());

      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMockForRealClassByNamePassingMockInstance()
   {
      Mockit.setUpMock(Collaborator.class.getName(), new MockCollaborator2());

      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMockForInterface()
   {
      BusinessInterface mock = Mockit.setUpMock(new MockCollaborator3());

      mock.provideSomeService();
   }

   interface BusinessInterface
   {
      void provideSomeService();
   }

   @MockClass(realClass = BusinessInterface.class)
   static class MockCollaborator3
   {
      @Mock
      void provideSomeService() {}
   }

   @Test(expected = RuntimeException.class)
   public void setUpAndTearDownMocks()
   {
      Mockit.setUpMocks(MockCollaborator1.class);
      codeUnderTest.doSomething();
      Mockit.tearDownMocks();
      codeUnderTest.doSomething();
   }

   @Test
   public void setUpMocksFromInnerMockClassWithMockConstructor()
   {
      Mockit.setUpMocks(new MockCollaborator4());
      assertFalse(mockExecuted);

      new CodeUnderTest().doSomething();

      assertTrue(mockExecuted);
   }

   @MockClass(realClass = Collaborator.class)
   class MockCollaborator4
   {
      @Mock
      void $init() { mockExecuted = true; }

      @Mock
      void provideSomeService() {}
   }

   @Test
   public void setUpMocksFromMockClassWithStaticMockMethod()
   {
      Mockit.setUpMocks(MockCollaborator5.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaborator5
   {
      @Mock
      @Deprecated // to check that another annotation doesn't interfere, and to increase coverage
      static void provideSomeService() {}
   }

   // Mocks WITH expectations /////////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void setUpMocksContainingExpectations()
   {
      Mockit.setUpMocks(MockCollaboratorWithExpectations.class);

      int result = codeUnderTest.performComputation(2, true);

      assertEquals(0, result);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithExpectations
   {
      @Mock(minInvocations = 1)
      int getValue() { return 0; }

      @Mock(maxInvocations = 2)
      void setValue(int value)
      {
         assertEquals(1, value);
      }

      @Mock
      List<?> complexOperation(Object input1, Object... otherInputs)
      {
         int i = (Integer) otherInputs[0];
         assertEquals(0, i);

         List<Integer> values = new ArrayList<Integer>();
         values.add((Integer) input1);
         return values;
      }

      @Mock(invocations = 0)
      void provideSomeService() {}
   }

   @Test(expected = AssertionError.class)
   public void setUpMockWithMinInvocationsExpectationButFailIt()
   {
      Mockit.setUpMocks(MockCollaboratorWithMinInvocationsExpectation.class);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithMinInvocationsExpectation
   {
      @Mock(minInvocations = 2)
      int getValue() { return 1; }
   }

   @Test(expected = AssertionError.class)
   public void setUpMockWithMaxInvocationsExpectationButFailIt()
   {
      Mockit.setUpMocks(MockCollaboratorWithMaxInvocationsExpectation.class);

      new Collaborator().setValue(23);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithMaxInvocationsExpectation
   {
      @Mock(maxInvocations = 0)
      void setValue(int v) { assertEquals(23, v); }
   }

   @Test(expected = AssertionError.class)
   public void setUpMockWithInvocationsExpectationButFailIt()
   {
      Mockit.setUpMocks(MockCollaboratorWithInvocationsExpectation.class);

      codeUnderTest.doSomething();
      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithInvocationsExpectation
   {
      @Mock(invocations = 1)
      void provideSomeService() {}
   }

   // Reentrant mocks /////////////////////////////////////////////////////////////////////////////////////////////////

   @Test(expected = RuntimeException.class)
   public void setUpReentrantMock()
   {
      Mockit.setUpMocks(MockCollaboratorWithReentrantMock.class);

      codeUnderTest.doSomething();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithReentrantMock
   {
      Collaborator it;

      @Mock(reentrant = false)
      int getValue() { return 123; }

      @Mock(reentrant = true, invocations = 1)
      void provideSomeService() { it.provideSomeService(); }
   }

   // Mocks for constructors and static methods ///////////////////////////////////////////////////////////////////////

   @Test
   public void setUpMockForConstructor()
   {
      Mockit.setUpMocks(MockCollaboratorWithConstructorMock.class);

      new Collaborator(5);
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorWithConstructorMock
   {
      @Mock(invocations = 1)
      void $init(int value)
      {
         assertEquals(5, value);
      }
   }

   @Test
   public void setUpMockForStaticMethod()
   {
      Mockit.setUpMocks(MockCollaboratorForStaticMethod.class);

      Collaborator.doInternal();
   }

   @MockClass(realClass = Collaborator.class)
   static class MockCollaboratorForStaticMethod
   {
      @Mock(invocations = 1)
      static String doInternal() { return ""; }
   }

   @Test
   public void setUpMockForSubclassConstructor()
   {
      Mockit.setUpMocks(MockSubCollaborator.class);

      new SubCollaborator();
   }

   static class SubCollaborator extends Collaborator
   {
      SubCollaborator() {}
   }

   @MockClass(realClass = SubCollaborator.class)
   static class MockSubCollaborator
   {
      @Mock(invocations = 1)
      void $init() {}

      @SuppressWarnings({"UnusedDeclaration"})
      native void doNothing();
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethodInClassWithRegisterNatives()
   {
      Mockit.setUpMocks(MockSystem.class);
      assertEquals(0, System.nanoTime());

      Mockit.tearDownMocks();
      assertTrue(System.nanoTime() > 0);
   }

   @MockClass(realClass = System.class)
   static class MockSystem
   {
      @Mock
      public static long nanoTime() { return 0; }
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethodInClassWithoutRegisterNatives() throws Exception
   {
      Mockit.setUpMocks(MockFloat.class);
      assertEquals(0.0, Float.intBitsToFloat(2243019), 0.0);

      Mockit.tearDownMocks();
      assertTrue(Float.intBitsToFloat(2243019) > 0);
   }

   @MockClass(realClass = Float.class)
   static class MockFloat
   {
      @SuppressWarnings({"UnusedDeclaration"})
      @Mock
      public static float intBitsToFloat(int bits) { return 0; }
   }

   @Test
   public void setUpStartupMock()
   {
      Mockit.setUpStartupMocks(MockCollaborator1.class, new MockCollaborator4());

      assertEquals(0, TestRun.mockFixture().getRedefinedClassCount());
   }

   @Test
   public void setUpMockForJREClass()
   {
      MockThread mockThread = new MockThread();
      Mockit.setUpMocks(mockThread);

      Thread.currentThread().interrupt();

      assertTrue(mockThread.interrupted);
   }

   @MockClass(realClass = Thread.class)
   public static class MockThread
   {
      boolean interrupted;

      @Mock(invocations = 1)
      public void interrupt() { interrupted = true; }
   }

   @Test
   public void mockJREMethodAndConstructorForGivenRealClass() throws Exception
   {
      Mockit.setUpMock(LoginContext.class, MockLoginContextWithoutAnnotation.class);

      new LoginContext("test", (CallbackHandler) null).login();
   }

   @Test
   public void mockJREMethodAndConstructorForGivenRealClassWithGivenMockInstance() throws Exception
   {
      Mockit.setUpMock(LoginContext.class, new MockLoginContextWithoutAnnotation());

      new LoginContext("test", (CallbackHandler) null).login();
   }

   public static class MockLoginContextWithoutAnnotation
   {
      MockLoginContextWithoutAnnotation() {}

      @Mock
      public void $init(String name, CallbackHandler callbackHandler)
      {
         assertEquals("test", name);
         assertNull(callbackHandler);
      }

      @Mock
      public void login() {}
   }

   @Test
   public void mockJREMethodAndConstructorUsingAnnotatedMockClass() throws Exception
   {
      Mockit.setUpMocks(new MockLoginContext());

      new LoginContext("test", (CallbackHandler) null).login();
   }

   @MockClass(realClass = LoginContext.class)
   public static class MockLoginContext
   {
      @Mock(invocations = 1)
      public void $init(String name, CallbackHandler callbackHandler)
      {
         assertEquals("test", name);
         assertNull(callbackHandler);
      }

      @Mock
      public void login() {}

      @Mock(maxInvocations = 1)
      public Subject getSubject() { return null; }
   }

   @Test
   public void mockJREMethodAndConstructorWithAnonymousMockClass() throws Exception
   {
      Mockit.setUpMock(LoginContext.class, new Object()
      {
         @Mock(minInvocations = 1)
         void $init(String name) { assertEquals("test", name); }

         @Mock(invocations = 1)
         void login() {}

         @Mock(maxInvocations = 1)
         void logout() {}
      });

      new LoginContext("test").login();
   }

   @Test(expected = LoginException.class)
   public void mockJREMethodAndConstructorWithMockUpClass() throws Exception
   {
      new MockUp<LoginContext>()
      {
         @Mock
         void $init(String name) { assertEquals("test", name); }

         @Mock
         void login() throws LoginException
         {
            throw new LoginException();
         }
      };

      new LoginContext("test").login();
   }

   @Test
   public void mockPrivateMethodInJREClassByName() throws Exception
   {
      Mockit.setUpMock(LoginContext.class.getName(), new MockLoginContextForPrivateMethod());

      Deencapsulation.invoke(new LoginContext(""), "clearState");
   }

   static final class MockLoginContextForPrivateMethod
   {
      @Mock @SuppressWarnings({"UnusedDeclaration"})
      void $init(String name) {}

      @Mock(invocations = 1)
      static void clearState() {}
   }

   @Test
   public void mockJREClassWithStubs() throws Exception
   {
      Mockit.setUpMocks(new MockLoginContextWithStubs());

      LoginContext context = new LoginContext("");
      context.login();
      context.logout();
   }

   @MockClass(realClass = LoginContext.class, stubs = {"(String)", "logout"})
   final class MockLoginContextWithStubs
   {
      @Mock(invocations = 1)
      void login() {}
   }

   @Test
   public void mockJREClassWithInverseStubs() throws Exception
   {
      Mockit.setUpMocks(MockLoginContextWithInverseStubs.class);

      LoginContext context = new LoginContext("", null, null);
      context.login();
      context.logout();
   }

   @MockClass(realClass = LoginContext.class, stubs = "", inverse = true)
   static class MockLoginContextWithInverseStubs
   {
      @Mock(invocations = 1)
      static void login() {}
   }

   static class ClassWithStaticInitializers
   {
      static String str = "initialized"; // if final it would be a compile-time constant
      static final Object obj = new Object(); // constant, but only at runtime

      static
      {
         System.exit(1);
      }

      static void doSomething() {}

      static
      {
         try {
            Class.forName("NonExistentClass");
         }
         catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
      }
   }

   @Test
   public void mockStaticInitializer()
   {
      new MockUp<ClassWithStaticInitializers>()
      {
         @Mock(invocations = 1)
         void $clinit() {}
      };

      ClassWithStaticInitializers.doSomething();

      assertNull(ClassWithStaticInitializers.str);
      assertNull(ClassWithStaticInitializers.obj);
   }

   static class AnotherClassWithStaticInitializers
   {
      static { System.exit(1); }

      static void doSomething() { throw new RuntimeException(); }
   }

   @Test
   public void stubOutStaticInitializer() throws Exception
   {
      Mockit.setUpMock(new MockForClassWithInitializer());

      AnotherClassWithStaticInitializers.doSomething();
   }

   @MockClass(realClass = AnotherClassWithStaticInitializers.class, stubs = "<clinit>")
   static class MockForClassWithInitializer
   {
      @Mock(minInvocations = 1, maxInvocations = 1)
      void doSomething() {}
   }

   @Test
   public void mockJREInterface() throws Exception
   {
      CallbackHandler callbackHandler = Mockit.setUpMock(new MockCallbackHandler());

      callbackHandler.handle(new Callback[] {new NameCallback("Enter name:")});
   }

   @MockClass(realClass = CallbackHandler.class)
   public static class MockCallbackHandler
   {
      @Mock(invocations = 1)
      public void handle(Callback[] callbacks)
      {
         assertEquals(1, callbacks.length);
         assertTrue(callbacks[0] instanceof NameCallback);
      }
   }

   @Test
   public void mockJREInterfaceWithMockUp() throws Exception
   {
      CallbackHandler callbackHandler = new MockUp<CallbackHandler>()
      {
         @Mock(invocations = 1)
         void handle(Callback[] callbacks)
         {
            assertEquals(1, callbacks.length);
            assertTrue(callbacks[0] instanceof NameCallback);
         }
      }.getMockInstance();

      callbackHandler.handle(new Callback[] {new NameCallback("Enter name:")});
   }

   @Test
   public void accessMockedInstanceThroughItField() throws Exception
   {
      final Subject testSubject = new Subject();

      new MockUp<LoginContext>()
      {
         LoginContext it;

         @Mock(invocations = 1)
         void $init(String name, Subject subject)
         {
            assertNotNull(name);
            assertSame(testSubject, subject);
            assertNotNull(it);
            setField(it, subject); // forces setting of private field, since no setter is available
         }

         @Mock(invocations = 1)
         void login()
         {
            assertNotNull(it);
            assertNull(it.getSubject()); // returns null until the subject is authenticated
            setField(it, "loginSucceeded", true); // private field set to true when login succeeds
         }

         @Mock(invocations = 1)
         void logout()
         {
            assertNotNull(it);
            assertSame(testSubject, it.getSubject());
         }
      };

      LoginContext theMockedInstance = new LoginContext("test", testSubject);
      theMockedInstance.login();
      theMockedInstance.logout();
   }

   @Test
   public void reenterMockedMethodsThroughItField() throws Exception
   {
      // Create objects to be exercised by the code under test:
      Configuration configuration = new Configuration()
      {
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            Map<String, ?> options = Collections.emptyMap();
            return new AppConfigurationEntry[]
            {
               new AppConfigurationEntry(
                  TestLoginModule.class.getName(),
                  AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, options)
            };
         }
      };
      LoginContext loginContext = new LoginContext("test", null, null, configuration);

      // Set up mocks:
      ReentrantMockLoginContext mockInstance = new ReentrantMockLoginContext();

      // Exercise the code under test:
      assertNull(loginContext.getSubject());
      loginContext.login();
      assertNotNull(loginContext.getSubject());
      assertTrue(mockInstance.loggedIn);

      mockInstance.ignoreLogout = true;
      loginContext.logout();
      assertTrue(mockInstance.loggedIn);

      mockInstance.ignoreLogout = false;
      loginContext.logout();
      assertFalse(mockInstance.loggedIn);
   }

   static final class ReentrantMockLoginContext extends MockUp<LoginContext>
   {
      LoginContext it;
      boolean ignoreLogout;
      boolean loggedIn;

      @Mock(reentrant = true)
      void login() throws LoginException
      {
         try {
            it.login();
            loggedIn = true;
         }
         finally {
            it.getSubject();
         }
      }

      @Mock(reentrant = true)
      void logout() throws LoginException
      {
         if (!ignoreLogout) {
            it.logout();
            loggedIn = false;
         }
      }
   }

   public static class TestLoginModule implements LoginModule
   {
      public void initialize(
         Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
         Map<String, ?> options)
      {
      }

      public boolean login() { return true; }
      public boolean commit() { return true; }
      public boolean abort() { return false; }
      public boolean logout() { return true; }
   }

   @Test
   public void mockFileConstructor()
   {
      new MockUp<File>()
      {
         File it;

         @Mock
         void $init(String pathName)
         {
            Deencapsulation.setField(it, "path", "fixedPrefix/" + pathName);
         }
      };

      File f = new File("test");
      assertEquals("fixedPrefix/test", f.getPath());
   }
}
