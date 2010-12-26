/*
 * JMockit
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

   public void testSecondMockingOfNativeMethods() throws Exception
   {
       new Expectations()
       {
           // Second mocking: retrieves from cache, no longer knowing it has native methods to re-register.
           @Mocked("sleep") final Thread unused = null;
       };

      Thread.sleep(5000);
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
