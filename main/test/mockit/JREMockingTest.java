/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;

import junit.framework.*;

@SuppressWarnings({
   "WaitWhileNotSynced", "UnconditionalWait", "WaitWithoutCorrespondingNotify", "WaitNotInLoop",
   "WaitOrAwaitWithoutTimeout", "UnusedDeclaration"
})
public final class JREMockingTest extends TestCase
{
   public void testMockingOfFile()
   {
      new NonStrictExpectations()
      {
         File file;

         {
            file.exists(); result = true;
         }
      };

      File f = new File("...");
      assertTrue(f.exists());
   }

   // Mocking of native methods ///////////////////////////////////////////////////////////////////////////////////////

   public void testFirstMockingOfNativeMethods() throws Exception
   {
       new Expectations()
       {
           // First mocking: puts mocked class in cache, knowing it has native methods to re-register.
           @Mocked("sleep") final Thread unused = null;
       };

      Thread.sleep(5000);
   }

   public void testSecondMockingOfNativeMethods(@Mocked("isAlive") final Thread mock)
   {
       new Expectations()
       {{
           // Second mocking: retrieves from cache, no longer knowing it has native methods to re-register.
           mock.isAlive(); result = true;
       }};

      assertTrue(mock.isAlive());
   }

   public void testUnmockedNativeMethods() throws Exception
   {
       Thread.sleep(10);
       assertTrue(System.currentTimeMillis() > 0);
   }

   // When a native instance method is called on a regular instance, there is no way to execute its real
   // implementation; therefore, dynamic mocking of native methods is not supported.
   public void testDynamicMockingOfNativeMethod(@Injectable final Thread t)
   {
      new NonStrictExpectations()
      {
         {
            t.isAlive();

            try {
               result = true;
               fail();
            }
            catch (IllegalStateException ignore) {
               // OK
            }
         }
      };
   }

   @Injectable FileOutputStream stream;

   // This interferes with the test runner if regular mocking is applied.
   public void testDynamicMockingOfFileOutputStreamThroughMockField() throws Exception
   {
      new Expectations()
      {
         {
            stream.write((byte[]) any);
         }
      };

      stream.write("Hello world".getBytes());
   }

   // Mocking of java.lang.Object methods /////////////////////////////////////////////////////////////////////////////

   final Object lock = new Object();

   void awaitNotification() throws InterruptedException
   {
      synchronized (lock) {
         lock.wait();
      }
   }

   public void testWaitingWithDynamicPartialMocking() throws Exception
   {
      final Object mockedLock = new Object();

      new Expectations(Object.class)
      {
         {
            mockedLock.wait();
         }
      };

      awaitNotification();
   }

   public void testWaitingWithLocalMockField() throws Exception
   {
      new NonStrictExpectations()
      {
         Object mockedLock;

         {
            mockedLock.wait(); times = 1;
         }
      };

      awaitNotification();
   }
}
