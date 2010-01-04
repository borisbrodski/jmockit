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

import java.math.*;
import java.util.*;

import static jmockit.tutorial.infrastructure.Database.*;
import org.apache.commons.mail.*;

public final class MyBusinessService
{
   // This method can easily be made to be transactional, so that any exception its execution throws
   // would cause a rollback (assuming a transaction gets started in the first place) somewhere up
   // in the call stack.
   public void doBusinessOperationXyz(EntityX data) throws EmailException
   {
      // Locate existing persistent entities of the same entity type (note that the query string is
      // a DSL for querying persistent domain entities, written in terms of the domain, not in terms
      // of relational tables and columns):
      List<EntityX> items =
         find("select item from EntityX item where item.someProperty=?1", data.getSomeProperty());

      // Compute or obtain from another service a total value for the new persistent entity:
      BigDecimal total = new BigDecimal("12.30");
      data.setTotal(total);

      // Persist the entity (no DAO required for such a common, high-level, operation):
      persist(data);

      sendNotificationEmailToCustomer(data, items);
   }

   private void sendNotificationEmailToCustomer(EntityX data, List<EntityX> items)
      throws EmailException
   {
      Email email = new SimpleEmail();
      email.setSubject("Notification about processing of ...");
      email.addTo(data.getCustomerEmail());

      // Other e-mail parameters, such as the host name of the mail server, have defaults defined
      // through external configuration.

      String message = buildNotificationMessage(items);
      email.setMsg(message);

      email.send();
   }

   private String buildNotificationMessage(List<EntityX> items)
   {
      StringBuilder message = new StringBuilder();

      for (EntityX item : items) {
         message.append(item.getSomeProperty()).append(" Total is: ").append(item.getTotal());
      }

      return message.toString();
   }
}
