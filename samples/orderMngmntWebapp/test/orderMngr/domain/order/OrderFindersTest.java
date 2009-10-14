/*
 * JMockit Samples
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
package orderMngr.domain.order;

import java.math.*;
import java.sql.*;
import java.util.*;

import mockit.*;
import mockit.integration.junit4.*;
import orderMngr.service.*;
import org.junit.*;

public final class OrderFindersTest extends JMockitTest
{
   @Mocked private final Database db = null;
   @Mocked private ResultSet result;
   private Order order;

   @Test
   public void findOrderByNumber() throws Exception
   {
      // Set up state.
      order = new Order(1, "test");
      OrderItem orderItem = new OrderItem(order, "343443", "Some product", 3, new BigDecimal(5));
      order.getItems().add(orderItem);

      // Set expectations:
      new Expectations()
      {
         @Mocked private ResultSet result2;

         {
            Database.executeQuery(
               withEqual("select customer_id from order where number=?"),
               withEqual(order.getNumber()));
            returns(result);

            result.next();
            returns(true);
            result.getString(1);
            returns(order.getCustomerId());

            Database.executeQuery(
               withMatch("select .+ from order_item where .+"), withEqual(order.getNumber()));
            returns(result2);
            result2.next();
            returns(true);
            result2.getString(1);
            result2.getString(2);
            result2.getInt(3);
            result2.getBigDecimal(4);
            result2.next();
            returns(false);
            Database.closeStatement(result2);

            Database.closeStatement(result);
         }
      };

      // Exercise code under test:
      Order found = new OrderRepository().findByNumber(order.getNumber());

      // Verify results:
      assertEquals(order, found);
   }

   @Test
   public void findOrderByCustomer() throws Exception
   {
      final String customerId = "Cust";
      order = new Order(890, customerId);

      new Expectations()
      {
         @Mocked(methods = "loadOrderItems") private OrderRepository repository;

         {
            Database.executeQuery(
               withMatch("select.+from\\s+order.*where.+customer_id\\s*=\\s*\\?"),
               withEqual(customerId));
            returns(result);

            result.next(); returns(true);
            result.getInt(1); returns(order.getNumber());
            invoke(repository, "loadOrderItems", order);
            result.next(); returns(false);

            Database.closeStatement(withSameInstance(result));
            endRecording();

            List<Order> found = repository.findByCustomer(customerId);

            assertTrue("Order not found by customer id", found.contains(order));
         }
      };
   }
}
