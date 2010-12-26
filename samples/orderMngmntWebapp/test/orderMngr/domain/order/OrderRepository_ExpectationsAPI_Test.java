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
package orderMngr.domain.order;

import java.math.*;

import org.junit.*;

import mockit.*;

import orderMngr.service.*;

/**
 * Unit tests for the OrderRepository class, which depends on the {@link Database} class.
 * The tests use expectations to simulate the interaction between OrderRepository and Database.
 */
public final class OrderRepository_ExpectationsAPI_Test
{
   @Mocked
   final Database db = null; // only contain static methods, so no instance is needed

   Order order;

   @Test
   public void createOrder()
   {
      order = new Order(561, "customer");
      final OrderItem orderItem =
         new OrderItem(order, "Prod", "Some product", 3, new BigDecimal("5.20"));
      order.getItems().add(orderItem);

      new Expectations()
      {
         {
            Database.executeInsertUpdateOrDelete(
               withPrefix("insert into order "), order.getNumber(), order.getCustomerId());
            Database.executeInsertUpdateOrDelete(
               withPrefix("insert into order_item "),
               order.getNumber(), orderItem.getProductId(), orderItem.getProductDescription(),
               orderItem.getQuantity(), orderItem.getUnitPrice());
         }
      };

      new OrderRepository().create(order);
   }

   @Test
   public void updateOrder()
   {
      order = new Order(1, "test");

      new Expectations()
      {
         {
            Database.executeInsertUpdateOrDelete(
               withPrefix("update order "), order.getCustomerId(), order.getNumber());
         }
      };

      new OrderRepository().update(order);
   }

   @Test
   public void removeOrder()
   {
      order = new Order(35, "remove");

      new Expectations()
      {
         {
            Database.executeInsertUpdateOrDelete(
               withPrefix("delete from order "), order.getNumber());
         }
      };

      new OrderRepository().remove(order);
   }
}
