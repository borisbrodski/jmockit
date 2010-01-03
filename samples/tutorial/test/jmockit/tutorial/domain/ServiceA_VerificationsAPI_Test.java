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

import org.junit.*;

import mockit.*;

@UsingMocksAndStubs(Database.class)
public final class ServiceA_VerificationsAPI_Test
{
   @Mocked Database onlyStatics;
   @Capturing Email email; // concrete subclass mocked on demand, when loaded

   @Test
   public void doBusinessOperationXyzPersistsData() throws Exception
   {
      final EntityX data = new EntityX(5, "abc", "5453-1");

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
      EntityX data = new EntityX(5, "abc", "5453-1");
      final List<EntityX> items = Arrays.asList(new EntityX(1, "AX5", "someone@somewhere.com"));

      // Invocations that produce a result are recorded, but only those we care about.
      new NonStrictExpectations()
      {
         {
            Database.find(withSubstring("select"), (Object[]) null); result = items;
         }
      };

      new MyBusinessService().doBusinessOperationXyz(data);

      new Verifications()
      {
         {
            email.send();
         }
      };
   }

   @Test(expected = EmailException.class)
   public void doBusinessOperationXyzWithInvalidItemStatus() throws Exception
   {
      new NonStrictExpectations()
      {
         {
            email.addTo((String) withNotNull()); result = new EmailException();
         }
      };

      EntityX data = new EntityX(5, "abc", "5453-1");
      new MyBusinessService().doBusinessOperationXyz(data);

      // Nothing left to verify at this point.
   }
}
