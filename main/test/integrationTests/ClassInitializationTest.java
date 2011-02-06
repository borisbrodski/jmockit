/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

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
         // Without this mock (which stubs out static initializers of the class), ALL tests fail as
         // a consequence of the class never being successfully initialized due to an exception.
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

   static class ClassWithStaticInitializer
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
         final ClassWithStaticInitializer mock = null;
      };

      assert ClassWithStaticInitializer.CONSTANT != null;
      ClassWithStaticInitializer.doSomething();
   }

   @Test
   public void useClassWithStaticInitializerNeverStubbedOutAndNotMockedNow()
   {
      assert ClassWithStaticInitializer.CONSTANT != null;

      try {
         ClassWithStaticInitializer.doSomething();
         fail();
      }
      catch (UnsupportedOperationException ignore) {}
   }

   static class AnotherClassWithStaticInitializer
   {
      static final String CONSTANT = new String("not a compile-time constant");
      static { doSomething(); }
      static void doSomething() { throw new UnsupportedOperationException("must not execute"); }
      int getValue() { return -1; }
   }

   @Test
   public void mockClassWithStaticInitializerStubbedOut(
      @Mocked(stubOutClassInitialization = true) AnotherClassWithStaticInitializer mock)
   {
      assert AnotherClassWithStaticInitializer.CONSTANT == null;
      AnotherClassWithStaticInitializer.doSomething();
      assert mock.getValue() == 0;
   }

   @Test
   public void useClassWithStaticInitializerPreviouslyStubbedOutButNotMockedNow()
   {
      assert AnotherClassWithStaticInitializer.CONSTANT == null;

      try {
         AnotherClassWithStaticInitializer.doSomething();
         fail();
      }
      catch (UnsupportedOperationException ignore) {}

      assert new AnotherClassWithStaticInitializer().getValue() == -1;
   }
}
