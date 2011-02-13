/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package jmockit.tutorial.domain;

import org.junit.*;

import mockit.*;

import static java.util.Arrays.*;
import jmockit.tutorial.infrastructure.*;
import org.apache.commons.mail.*;

public final class MyBusinessService_VerificationsAPI_Test
{
   @Mocked(stubOutClassInitialization = true) Database onlyStatics;
   @Capturing Email email; // concrete subclass mocked on demand, when loaded

   final EntityX data = new EntityX(5, "abc", "someone@somewhere.com");

   @Test
   public void doBusinessOperationXyzPersistsData() throws Exception
   {
      // No expectations recorded in this case.
      
      new MyBusinessService().doBusinessOperationXyz(data);

      new Verifications()
      {
         { Database.persist(data); }
      };
   }

   @Test
   public void doBusinessOperationXyzFindsItemsAndSendsNotificationEmail() throws Exception
   {
      // Invocations that produce a result are recorded, but only those we care about.
      new NonStrictExpectations()
      {
         {
            Database.find(withSubstring("select"), (Object[]) null);
            result = asList(new EntityX(1, "AX5", "someone@somewhere.com"));
         }
      };

      new MyBusinessService().doBusinessOperationXyz(data);

      new VerificationsInOrder()
      {
         {
            email.addTo(data.getCustomerEmail());
            email.setMsg(withNotEqual(""));
            email.send();
         }
      };
   }
}
