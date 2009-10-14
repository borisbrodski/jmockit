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

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import static mockit.Mockit.*;
import mockit.integration.junit4.*;

/**
 * State-based unit tests for the OrderRepository class, which depends on the {@linkplain Database}
 * class.
 * The tests use mocks to simulate the interaction between OrderRepository and Database.
 */
@RunWith(JMockit.class)
public final class OrderRepositoryTest
{
   private static PreparedStatement proxyStmt;
   private static ResultSet proxyRS;
   private Order order;
   private OrderItem orderItem;

   @Test
   public void createOrder()
   {
      order = new Order(561, "customer");
      orderItem = new OrderItem(order, "Prod", "Some product", 3, new BigDecimal("5.20"));
      order.getItems().add(orderItem);

      new MockUp<Database>()
      {
         boolean orderInserted;

         @Mock(invocations = 2)
         void executeInsertUpdateOrDelete(String sql, Object... args)
         {
            if (orderInserted) {
               assertTrue(sql.trim().toLowerCase().startsWith("insert into order_item "));
               assertEquals(5, args.length);
               assertEquals(order.getNumber(), args[0]);
               assertEquals(orderItem.getProductId(), args[1]);
               assertEquals(orderItem.getProductDescription(), args[2]);
               assertEquals(orderItem.getQuantity(), args[3]);
               assertEquals(orderItem.getUnitPrice(), args[4]);
            }
            else {
               assertTrue(sql.trim().toLowerCase().startsWith("insert into order "));
               assertEquals(order.getNumber(), args[0]);
               assertEquals(order.getCustomerId(), args[1]);
               orderInserted = true;
            }
         }
      };

      new OrderRepository().create(order);
   }

   @Test
   public void updateOrder()
   {
      order = new Order(1, "test");

      new MockUp<Database>()
      {
         @Mock(invocations = 1)
         void executeInsertUpdateOrDelete(String sql, Object... args)
         {
            assertTrue(sql.trim().toLowerCase().startsWith("update order "));
            String customerId = (String) args[0];
            assertEquals("test", customerId);
            Integer orderNo = (Integer) args[1];
            assertEquals(1, orderNo.intValue());
         }
      };

      new OrderRepository().update(order);
   }

   @Test
   public void removeOrder()
   {
      order = new Order(35, "remove");

      new MockUp<Database>()
      {
         @Mock(minInvocations = 1, maxInvocations = 1) // equivalent to "invocations = 1"
         void executeInsertUpdateOrDelete(String sql, Object... args)
         {
            assertTrue(sql.trim().toLowerCase().startsWith("delete from order "));
            assertEquals(order.getNumber(), args[0]);
         }
      };

      new OrderRepository().remove(order);
   }

   /**
    * Demonstrates use of {@link Mockit#newEmptyProxy(ClassLoader, Class)}.
    */
   @Test
   public void findOrderByNumber()
   {
      order = new Order(1, "test");
      orderItem = new OrderItem(order, "343443", "Some product", 3, new BigDecimal(5));
      order.getItems().add(orderItem);

      setUpMocks(MockDatabase.class);
      setUpMock(MockDatabase.connection().getClass(), MockConnection.class);
      ClassLoader classLoader = OrderRepository.class.getClassLoader();
      proxyStmt = newEmptyProxy(classLoader, PreparedStatement.class);
      setUpMock(proxyStmt.getClass(), MockPreparedStatement.class);
      proxyRS = newEmptyProxy(classLoader, ResultSet.class);
      setUpMock(proxyRS.getClass(), new MockResultSet());

      Order found = new OrderRepository().findByNumber(order.getNumber());

      assertEquals(order, found);
   }

   public static class MockConnection
   {
      @Mock
      public PreparedStatement prepareStatement(String sql)
      {
         assertNotNull(sql);
         return proxyStmt;
      }
   }

   public static class MockPreparedStatement
   {
      @Mock
      public int executeUpdate() { return 1; }

      @Mock
      public ResultSet executeQuery() { return proxyRS; }
   }

   public class MockResultSet
   {
      int callNo;

      MockResultSet() {}

      @Mock
      public boolean next()
      {
         callNo++;
         assertTrue("attempted to read more DB rows than expected", callNo <= 3);
         return callNo < 3;
      }

      @Mock
      public String getString(int columnIndex)
      {
         if (callNo == 1) {
            return order.getCustomerId();
         }

         return columnIndex == 1 ? orderItem.getProductId() : orderItem.getProductDescription();
      }

      @Mock
      public int getInt(int i)
      {
         assertEquals(3, i);
         return orderItem.getQuantity();
      }

      @Mock
      public BigDecimal getBigDecimal(int i)
      {
         assertEquals(4, i);
         return orderItem.getUnitPrice();
      }

      @Mock
      public Statement getStatement() { return proxyStmt; }
   }

   @Test
   public void findOrderByCustomer()
   {
      order = new Order(890, "Cust");

      new MockDatabaseForFindByCustomer();
      stubOutClass(OrderRepository.class, "loadOrderItems");

      List<Order> found = new OrderRepository().findByCustomer(order.getCustomerId());

      assertTrue("Order not found by customer id", found.contains(order));
   }

   public final class MockDatabaseForFindByCustomer extends MockUp<Database>
   {
      private ResultSet mockRS;

      @Mock(invocations = 1)
      public ResultSet executeQuery(String sql, Object... args)
      {
         assertTrue(
            "Invalid Order query: " + sql,
            sql.matches("select.+from\\s+order.*where.+customer_id\\s*=\\s*\\?"));
         assertEquals(1, args.length);
         assertEquals("Cust", args[0]);

         mockRS = new MockUp<ResultSet>()
         {
            private int rowIndex;

            @Mock(invocations = 2)
            boolean next()
            {
               rowIndex++;
               return rowIndex == 1;
            }

            @Mock(invocations = 1)
            int getInt(int i)
            {
               assertEquals(1, i);
               return order.getNumber();
            }
         }.getMockInstance();

         return mockRS;
      }

      @Mock(invocations = 1)
      public void closeStatement(ResultSet result)
      {
         assertSame(mockRS, result);
      }
   }
}
