/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import static mockit.Mockit.*;
import org.junit.*;
import static org.testng.AssertJUnit.*;

import mockit.*;

public final class ClassInitializationTest
{
   static final class ClassWhichFailsAtInitialization
   {
      static
      {
         //noinspection ConstantIfStatement
         if (true) {
            throw new AssertionError();
         }
      }

      static int value() { return 0; }
   }

   @Test
   public void usingMockUp()
   {
      new MockUp<ClassWhichFailsAtInitialization>()
      {
         @Mock
         void $clinit() {}

         @Mock
         int value() { return 1; }
      };

      assertEquals(1, ClassWhichFailsAtInitialization.value());
   }

   @Test
   public void noMockingAtAll()
   {
      stubOutClass(ClassWhichFailsAtInitialization.class, "<clinit>");
      assertEquals(0, ClassWhichFailsAtInitialization.value());
   }

   @Test
   public void usingExpectations()
   {
      new Expectations()
      {
         ClassWhichFailsAtInitialization unused;

         {
            ClassWhichFailsAtInitialization.value(); result = 1;
         }
      };

      assertEquals(1, ClassWhichFailsAtInitialization.value());
   }

   static class ClassWithStaticInitializer1
   {
      static final String CONSTANT = new String("not a compile-time constant");
      static { doSomething(); }
      static void doSomething() { throw new UnsupportedOperationException("must not execute"); }
   }

   @Test
   public void mockClassWithStaticInitializerNotStubbedOut()
   {
      new NonStrictExpectations()
      {
         @Mocked(stubOutClassInitialization = false)
         final ClassWithStaticInitializer1 mock = null;
      };

      assert ClassWithStaticInitializer1.CONSTANT != null;
      ClassWithStaticInitializer1.doSomething();
   }

   static class ClassWithStaticInitializer2
   {
      static final String CONSTANT = new String("not a compile-time constant");
      static { doSomething(); }
      static void doSomething() { throw new UnsupportedOperationException("must not execute"); }
   }

   @Test
   public void useClassWithStaticInitializerNeverStubbedOutAndNotMockedNow()
   {
      // Allows the class to be initialized without throwing the exception.
      stubOutClass(ClassWithStaticInitializer2.class, "doSomething");

      // Initializes the class:
      assert ClassWithStaticInitializer2.CONSTANT != null;

      // Restore the now initialized class:
      tearDownMocks(ClassWithStaticInitializer2.class);

      try {
         ClassWithStaticInitializer2.doSomething();
         fail();
      }
      catch (UnsupportedOperationException ignore) {}
   }

   static class AnotherClassWithStaticInitializer1
   {
      static final String CONSTANT = new String("not a compile-time constant");
      static { doSomething(); }
      static void doSomething() { throw new UnsupportedOperationException("must not execute"); }
      int getValue() { return -1; }
   }

   @Test
   public void mockClassWithStaticInitializerStubbedOut(
      @Mocked(stubOutClassInitialization = true) AnotherClassWithStaticInitializer1 mock)
   {
      assert AnotherClassWithStaticInitializer1.CONSTANT == null;
      AnotherClassWithStaticInitializer1.doSomething();
      assert mock.getValue() == 0;
   }

   static class AnotherClassWithStaticInitializer2
   {
      static final String CONSTANT = new String("not a compile-time constant");
      static { doSomething(); }
      static void doSomething() { throw new UnsupportedOperationException("must not execute"); }
      int getValue() { return -1; }
   }

   @Test
   public void useClassWithStaticInitializerPreviouslyStubbedOutButNotMockedNow()
   {
      // Stubs out the static initializer, initializes the class, and then restores it:
      stubOutClass(AnotherClassWithStaticInitializer2.class, "<clinit>");
      assert AnotherClassWithStaticInitializer2.CONSTANT == null;
      tearDownMocks(AnotherClassWithStaticInitializer2.class);

      try {
         AnotherClassWithStaticInitializer2.doSomething();
         fail();
      }
      catch (UnsupportedOperationException ignore) {}

      assert new AnotherClassWithStaticInitializer2().getValue() == -1;
   }
}
