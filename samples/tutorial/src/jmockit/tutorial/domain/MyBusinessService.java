/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package jmockit.tutorial.domain;

import java.math.*;
import java.util.*;

import static jmockit.tutorial.infrastructure.Database.*;
import org.apache.commons.mail.*;

/**
 * This class makes use of several idioms which would prevent unit testing with more "conventional" mocking tools.
 * <p/>
 * One of these idioms is the use of a <em>static persistence facade</em> (the
 * {@linkplain jmockit.tutorial.infrastructure.Database Database} class) for high-level database operations in a
 * thread-bound work unit.
 * Since all interaction with the facade is through {@code static} methods, this class could not be unit tested with a
 * tool which only supports <em>mock objects</em>. With JMockit, however, tests become as simple as they could possibly
 * be.
 * <p/>
 * Another idiom which is incompatible with other tools is the direct instantiation and use of external dependencies,
 * such as the <a href="http://commons.apache.org/email">Apache Commons EMail</a> API, used here to send notification
 * e-mails. As demonstrated here, sending an e-mail is simply a matter of instantiating the appropriate {@code Email}
 * subclass, setting the necessary data items, and calling the {@code send()} method. It is certainly not a good use
 * case for <em>Dependency Injection</em> (DI).
 * <p/>
 * So, usage of this business service class is as simple as it gets:
 * {@code new MyBusinessService().doBusinessOperationXyz(data)}. No need to make it stateless, or worse, a
 * <em>singleton</em>. (Although not shown in this simple example, it is a great idea to have stateful service objects
 * with operation-specific state passed in a constructor and assigned to {@code final} fields.)
 * Classes like this one are typically specific to a single <em>use case</em>, which makes them inherently non-reusable
 * in different contexts/applications. As such, they can be made {@code final} to reflect the fact that they are not
 * supposed to be extended through inheritance. (Being {@code final} would prevent them from being mocked with other
 * tools, in case unit tests for higher-level classes are desired. However, there is no reason we should avoid
 * <em>designing for extension</em>, whereby making some classes/methods {@code final} is part of the game.)
 */
public final class MyBusinessService
{
   // This method can easily be made transactional, so that any exception thrown during its execution causes a rollback
   // somewhere up in the call stack (assuming a transaction gets started in the first place).
   public void doBusinessOperationXyz(EntityX data) throws EmailException
   {
      // Locate existing persistent entities of the same entity type (note that the query string is a DSL for querying
      // persistent domain entities, written in terms of the domain, not in terms of relational tables and columns):
      List<EntityX> items = find("select item from EntityX item where item.someProperty=?1", data.getSomeProperty());

      // Compute or obtain from another service a total value for the new persistent entity:
      BigDecimal total = new BigDecimal("12.30");
      data.setTotal(total);

      // Persist the entity (no DAO required for such a common, high-level, operation):
      persist(data);

      sendNotificationEmail(data, items);
   }

   private void sendNotificationEmail(EntityX data, List<EntityX> items) throws EmailException
   {
      Email email = new SimpleEmail();
      email.setSubject("Notification about processing of ...");
      email.addTo(data.getCustomerEmail());

      // Other e-mail parameters, such as the host name of the mail server, have defaults defined through external
      // configuration.

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
