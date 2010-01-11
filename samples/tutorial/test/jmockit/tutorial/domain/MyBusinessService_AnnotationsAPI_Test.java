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

import org.junit.*;

import jmockit.tutorial.domain.MyBusinessService_AnnotationsAPI_Test.*;
import org.apache.commons.mail.*;
import static org.junit.Assert.*;

import mockit.*;

import jmockit.tutorial.infrastructure.*;

@UsingMocksAndStubs({MockDatabase.class, Email.class})
public final class MyBusinessService_AnnotationsAPI_Test
{
   @MockClass(realClass = Database.class, stubs = "<clinit>")
   public static class MockDatabase
   {
      @Mock(invocations = 1)
      public static List<EntityX> find(String ql, Object... args)
      {
         assertNotNull(ql);
         assertTrue(args.length > 0);
         return Arrays.asList(new EntityX(1, "AX5", "someone@somewhere.com"));
      }

      @Mock(maxInvocations = 1)
      public static void persist(Object o) { assertNotNull(o); }
   }

   final EntityX data = new EntityX(5, "abc", "5453-1");

   @Test
   public void doBusinessOperationXyz() throws Exception
   {
      new MockUp<Email>()
      {
         @Mock(invocations = 1)
         String send() { return ""; }
      };

      new MyBusinessService().doBusinessOperationXyz(data);
   }

   @Test(expected = EmailException.class)
   public void doBusinessOperationXyzWithInvalidEmailAddress() throws Exception
   {
      new MockUp<Email>()
      {
         @Mock
         Email addTo(String emailAddress) throws EmailException
         {
            assertNotNull(emailAddress);
            throw new EmailException();
         }
         
         @Mock(invocations = 0)
         String send() { return null; }
      };

      new MyBusinessService().doBusinessOperationXyz(data);
   }
}
