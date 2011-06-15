/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.util.*;

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

   public void testMockingOfCalendar()
   {
      final Calendar calCST = new GregorianCalendar(2010, 4, 15);

      new NonStrictExpectations(Calendar.class) {{
         Calendar.getInstance(TimeZone.getTimeZone("CST")); result = calCST;
      }};

      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
      assertSame(calCST, cal);
      assertEquals(2010, cal.get(Calendar.YEAR));

      assertNotSame(calCST, Calendar.getInstance(TimeZone.getTimeZone("PST")));
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

   // Un-mockable JRE classes /////////////////////////////////////////////////////////////////////////////////////////

   public void testAttemptToMockJREClassThatIsNotMockable()
   {
      try {
         new Expectations() { Class<?> mockClass; };
         fail();
      }
      catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().contains("java.lang.Class"));
      }
   }
}
