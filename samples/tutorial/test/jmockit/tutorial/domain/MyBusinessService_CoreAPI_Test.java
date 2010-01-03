/*
 * JMockit Samples
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
package jmockit.tutorial.domain;

import java.util.*;

import jmockit.tutorial.infrastructure.*;
import org.apache.commons.mail.*;

import junit.framework.*;

import mockit.*;

public final class MyBusinessService_CoreAPI_Test extends TestCase
{
   private boolean emailSent;

   public static class MockDatabase
   {
      static int findMethodCallCount;
      static int persistMethodCallCount;

      public static List<EntityX> find(String ql, Object... args)
      {
         assertNotNull(ql);
         assertTrue(args.length > 0);
         findMethodCallCount++;
         return Arrays.asList(new EntityX(1, "AX5", "someone@somewhere.com"));
      }

      public static void persist(Object o)
      {
         assertNotNull(o);
         persistMethodCallCount++;
      }
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      Mockit.stubOut(Database.class, Email.class);

      MockDatabase.findMethodCallCount = 0;
      MockDatabase.persistMethodCallCount = 0;
      Mockit.redefineMethods(Database.class, MockDatabase.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      Mockit.restoreAllOriginalDefinitions();
      super.tearDown();
   }

   public void testDoBusinessOperationXyz() throws Exception
   {
      Mockit.redefineMethods(Email.class, new MockEmail(true));

      EntityX data = new EntityX(5, "abc", "5453-1");

      new MyBusinessService().doBusinessOperationXyz(data);

      assertEquals(1, MockDatabase.findMethodCallCount);
      assertEquals(1, MockDatabase.persistMethodCallCount);
      assertTrue(emailSent);
   }

   public class MockEmail
   {
      final boolean addToShouldSucceed;
      public Email it;

      MockEmail(boolean addToShouldSucceed) { this.addToShouldSucceed = addToShouldSucceed; }

      public Email addTo(String emailAddress) throws EmailException
      {
         assertNotNull(emailAddress);

         if (addToShouldSucceed) {
            return it;
         }
         else {
            throw new EmailException();
         }
      }

      public String send()
      {
         emailSent = true;
         return "";
      }
   }

   public void testDoBusinessOperationXyzWithInvalidCustomerEmailAddress() throws Exception
   {
      Mockit.redefineMethods(Email.class, new MockEmail(false));

      EntityX data = new EntityX(5, "abc", "5453-1");

      try {
         new MyBusinessService().doBusinessOperationXyz(data);
         fail(EmailException.class + " was expected");
      }
      catch (EmailException ignore) {
         // OK, test passed
         assertFalse(emailSent);
      }
   }
}
