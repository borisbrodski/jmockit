/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.concurrent.*;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.internal.*;

@SuppressWarnings("UnusedDeclaration")
public final class RestrictedFullVerificationsTest
{
   static class Dependency
   {
      public void setSomething(int value) {}
      public int editABunchMoreStuff() { return 0; }
      public boolean prepare() { return false; }
      public void save() {}
      static void staticMethod(String s) {}
   }

   static final class SubDependency extends Dependency
   {
      int getValue() { return 5; }
   }

   static final class AnotherDependency
   {
      void doSomething() {}
      String doSomethingElse(int i) { return "" + i; }
      static boolean staticMethod() { return true; }
   }
   
   @Mocked Dependency mock;

   private void exerciseCodeUnderTest()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.editABunchMoreStuff();
      mock.save();
   }

   @Test
   public void verifyAllInvocationsToOnlyOneOfTwoMockedTypes(AnotherDependency mock2)
   {
      exerciseCodeUnderTest();
      mock2.doSomething();

      new FullVerifications(mock) {{
         mock.prepare();
         mock.setSomething(anyInt); minTimes = 1; maxTimes = 2;
         mock.editABunchMoreStuff();
         mock.save(); times = 1;
      }};

      new FullVerifications(mock.getClass()) {{
         mock.prepare();
         mock.setSomething(anyInt); minTimes = 1; maxTimes = 2;
         mock.editABunchMoreStuff();
         mock.save(); times = 1;
      }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllInvocationsWithSomeMissing(final AnotherDependency mock2)
   {
      exerciseCodeUnderTest();
      mock2.doSomething();

      new FullVerifications(mock, mock2) {{
         mock.prepare();
         mock.setSomething(anyInt);
         mock.save();
         mock2.doSomething();
      }};
   }

   @Test
   public void verifyOnlyInvocationsToGenericType(final Callable<Dependency> mock2) throws Exception
   {
      exerciseCodeUnderTest();
      mock2.call();

      new FullVerificationsInOrder(mock2) {{ mock2.call(); }};
   }

   @Test
   public void verifyAllInvocationsToOneOfTwoMocksInIteratingBlock(AnotherDependency mock2)
   {
      mock2.doSomething();
      mock.setSomething(123);
      mock.save();
      mock2.doSomethingElse(1);
      mock.setSomething(45);
      mock.save();
      mock2.doSomethingElse(2);

      new FullVerifications(2, new Object[] {mock}) {{
         mock.setSomething(anyInt);
         mock.save();
      }};
   }

   @Test
   public void verifyAllInvocationsToInheritedMethods(SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(1, new Object[] {mock}) {{ mock.prepare(); }};
      new FullVerificationsInOrder(Dependency.class) {{ mock.prepare(); }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllInvocationsToInheritedMethods_whenNotVerified(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(1, new Object[] {mock}) {{ mock2.getValue(); }};
   }

   @Test
   public void verifyAllInvocationsToSubclassMethods(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(1, new Object[] {mock2.getClass()}) {{ mock2.getValue(); }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllInvocationsToSubclassMethods_whenNotVerified(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(1, new Object[] {mock2.getClass()}) {{ mock.prepare(); }};
   }

   @Test
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(mock2) {{
         mock.prepare();
         mock2.getValue();
      }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenInheritedMethodNotVerified(
      final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(mock2) {{ mock2.getValue(); }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenSubclassMethodNotVerified(
      final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(mock2) {{ mock.prepare(); }};
   }

   @Test
   public void verifyAllInvocationsWithReplayOnDifferentInstance()
   {
      new Dependency().save();

      new FullVerificationsInOrder(mock) {{
         new Dependency();
         mock.save();
      }};
   }

   @Test
   public void verifyAllInvocationsWithReplayOnSameInstance(final Dependency mock2)
   {
      mock2.editABunchMoreStuff();

      new FullVerifications(mock2) {{ mock2.editABunchMoreStuff(); }};
   }

   @Test(expected = MissingInvocation.class)
   public void verifyAllWithReplayOnDifferentInstanceWhenShouldBeSame(final Dependency mock2)
   {
      mock2.editABunchMoreStuff();

      new FullVerificationsInOrder(mock2) {{
         mock.editABunchMoreStuff();
      }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void verifyAllWithUnverifiedReplayOnSameInstance(final Dependency mock2)
   {
      mock.editABunchMoreStuff();
      mock2.editABunchMoreStuff();

      new FullVerifications(mock2) {{ mock.editABunchMoreStuff(); }};
   }

   @Test
   public void verifyStaticInvocationForSpecifiedMockInstance(final AnotherDependency mock2)
   {
      mock2.doSomething();
      AnotherDependency.staticMethod();
      mock2.doSomethingElse(1);
      mock.editABunchMoreStuff();
      mock2.doSomethingElse(2);

      new FullVerificationsInOrder(mock2) {{
         mock2.doSomething();
         AnotherDependency.staticMethod();
         mock2.doSomethingElse(anyInt);
         mock2.doSomethingElse(anyInt);
      }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void unverifiedStaticInvocationForSpecifiedMockInstance(final AnotherDependency mock2)
   {
      mock2.doSomething();
      AnotherDependency.staticMethod();

      new FullVerifications(mock2) {{ mock2.doSomething(); }};
   }

   @Test(expected = UnexpectedInvocation.class)
   public void unverifiedStaticInvocationForSpecifiedSubclassInstance(final SubDependency mock2)
   {
      mock2.getValue();
      Dependency.staticMethod("test");

      new FullVerificationsInOrder(1, new Object[] {mock2}) {{ mock2.getValue(); }};
   }

   @Test
   public void verifyNoInvocationsOccurredOnOneOfTwoMockedDependencies(AnotherDependency mock2)
   {
      mock2.doSomething();

      new FullVerifications(mock) {};
   }

   @Test
   public void verifyNoInvocationsOccurredOnMockedDependencyWithOneHavingOccurred(AnotherDependency mock2)
   {
      mock2.doSomething();
      mock.editABunchMoreStuff();

      try {
         new FullVerifications(mock) {};
         fail();
      }
      catch (UnexpectedInvocation e) {
         assertTrue(e.getMessage().contains("editABunchMoreStuff()"));
      }
   }

   @Test
   public void verifyNoInvocationsOnOneOfTwoMockedDependenciesBeyondThoseRecordedAsExpected(
      final AnotherDependency mock2)
   {
      new NonStrictExpectations() {{
         mock.setSomething(anyInt); minTimes = 1;
         mock2.doSomething(); times = 1;
      }};

      mock.prepare();
      mock.setSomething(1);
      mock.setSomething(2);
      mock.save();
      mock2.doSomething();

      new FullVerifications(mock2) {};
   }
}