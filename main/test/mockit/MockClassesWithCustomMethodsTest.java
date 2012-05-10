/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.Assert.*;
import org.junit.*;

public final class MockClassesWithCustomMethodsTest
{
   // The "code under test" for the tests in this class ///////////////////////////////////////////////////////////////

   private final CodeUnderTest codeUnderTest = new CodeUnderTest();

   public interface IDependency { int doSomething(); }

   static class CodeUnderTest
   {
      private final Collaborator dependency = new Collaborator();

      void doSomething() { dependency.provideSomeService(); }

      int doSomethingWithInnerDependency()
      {
         return new IDependency() {
            public int doSomething() { return 45; }
         }.doSomething();
      }

      int performComputation(int a, boolean b)
      {
         int i = dependency.getValue();

         if (b) {
            dependency.setValue(a);
         }

         return i;
      }

      private static class AnotherDependency { int getValue() { return 99; } }
      private final IDependency anotherDependency = new IDependency() {
         public int doSomething() { return 123; }
      };
      int doSomethingWithNestedDependency() { return anotherDependency.doSomething(); }
   }

   static class Collaborator
   {
      static Object xyz;
      protected int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      void provideSomeService() { throw new RuntimeException("Real provideSomeService() called"); }

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   static final class SubCollaborator1 extends Collaborator
   {
      boolean provideAnotherService(String s) { return s.length() > 0; }

      @Override
      int getValue() { return 45; }
   }

   static final class SubCollaborator2 extends Collaborator
   {
      boolean provideAnotherService(String s) { return s.length() > 0; }

      @Override
      int getValue() { return 45; }
   }

   // Methods that specify subclasses to be mocked ////////////////////////////////////////////////////////////////////

   @Test
   public void mockSubclassAlreadyLoaded()
   {
      SubCollaborator1 subCollaborator = new SubCollaborator1();

      new MockUp<Collaborator>() {
         @Override
         protected boolean shouldBeMocked(ClassLoader cl, String subclassName)
         {
            assertEquals(SubCollaborator1.class.getName(), subclassName);
            return true;
         }

         @Mock
         int getValue() { return 123; }
      };

      assertEquals(123, new Collaborator().getValue());
      assertEquals(123, subCollaborator.getValue());
   }

   @Test
   public void mockSubclassWhenLoaded()
   {
      new SubCollaborator1();

      new MockUp<Collaborator>() {
         int i;

         @Override
         protected boolean shouldBeMocked(ClassLoader cl, String subclassName)
         {
            assertTrue(i < 2);
            assertSame(ClassLoader.getSystemClassLoader(), cl);
            assertEquals(i == 0 ? SubCollaborator1.class.getName() : SubCollaborator2.class.getName(), subclassName);
            i++;
            return true;
         }

         @Mock
         int getValue() { return 123; }
      };

      assertEquals(123, new Collaborator().getValue());
      assertEquals(123, new SubCollaborator2().getValue());
   }
}
