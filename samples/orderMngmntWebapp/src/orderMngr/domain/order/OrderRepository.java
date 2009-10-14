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

import orderMngr.service.*;
import static orderMngr.service.Database.*;

public final class OrderRepository
{
   public void create(Order order)
   {
      String sql = "insert into order (number, customer_id) values (?, ?)";
      int orderNo = order.getNumber();
      Database.executeInsertUpdateOrDelete(sql, orderNo, order.getCustomerId());

      for (OrderItem item : order.getItems()) {
         Database.executeInsertUpdateOrDelete(
            "insert into order_item (order_no, product_id, product_desc, quantity, unit_price) " +
            "values (?, ?, ?, ?, ?)",
            orderNo, item.getProductId(), item.getProductDescription(), item.getQuantity(),
            item.getUnitPrice());
      }
   }

   public void update(Order order)
   {
      String sql = "update order set customer_id=? where number=?";
      Database.executeInsertUpdateOrDelete(sql, order.getCustomerId(), order.getNumber());
   }

   public void remove(Order order)
   {
      String sql = "delete from order where number=?";
      Database.executeInsertUpdateOrDelete(sql, order.getNumber());
   }

   public Order findByNumber(int orderNumber)
   {
      try {
         return loadOrder(orderNumber);
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
   }

   private Order loadOrder(int orderNumber) throws SQLException
   {
      ResultSet result = executeQuery("select customer_id from order where number=?", orderNumber);

      try {
         if (result.next()) {
            String customerId = result.getString(1);
            Order order = new Order(orderNumber, customerId);
            loadOrderItems(order);
            return order;
         }

         return null;
      }
      finally {
         closeStatement(result);
      }
   }

   private void loadOrderItems(Order order) throws SQLException
   {
      ResultSet result =
         executeQuery(
            "select product_id, product_desc, quantity, unit_price from order_item " +
            "where order_number=?", order.getNumber());

      try {
         Collection<OrderItem> items = order.getItems();

         while (result.next()) {
            String productId = result.getString(1);
            String productDescription = result.getString(2);
            int quantity = result.getInt(3);
            BigDecimal unitPrice = result.getBigDecimal(4);

            OrderItem item =
               new OrderItem(order, productId, productDescription, quantity, unitPrice);
            items.add(item);
         }
      }
      finally {
         closeStatement(result);
      }
   }

   public List<Order> findByCustomer(String customerId)
   {
      ResultSet result = executeQuery("select number from order where customer_id=?", customerId);

      try {
         List<Order> orders = new ArrayList<Order>();

         while (result.next()) {
            int orderNumber = result.getInt(1);
            Order order = new Order(orderNumber, customerId);
            loadOrderItems(order);
            orders.add(order);
         }

         return orders;
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
      finally {
         closeStatement(result);
      }
   }
}
