/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;
import static org.junit.Assert.*;

import static mockit.Mockit.*;

public final class ReentrantMockTest
{
   public static class RealClass
   {
      String foo() { return "real value"; }
   }

   @MockClass(realClass = RealClass.class)
   public static class AnnotatedMockClass
   {
      private static Boolean fakeIt;
      public RealClass it;

      @Mock(reentrant = true)
      public String foo()
      {
         if (fakeIt == null) {
            throw new IllegalStateException("null fakeIt");
         }
         else if (fakeIt) {
            return "fake value";
         }
         else {
            return it.foo();
         }
      }
   }

   @Test
   public void callMockMethod()
   {
      setUpMocks(AnnotatedMockClass.class);
      AnnotatedMockClass.fakeIt = true;

      String foo = new RealClass().foo();

      assertEquals("fake value", foo);
   }

   @Test
   public void callOriginalMethod()
   {
      setUpMocks(AnnotatedMockClass.class);
      AnnotatedMockClass.fakeIt = false;

      String foo = new RealClass().foo();

      assertEquals("real value", foo);
   }

   @Test(expected = IllegalStateException.class)
   public void calledMockThrowsException()
   {
      setUpMocks(AnnotatedMockClass.class);
      AnnotatedMockClass.fakeIt = null;

      new RealClass().foo();
   }

   @MockClass(realClass = Runtime.class)
   public static class MockRuntime
   {
      public Runtime it;
      private int runFinalizationCount;

      @Mock(reentrant = true, minInvocations = 3)
      public void runFinalization()
      {
         if (runFinalizationCount < 2) {
            it.runFinalization();
         }

         runFinalizationCount++;
      }

      @Mock(reentrant = true)
      public boolean removeShutdownHook(Thread hook)
      {
         if (hook == null) {
            //noinspection AssignmentToMethodParameter
            hook = Thread.currentThread();
         }

         return it.removeShutdownHook(hook);
      }

      @Mock(invocations = 1)
      public void runFinalizersOnExit(boolean value)
      {
         assertTrue(value);
      }
   }

   @Test
   public void callMockMethodForJREClass()
   {
      Runtime runtime = Runtime.getRuntime();
      setUpMocks(MockRuntime.class);

      runtime.runFinalization();
      runtime.runFinalization();
      runtime.runFinalization();

      assertFalse(runtime.removeShutdownHook(null));

      //noinspection deprecation
      Runtime.runFinalizersOnExit(true);
   }

   @MockClass(realClass = Runtime.class)
   public static class ReentrantMockForNativeMethod
   {
      @Mock(reentrant = true)
      public int availableProcessors() { return 5; }
   }

   @Test(expected = IllegalArgumentException.class)
   public void attemptToSetUpReentrantMockForNativeMethod()
   {
      setUpMocks(ReentrantMockForNativeMethod.class);
   }

   @MockClass(realClass = RealClass.class)
   static class MultiThreadedMock
   {
      public RealClass it;
      private static boolean nobodyEntered = true;

      @Mock(reentrant = true)
      public String foo() throws InterruptedException
      {
         String value = it.foo();

         synchronized (MultiThreadedMock.class) {
            if (nobodyEntered) {
               nobodyEntered = false;
               MultiThreadedMock.class.wait(5000);
            }
            else {
               MultiThreadedMock.class.notifyAll();
            }
         }

         return value.replace("real", "fake");
      }
   }

   @Test(timeout = 1000)
   public void twoConcurrentThreadsCallingTheSameReentrantMock() throws Exception
   {
      setUpMocks(MultiThreadedMock.class);

      final StringBuilder first = new StringBuilder();
      final StringBuilder second = new StringBuilder();

      Thread thread1 = new Thread(new Runnable()
      {
         public void run() { first.append(new RealClass().foo()); }
      });
      thread1.start();

      Thread thread2 = new Thread(new Runnable()
      {
         public void run() { second.append(new RealClass().foo()); }
      });
      thread2.start();

      thread1.join();
      thread2.join();

      assertEquals("fake value", first.toString());
      assertEquals("fake value", second.toString());
   }
}
