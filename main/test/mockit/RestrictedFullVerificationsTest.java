/*
 * JMockit Verifications
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
package mockit;

import java.util.concurrent.*;

import org.junit.*;

@SuppressWarnings({"UnusedDeclaration"})
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

      new FullVerifications(mock)
      {{
         mock.prepare();
         mock.setSomething(anyInt); minTimes = 1; maxTimes = 2;
         mock.editABunchMoreStuff();
         mock.save(); times = 1;
      }};
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsWithSomeMissing(final AnotherDependency mock2)
   {
      exerciseCodeUnderTest();
      mock2.doSomething();

      new FullVerifications(mock, mock2)
      {{
         mock.prepare();
         mock.setSomething(withAny(0));
         mock.save();
         mock2.doSomething();
      }};
   }

   @Test
   public void verifyOnlyInvocationsToGenericType(final Callable<Dependency> mock2) throws Exception
   {
      exerciseCodeUnderTest();
      mock2.call();

      new FullVerificationsInOrder(mock2)
      {{
         mock2.call();
      }};
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

      new FullVerifications(2, mock)
      {{
         mock.setSomething(anyInt);
         mock.save();
      }};
   }

   @Test
   public void verifyAllInvocationsToInheritedMethods(SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(1, mock)
      {
         {
            mock.prepare();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsToInheritedMethods_whenNotVerified(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(1, mock)
      {
         {
            mock2.getValue();
         }
      };
   }

   @Test
   public void verifyAllInvocationsToSubclassMethods(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(1, mock2.getClass())
      {
         {
            mock2.getValue();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsToSubclassMethods_whenNotVerified(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(1, mock2.getClass())
      {
         {
            mock.prepare();
         }
      };
   }

   @Test
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass(final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(mock2)
      {
         {
            mock.prepare();
            mock2.getValue();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenInheritedMethodNotVerified(
      final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerificationsInOrder(mock2)
      {
         {
            mock2.getValue();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenSubclassMethodNotVerified(
      final SubDependency mock2)
   {
      mock.prepare();
      mock2.getValue();

      new FullVerifications(mock2)
      {
         {
            mock.prepare();
         }
      };
   }

   @Test
   public void verifyAllInvocationsWithReplayOnDifferentInstance()
   {
      new Dependency().save();

      new FullVerificationsInOrder(mock)
      {
         {
            new Dependency();
            mock.save();
         }
      };
   }

   @Test
   public void verifyAllInvocationsWithReplayOnSameInstance(final Dependency mock2)
   {
      mock2.editABunchMoreStuff();

      new FullVerifications(mock2)
      {
         {
            mock2.editABunchMoreStuff();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllWithReplayOnDifferentInstanceWhenShouldBeSame(final Dependency mock2)
   {
      mock2.editABunchMoreStuff();

      new FullVerificationsInOrder(mock2)
      {
         {
            mock.editABunchMoreStuff();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void verifyAllWithUnverifiedReplayOnSameInstance(final Dependency mock2)
   {
      mock.editABunchMoreStuff();
      mock2.editABunchMoreStuff();

      new FullVerifications(mock2)
      {
         {
            mock.editABunchMoreStuff();
         }
      };
   }

   @Test
   public void verifyStaticInvocationForSpecifiedMockInstance(final AnotherDependency mock2)
   {
      mock2.doSomething();
      AnotherDependency.staticMethod();
      mock2.doSomethingElse(1);
      mock.editABunchMoreStuff();
      mock2.doSomethingElse(2);

      new FullVerificationsInOrder(mock2)
      {
         {
            mock2.doSomething();
            AnotherDependency.staticMethod();
            mock2.doSomethingElse(anyInt);
            mock2.doSomethingElse(anyInt);
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unverifiedStaticInvocationForSpecifiedMockInstance(final AnotherDependency mock2)
   {
      mock2.doSomething();
      AnotherDependency.staticMethod();

      new FullVerifications(mock2)
      {
         {
            mock2.doSomething();
         }
      };
   }

   @Test(expected = AssertionError.class)
   public void unverifiedStaticInvocationForSpecifiedSubclassInstance(final SubDependency mock2)
   {
      mock2.getValue();
      Dependency.staticMethod("test");

      new FullVerificationsInOrder(1, mock2)
      {
         {
            mock2.getValue();
         }
      };
   }
}