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
import java.util.ArrayList;
import java.util.*;

import org.junit.*;

import mockit.*;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

public final class OrderFactory_AnnotationsAPI_Test
{
   @Test
   public void createOrder() throws Exception
   {
      String customerId = "123";
      List<OrderItem> items = asList(
         new OrderItem("393439493", "Core Java 5 6ed", 2, new BigDecimal("45.00")),
         new OrderItem("04940458", "JUnit Recipes", 1, new BigDecimal("49.95")));

      MockOrder mockOrder = new MockOrder(customerId);

      new MockUp<OrderRepository>()
      {
         @Mock(invocations = 1)
         void create(Order order)
         {
            assertNotNull(order);
         }
      };

      Order order = new OrderFactory().createOrder(customerId, items);

      assertNotNull(order);
      assertEquals(items, mockOrder.getItems());
   }

   static final class MockOrder extends MockUp<Order>
   {
      static String expectedCustomerId;
      final Collection<OrderItem> items = new ArrayList<OrderItem>();

      MockOrder(String customerId) { expectedCustomerId = customerId; }

      @Mock(invocations = 1)
      void $init(int number, String actualCustomerId) // TODO: can't be a mock constructor; check it
      {
         assertTrue(number > 0);
         assertEquals(expectedCustomerId, actualCustomerId);
      }

      @Mock(invocations = 1)
      Collection<OrderItem> getItems()
      {
         return items;
      }
   }

   // The following tests are here just for completeness, since they have no need for mocks.

   @Test(expected = MissingOrderItems.class)
   public void createOrderWithEmptyItemList() throws Exception
   {
      new OrderFactory().createOrder("45", Collections.<OrderItem>emptyList());
   }

   @Test(expected = InvalidOrderItem.class)
   public void createOrderWithInvalidItemQuantity() throws Exception
   {
      List<OrderItem> items = asList(
         new OrderItem("393439493", "Core Java 5 6ed", 0, new BigDecimal("45.00")));

      new OrderFactory().createOrder("45", items);
   }

   @Test(expected = InvalidOrderItem.class)
   public void createOrderWithInvalidItemUnitPrice() throws Exception
   {
      List<OrderItem> items = asList(
         new OrderItem("393439493", "Core Java 5 6ed", 1, new BigDecimal("-5.20")));

      new OrderFactory().createOrder("45", items);
   }

   @Test(expected = DuplicateOrderItem.class)
   public void createOrderWithDuplicateItem() throws Exception
   {
      List<OrderItem> items = asList(
         new OrderItem("39", "Core Java 5 6ed", 1, new BigDecimal("45.00")),
         new OrderItem("39", "Xyz", 1, new BigDecimal("67.50")));

      new OrderFactory().createOrder("45", items);
   }
}
