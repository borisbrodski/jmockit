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
package mockit;

import javax.security.auth.callback.*;
import javax.security.auth.login.*;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.internal.state.*;

public final class MockitTest
{
   private Class<?> classRedefined;

   static class RealClass
   {
      int doSomething() { return 1; }
   }

   @Test
   public void redefineMethodsWithEmptyMockClass()
   {
      Mockit.redefineMethods(RealClass.class, EmptyMockClass.class);

      assertEquals(0, TestRun.mockFixture().getRedefinedClassCount());
      assertEquals(0, TestRun.getMockClasses().getRegularMocks().getInstanceCount());
      assertEquals(1, new RealClass().doSomething());
      assertEquals(2, new EmptyMockClass().utilityMethod());
   }

   public static class EmptyMockClass
   {
      int utilityMethod() { return 2; }
   }

   @Test
   public void redefineMethodsWithMockClass()
   {
      classRedefined = RealClass.class;
      Mockit.redefineMethods(RealClass.class, MockClass.class);

      assertClassRedefined();
      assertEquals(0, TestRun.getMockClasses().getRegularMocks().getInstanceCount());
   }

   public static class MockClass
   {
      public int doSomething() { return -1; }
   }

   private void assertClassRedefined()
   {
      assertEquals(1, TestRun.mockFixture().getRedefinedClassCount());
      assertTrue(TestRun.mockFixture().containsRedefinedClass(classRedefined));
   }

   @Test
   public void redefineMethodsWithMockInstance()
   {
      classRedefined = RealClass.class;
      MockClass mockInstance = new MockClass();
      Mockit.redefineMethods(RealClass.class, mockInstance);

      assertClassRedefined();
      assertEquals(1, TestRun.getMockClasses().getRegularMocks().getInstanceCount());
      assertTrue(TestRun.getMockClasses().getRegularMocks().containsInstance(mockInstance));
   }

   @Test(expected = IllegalArgumentException.class)
   public void redefineMethodsWithMockMethodForNoCorrespondingRealMethod()
   {
      Mockit.redefineMethods(RealClass.class, MockClassWithMethodWithoutRealMethod.class);
   }

   public static class MockClassWithMethodWithoutRealMethod
   {
      @SuppressWarnings({"UnusedDeclaration"})
      public void doSomethingElse() {}
   }

   @Test(expected = IllegalArgumentException.class)
   public void redefineMethodsWithIncompatibleReturnTypeBetweenRealAndMockMethod()
   {
      Mockit.redefineMethods(RealClass2.class, MockClassWithIncompatibleReturnType.class);

      // Force the compiler to generate a synthetic "String access$0(RealClass2)" method:
      new RealClass2().dummy();
   }

   @SuppressWarnings({"InnerClassMayBeStatic"})
   class RealClass2
   {
      @SuppressWarnings({"UnusedDeclaration"})
      private String dummy() { return null; }
   }

   public static class MockClassWithIncompatibleReturnType
   {
      @SuppressWarnings({"UnusedDeclaration"})
      public int dummy() { return 0; }
   }

   @Test
   public void restoreOriginalDefinition()
   {
      Mockit.redefineMethods(RealClass.class, MockClass.class);

      Mockit.restoreOriginalDefinition(RealClass.class);

      assertEquals(0, TestRun.mockFixture().getRedefinedClassCount());
   }

   @Test
   public void restoreOriginalDefinitionWithClassNotRedefined()
   {
      Mockit.restoreOriginalDefinition(RealClass.class);

      assertEquals(0, TestRun.mockFixture().getRedefinedClassCount());
   }

   @Test
   public void restoreAllDefinitions()
   {
      Mockit.redefineMethods(RealClass.class, new MockClass());

      Mockit.restoreAllOriginalDefinitions();

      assertEquals(0, TestRun.mockFixture().getRedefinedClassCount());
      assertEquals(0, TestRun.getMockClasses().getRegularMocks().getInstanceCount());
   }

   public interface SomeInterface
   {
      int method1();
      long method2(int i);
      boolean method3(String s, boolean b);
      String method4();
      double method5();
      float method6();
      char method7();
      byte method8();
      short method9();
      int doSomething();
   }

   @Test
   public void createClassWithNewEmptyProxy()
   {
      SomeInterface proxy = Mockit.newEmptyProxy(SomeInterface.class);

      assertNotNull(proxy);
      assertEquals(0, proxy.method1());
      assertEquals(0L, proxy.method2(4));
      assertFalse(proxy.method3(null, true));
      assertNull(proxy.method4());
      assertEquals(0.0, proxy.method5(), 0.0);
      assertEquals(0.0F, proxy.method6(), 0.0);
      assertEquals('\0', proxy.method7());
      assertEquals((byte) 0, proxy.method8());
      assertEquals((short) 0, proxy.method9());
   }

   @Test
   public void createEmptyProxyForJREInterface()
   {
      Runnable proxy = Mockit.newEmptyProxy(null, Runnable.class);

      proxy.run();
   }

   @Test
   public void redefineMethodsInProxyClass()
   {
      SomeInterface proxy = Mockit.newEmptyProxy(SomeInterface.class);
      classRedefined = proxy.getClass();

      Mockit.redefineMethods(classRedefined, MockClass.class);

      assertClassRedefined();
   }

   @Test
   public void callEqualsMethodOnEmptyProxy()
   {
      SomeInterface proxy1 = Mockit.newEmptyProxy(SomeInterface.class);
      SomeInterface proxy2 = Mockit.newEmptyProxy(SomeInterface.class);

      //noinspection SimplifiableJUnitAssertion
      assertTrue(proxy1.equals(proxy1));
      assertFalse(proxy1.equals(proxy2));
      assertFalse(proxy2.equals(proxy1));
      //noinspection ObjectEqualsNull
      assertFalse(proxy1.equals(null));
   }

   @Test
   public void callHashCodeMethodOnEmptyProxy()
   {
      SomeInterface proxy = Mockit.newEmptyProxy(SomeInterface.class);

      assertEquals(System.identityHashCode(proxy), proxy.hashCode());
   }

   @Test
   public void callToStringMethodOnEmptyProxy()
   {
      SomeInterface proxy = Mockit.newEmptyProxy(SomeInterface.class);

      assertEquals(
         proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode()),
         proxy.toString());
   }

   @Test
   public void mockJREMethodAndConstructor() throws Exception
   {
      Mockit.redefineMethods(LoginContext.class, new MockLoginContext("test", null));

      new LoginContext("test", (CallbackHandler) null).login();
   }

   @SuppressWarnings({"UnusedDeclaration"})
   public static class MockLoginContext
   {
      public MockLoginContext(String name, CallbackHandler callbackHandler)
      {
         assertEquals("test", name);
         assertNull(callbackHandler);
      }

      public void login() {}
   }
}
