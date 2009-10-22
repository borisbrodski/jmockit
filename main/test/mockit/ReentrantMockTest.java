/*
 * JMockit Core/Annotations
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

import org.junit.*;
import static org.junit.Assert.*;

import static mockit.Mockit.*;

public final class ReentrantMockTest
{
   @Test
   public void callMockMethodUsingCoreAPI()
   {
      Mockit.redefineMethods(RealClass.class, MockClassForCoreAPI.class);
      MockClassForCoreAPI.fakeIt = true;

      String foo = new RealClass().foo();

      assertEquals("fakevalue", foo);
   }

   @Test(expected = StackOverflowError.class)
   public void callOriginalMethodUsingCoreAPI()
   {
      Mockit.redefineMethods(RealClass.class, new MockClassForCoreAPI());
      MockClassForCoreAPI.fakeIt = false;

      // This call will recurse infinitely.
      String foo = new RealClass().foo();

      assertEquals("realvalue", foo);
   }

   public static class MockClassForCoreAPI
   {
      private static boolean fakeIt;
      public RealClass it;

      @SuppressWarnings({"UnusedDeclaration"})
      public String foo()
      {
         if (fakeIt) {
            return "fakevalue";
         }
         else {
            // Keeps calling the mock foo in infinite recursion.
            return it.foo();
         }
      }
   }

   public static class RealClass
   {
      String foo()
      {
         return "realvalue";
      }
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
            return "fakevalue";
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

      assertEquals("fakevalue", foo);
   }

   @Test
   public void callOriginalMethod()
   {
      setUpMocks(AnnotatedMockClass.class);
      AnnotatedMockClass.fakeIt = false;

      String foo = new RealClass().foo();

      assertEquals("realvalue", foo);
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
               MultiThreadedMock.class.wait();
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

      assertEquals("fakevalue", first.toString());
      assertEquals("fakevalue", second.toString());
   }
}
