/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;

import static org.junit.Assert.*;

@SuppressWarnings({"UnusedDeclaration"})
public final class NonStrictExpectationsTest
{
   public static class Dependency
   {
      public void setSomething(int value) {}
      public void setSomethingElse(String value) {}
      public int editABunchMoreStuff() { return 1; }
      public boolean notifyBeforeSave() { return true; }
      public void prepare() {}
      public void save() {}

      static int staticMethod(Object o, Exception e) { return -1; }
   }

   @Mocked Dependency mock;

   private void exerciseCodeUnderTest()
   {
      mock.prepare();
      mock.setSomething(123);
      mock.setSomethingElse("anotherValue");
      mock.setSomething(45);
      mock.editABunchMoreStuff();
      mock.notifyBeforeSave();
      mock.save();
   }

   @Test
   public void recordSimpleInvocations()
   {
      new NonStrictExpectations()
      {{
         mock.prepare();
         mock.editABunchMoreStuff();
         mock.setSomething(45);
      }};

      exerciseCodeUnderTest();
   }

   @Test
   public void recordInvocationThatWillNotOccur()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); returns(123);
      }};

      mock.setSomething(123);
      mock.prepare();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithExactExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); times = 1;
      }};
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithMinimumExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); minTimes = 2;
      }};

      mock.editABunchMoreStuff();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationWithMaximumExpectedNumberOfInvocationsButFailToSatisfy()
   {
      new NonStrictExpectations()
      {{
         mock.editABunchMoreStuff(); maxTimes = 1;
      }};

      mock.editABunchMoreStuff();
      mock.editABunchMoreStuff();
   }

   @Test
   public void recordInvocationsWithExpectedInvocationCounts()
   {
      new NonStrictExpectations()
      {{
         mock.setSomethingElse(anyString); minTimes = 1;
         mock.save(); times = 2;
      }};

      mock.setSomething(3);
      mock.save();
      mock.setSomethingElse("test");
      mock.save();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationsWithMinInvocationCountLargerThanWillOccur()
   {
      new NonStrictExpectations()
      {{
         mock.save(); minTimes = 2;
      }};

      mock.save();
   }

   @Test
   public void recordWithArgumentMatcherAndIndividualInvocationCounts()
   {
      new NonStrictExpectations(1)
      {{
         mock.prepare(); maxTimes = 1;
         mock.setSomething(anyInt); minTimes = 2;
         mock.editABunchMoreStuff(); minTimes = 0; maxTimes = 5;
         mock.save(); times = 1;
      }};

      exerciseCodeUnderTest();
   }

   @Test
   public void recordWithMaxInvocationCountFollowedByReturnValue()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         maxTimes = 1;
         returns(1);
      }};

      assertEquals(1, Dependency.staticMethod(new Object(), new Exception()));
   }

   @Test(expected = AssertionError.class)
   public void recordWithMaxInvocationCountFollowedByReturnValueButReplayOneTimeBeyondMax()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         maxTimes = 1;
         returns(1);
      }};

      Dependency.staticMethod(null, null);
      Dependency.staticMethod(null, null);
   }

   @Test
   public void recordWithReturnValueFollowedByExpectedInvocationCount()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         returns(1);
         times = 1;
      }};

      assertEquals(1, Dependency.staticMethod(null, null));
   }

   @Test
   public void recordWithMinInvocationCountFollowedByReturnValueUsingDelegate()
   {
      new NonStrictExpectations()
      {{
         Dependency.staticMethod(any, null);
         minTimes = 1;
         returns(new Delegate()
         {
            int staticMethod(Object o, Exception e) { return 1; }
         });
      }};

      assertEquals(1, Dependency.staticMethod(null, null));
   }

   @Test
   public void recordInvocationsInIteratingBlock()
   {
      new NonStrictExpectations(2)
      {{
         mock.setSomething(anyInt); times = 1;
         mock.save(); times = 1;
      }};

      mock.setSomething(123);
      mock.save();
      mock.setSomething(45);
      mock.save();
   }

   @Test(expected = AssertionError.class)
   public void recordInvocationInBlockWithWrongNumberOfIterations()
   {
      new NonStrictExpectations(3)
      {{
         mock.setSomething(123); minTimes = 1;
      }};

      mock.setSomething(123);
   }

   @Test
   public void recordWithArgumentMatcherAndIndividualInvocationCountsInIteratingBlock()
   {
      new NonStrictExpectations(2)
      {{
         mock.prepare(); maxTimes = 1;
         mock.setSomething(anyInt); minTimes = 2;
         mock.editABunchMoreStuff(); minTimes = 1; maxTimes = 5;
         mock.save(); times = 1;
      }};

      for (int i = 0; i < 2; i++) {
         exerciseCodeUnderTest();
      }
   }
}
