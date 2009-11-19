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
package orderMngr.web.submitOrder;

import java.math.*;
import java.util.*;

import org.junit.*;

import mockit.*;

import orderMngr.domain.order.*;
import static org.junit.Assert.*;

public final class OrderEntryPageTest
{
   private final OrderEntryPage page = new OrderEntryPage();
   private int orderNo;
   private String customerId;

   @Test
   public void submitOrder() throws Exception
   {
      customerId = "889000";
      orderNo = 464;

      new MockUp<OrderFactory>()
      {
         @Mock(invocations = 1)
         public Order createOrder(String custId, List<OrderItem> items)
         {
            assertEquals(customerId, custId);
            assertEquals(page.getOrderItems(), items);
            return new Order(orderNo, customerId);
         }
      };

      page.load();
      page.setCustomerId(customerId);
      addItemToEndOfList();

      assertEquals(0, page.getOrderNo());
      page.submitOrder();

      assertEquals(orderNo, page.getOrderNo());
   }

   private void addItemToEndOfList()
   {
      page.getOrderItems().add(new OrderItem("3934", "test item", 2, new BigDecimal(20)));
   }

   // Doesn't require mocks.
   @Test
   public void addItemToEmptyList()
   {
      String productId = "393034";
      page.setProductId(productId);
      String productDescription = "Domain-Driven Design, by Eric Evans";
      page.setProductDescription(productDescription);
      int quantity = 1;
      page.setQuantity(quantity);
      BigDecimal unitPrice = new BigDecimal("49.99");
      page.setUnitPrice(unitPrice);

      page.load();
      page.addItem();

      assertEquals(1, page.getOrderItems().size());
      OrderItem item = page.getOrderItems().get(0);
      assertEquals(productId, item.getProductId());
      assertEquals(productDescription, item.getProductDescription());
      assertEquals(quantity, item.getQuantity());
      assertEquals(unitPrice, item.getUnitPrice());
   }

   // Doesn't require mocks.
   @Test
   public void removeOnlyItem()
   {
      page.setItemToRemove(0);
      page.load();
      addItemToEndOfList();

      page.removeItem();

      assertEquals(0, page.getOrderItems().size());
   }

   // Doesn't require mocks.
   @Test(expected = IndexOutOfBoundsException.class)
   public void removeItemWithInvalidIndex()
   {
      page.setItemToRemove(1);
      page.load();
      addItemToEndOfList();

      page.removeItem();
   }

   // Doesn't require mocks.
   @Test
   public void submitOrderWithMissingData()
   {
      page.load();

      try {
         page.submitOrder();
         fail();
      }
      catch (Exception ignore) {
         assertEquals(0, page.getOrderNo());
      }
   }
}
