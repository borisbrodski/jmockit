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

import org.junit.*;

import mockit.*;

import static java.util.Arrays.*;
import jmockit.tutorial.infrastructure.*;
import org.apache.commons.mail.*;

public final class MyBusinessService_VerificationsAPI_Test
{
   @Mocked Database onlyStatics;
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
