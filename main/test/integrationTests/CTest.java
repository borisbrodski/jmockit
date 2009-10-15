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
package integrationTests;

import java.io.*;
import static java.util.Arrays.*;
import java.util.*;
import static java.util.Collections.*;

import mockit.*;
import static mockit.Mockit.*;
import org.junit.*;
import static org.junit.Assert.*;

@SuppressWarnings({"UnusedDeclaration", "ClassWithTooManyMethods"})
public final class CTest
{
   private String testData;

   @After
   public void tearDown()
   {
      Mockit.restoreAllOriginalDefinitions();
   }

   @Test
   public void testPublicStaticBooleanNoArgs() throws Exception
   {
      redefineMethods(C.class, D.class);

      boolean b = C.b();
      assertFalse(b);
   }

   @Test
   public void testPublicIntNoArgs() throws Exception
   {
      redefineMethods(C.class, new E());

      int i = new C().i();
      assertEquals(2, i);
   }

   public static class E
   {
      static boolean noReturnCalled;

      public int i()
      {
         System.out.println("E.i");
         return 2;
      }

      public void noReturn()
      {
         noReturnCalled = true;
      }
   }

   @Test
   public void testPackageVoidNoArgs()
   {
      redefineMethods(C.class, E.class);

      E.noReturnCalled = false;
      C c = new C();
      c.noReturn();
      assertTrue("noReturn() wasn't called", E.noReturnCalled);
   }

   @Test
   public void testProtectedVoid1Arg()
   {
      F f = new F();
      redefineMethods(C.class, f);

      C c = new C();
      c.setSomeValue("test");
      assertEquals("mock setSomeValue not called", "test", f.someValue);
   }

   public static class F
   {
      String someValue = "";

      public void setSomeValue(String someValue)
      {
         this.someValue = someValue;
      }
   }

   @Test
   public void testPrimitiveLongAndIntParameters()
   {
      redefineMethods(C.class, PrimitiveLongAndIntParametersMock.class);
      PrimitiveLongAndIntParametersMock.sum = 0;
      C.validateValues(1L, 2);
      assertEquals(3L, PrimitiveLongAndIntParametersMock.sum);
   }

   public static class PrimitiveLongAndIntParametersMock
   {
      static long sum;

      public static void validateValues(long v1, int v2)
      {
         sum = v1 + v2;
      }
   }

   @Test
   public void testPrimitiveNumericParameters()
   {
      redefineMethods(C.class, new Object() {
         public double sumValues(byte v1, short v2, int v3, long v4, float v5, double v6)
         {
            return 0.0;
         }
      });

      double sum = new C().sumValues((byte) 1, (short) 2, 3, 4L, 5.0F, 6.0);
      assertEquals(0.0, sum, 0.0);
   }

   @Test
   public void testDefaultConstructor()
   {
      redefineMethods(C.class, new DefaultConstructorMock(), true);

      DefaultConstructorMock.x = 0;
      new C();

      assertEquals(1, DefaultConstructorMock.x);
   }

   @SuppressWarnings({"UtilityClassWithPublicConstructor"})
   public static class DefaultConstructorMock
   {
      static int x;
      public DefaultConstructorMock() { x = 1; }
   }

   @Test
   public void testConstructor1Arg()
   {
      redefineMethods(C.class, F2.class);

      C c = new C("test");
      assertNull(c.getSomeValue());
   }

   public static class F2
   {
      public F2(String someValue)
      {
         // do nothing
      }
   }

   @Test
   public void testTwoConstructorsWithMultipleArgs()
   {
      redefineMethods(RealClassWithTwoConstructors.class, new MockConstructors("", '\0'));

      new RealClassWithTwoConstructors("a", 'b');
      assertEquals("ab", testData);

      new RealClassWithTwoConstructors("a", 'b', 1);
      assertEquals("ab1", testData);
   }

   // TODO: this class should be static, but then special handling for the first parameter in the
   // mock constructors is needed
   public class RealClassWithTwoConstructors
   {
      RealClassWithTwoConstructors(String a, char b) {}
      RealClassWithTwoConstructors(String a, char b, int c) {}
   }

   public class MockConstructors
   {
      public MockConstructors(String a, char b) { testData = a + b; }
      public MockConstructors(String a, char b, int c) { testData = a + b + c; }
   }

   @Test
   public void testPublicFinalDate3Args()
   {
     redefineMethods(C.class, G.class);

     assertNull(new C().createOtherObject(true, new Date(1000), 2000));
   }

   public static class G
   {
      public final Date createOtherObject(boolean b, Date d, int a)
      {
         System.out.println("G.createOtherObject(" + b + "," + d + "," + a + "," + ")");
         return null;
      }
   }

   @Test
   public void testPrivateStaticVoidNoArgs()
   {
      redefineMethods(C.class, H.class);

      C.printText();
      assertEquals("H.doPrintText", C.printedText);
   }

   public static class H { public static void doPrintText() { C.printedText = "H.doPrintText"; } }

   @Test
   public void testStaticVoid1ArgOverload()
   {
      redefineMethods(C.class, H2.class);

      C.printedText = "";
      C.printText("mock");
      assertEquals("", C.printedText);
   }

   public static class H2
   {
      public static void printText(String text) { assertEquals("mock", text); }
   }

   @Test
   public void testInt1ArgWithWildcard()
   {
      redefineMethods(C.class, new I());

      Collection<String> names = asList("abc", "G20", "xyz");
      int c = new C().count(names);
      assertEquals(0, c);
   }

   public static class I { public int count(Collection<?> items) { return 0; } }

   @Test
   public void testGenericList2Args()
   {
      redefineMethods(C.class, J.class);

      Collection<String> names = asList("abc", "G20", "xyz");
      names = new C().orderBy(names, false);
      //noinspection AssertEqualsBetweenInconvertibleTypes
      assertEquals(emptyList(), names);
   }

   public static class J
   {
      public <E extends Comparable<E>> List<E> orderBy(Collection<E> items, boolean asc)
      {
         return emptyList();
      }
   }

   @Test
   public void testThrowsException() throws FileNotFoundException
   {
      redefineMethods(C.class, K.class);

      new C().loadFile("temp");
      // no exception expected
      assertTrue(K.executed);
   }

   public static class K
   {
      static boolean executed;
      public void loadFile(String name) { executed = true; }
   }

   @Test
   public void testThrowsRuntimeException() throws FileNotFoundException
   {
      redefineMethods(C.class, L.class);

      try {
         new C().loadFile("temp");
         fail();
      }
      catch (IllegalArgumentException ignore) {
         // passed
      }
   }

   public static class L
   {
      public void loadFile(String name) throws FileNotFoundException
      {
         throw new IllegalArgumentException();
      }
   }

   @Test(expected = TooManyListenersException.class)
   public void testThrowsCheckedExceptionNotInThrowsOfRealMethod() throws Exception
   {
      redefineMethods(C.class, MockThrowingCheckedExceptionNotInRealMethodThrowsClause.class);

      new C().loadFile("test");
      fail();
   }

   public static class MockThrowingCheckedExceptionNotInRealMethodThrowsClause
   {
      public void loadFile(String name) throws TooManyListenersException
      {
         throw new TooManyListenersException();
      }
   }

   @Test
   public void testVarargs()
   {
      redefineMethods(C.class, M.class);

      new C().printArgs(1, true, "test");
      assertEquals("mock", C.printedText);
   }

   public static class M
   {
      public void printArgs(Object... args)
      {
         C.printedText = "mock";
         System.out.println("M.printArgs: " + asList(args));
      }
   }

   @Test
   public void testRedefineOneClassTwoTimes()
   {
      redefineMethods(C.class, D.class);
      boolean b = C.b();
      assertFalse(b);

      redefineMethods(C.class, D.class);
      b = C.b();
      assertFalse(b);
   }

   @Test
   public void testRedefineOneClassThreeTimes()
   {
      redefineMethods(Ct.class, Cf.class);
      assertFalse(Ct.b());

      redefineMethods(Ct.class, Ce.class);
      redefineMethods(Ct.class, Ce.class);

      redefineMethods(Ct.class, Cf.class);
      assertFalse(Ct.b());
   }

   public static class Ct { public static boolean b() { return true; } }
   public static class Cf { public static boolean b() { return false; } }
   public static class Ce { public static boolean b() { return true; }}

   @Test
   public void testRedefineMultipleClasses()
   {
      redefineMethods(C.class, D.class);
      redefineMethods(C2.class, N.class);

      boolean b = C.b();
      assertFalse(b);

      List<C2> c2Found = new C().findC2();
      assertNull(c2Found.get(0).getCode());
   }

   public static class N
   {
      public String getCode() { return null; }
   }

   @Test
   public void testMockInstanceState()
   {
      O mock = new O();
      redefineMethods(C.class, mock);

      C c = new C("some data");

      assertEquals(mock.value, c.getSomeValue());
   }

   public static class O
   {
      String value = "mock data";
      public String getSomeValue() { return value; }
   }

   @Test
   public void testTwoMockInstances()
   {
      P mock1 = new P(new C2(123, "one23"));
      redefineMethods(C.class, mock1);

      C2Mock mock2 = new C2Mock();
      mock2.code = "mock2";
      redefineMethods(C2.class, mock2);

      List<C2> c2Found = new C().findC2();
      assertEquals(mock1.data, c2Found);

      C2 c2 = c2Found.get(0);
      assertEquals(mock2.code, c2.getCode());
   }

   public static class P
   {
      List<C2> data;

      P(C2... data) { this.data = asList(data); }
      public List<C2> findC2() { return data; }
   }

   public class C2Mock
   {
      private String code;

      C2Mock() {}
      public String getCode() { return code; }
   }

   @Test
   public void testRedefineMethodsWithInnerMockClass()
   {
      try {
         redefineMethods(C2.class, C2Mock.class);
         fail();
      }
      catch (IllegalArgumentException e) {
         // OK
      }
   }

   // Redefines the same as C2Mock, since inherited public methods ARE also considered.
   public class C3Mock extends C2Mock
   {
      C3Mock() {}
   }

   @Test
   public void testRedefineMethodsWithExtendedMockClass()
   {
      redefineMethods(C2.class, new C3Mock());
      C2 c2 = new C2(12, "c2");
      assertNull(c2.getCode());
   }

   @Test
   public void testMockInstanceOfAnonymousInnerClass()
   {
      redefineMethods(C.class, new Object() {
         public String getSomeValue() { return "test data"; }
      });

      C c = new C("some data");

      assertEquals("test data", c.getSomeValue());
   }

   @Test
   public void testMockInstanceOfAnonymousInnerClassUsingParentData()
   {
      redefineMethods(C.class, new Serializable() {
         public String getSomeValue() { return testData; }
      });

      testData = "my test data";
      C c = new C("some data");

      assertEquals(testData, c.getSomeValue());
   }

   @Test
   public void testAccessToInstanceUnderTest()
   {
      redefineMethods(C.class, new Object()
      {
         // Instance under test. Warning: calling redefined methods may lead to infinite recursion!
         C it;

         @Override
         public String toString() { return it.getSomeValue().toUpperCase(); }
      });

      String value = new C("test data").toString();

      assertEquals("TEST DATA", value);
   }

   @Test
   public void testAttemptToMockNonExistentMethodInRealClass()
   {
      try {
         redefineMethods(C.class, ClassWithMockMethodForNonExistentRealMethod.class);
         fail("Should have thrown " + IllegalArgumentException.class);
      }
      catch (IllegalArgumentException e) {
         // OK
      }
   }

   public static class ClassWithMockMethodForNonExistentRealMethod
   {
      public void nonExistent() {}
      int helperMethodNotIntendedAsMock(String s) { return s.length(); }
   }

   @Test
   public void testMockJREClasses()
   {
      redefineMethods(Date.class, new MockDate());
      assertEquals(0L, new Date().getTime());
      assertTrue(new Date().before(null));

      redefineMethods(Date.class, MockDate.class);
      assertEquals(0L, new Date().getTime());
      assertTrue(new Date().before(null));

      redefineMethods(String.class, MockString.class);
      assertEquals("0", String.valueOf(1.2));
      assertEquals("1", String.valueOf(5.0f));

      redefineMethods(String.class, new MockString());
      assertEquals("0", String.valueOf(1.2));
      assertEquals("1", String.valueOf(5.0f));
   }

   public static class MockDate
   {
      public long getTime() { return 0L; }
      public static boolean before(Date d) { return true; }
   }

   public static class MockString
   {
      public static String valueOf(double d) { return "0"; }
      public String valueOf(float f) { return "1"; }
   }

   @Test
   public void testStaticMockMethodForNonStaticRealMethod()
   {
      redefineMethods(CX.class, C4Mock.class);

      int i = new CX().i("");

      assertEquals(2, i);
   }

   public static class C4Mock
   {
      public static int i(String s) { return 2; }
   }

   @Test
   public void testNonStaticMockMethodForStaticRealMethod()
   {
      redefineMethods(CX.class, new CXMock());

      boolean b = CX.b("");

      assertFalse(b);
   }

   public static class CX
   {
      public int i(String s)
      {
         return 1;
      }

      public static boolean b(String s)
      {
         return true;
      }
   }

   public class CXMock
   {
      CXMock() {}

      public boolean b(String s)
      {
         return false;
      }
   }

   @Test
   public void testMockMethodWithDifferentReturnType()
   {
      try {
         redefineMethods(C.class, MockWithDifferentButAssignableReturnType.class);
         fail();
      }
      catch (IllegalArgumentException e) {
         // passed
      }
   }

   public static class MockWithDifferentButAssignableReturnType
   {
      public final java.sql.Date createOtherObject(boolean b, Date d, int a)
      {
         return new java.sql.Date(d.getTime());
      }
   }
}
