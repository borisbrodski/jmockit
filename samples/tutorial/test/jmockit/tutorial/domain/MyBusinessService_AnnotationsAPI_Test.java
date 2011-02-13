/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
