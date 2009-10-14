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

import orderMngr.domain.order.*;

public final class OrderEntryPage
{
   // Data accumulated before submitting the order:
   private String customerId;
   private List<OrderItem> orderItems;

   // Parameters for adding an order item:
   private String productId;
   private String productDescription;
   private int quantity;
   private BigDecimal unitPrice;

   // Item index for removing an item:
   private int itemToRemove;

   // Resulting data when the order is submitted:
   private int orderNo;

   public void load()
   {
      orderItems = new ArrayList<OrderItem>(5);
      // use some web MVC framework service to retrieve item data, either from request parameters
      // or from the HTTPSession
   }

   public void setCustomerId(String customerId)
   {
      this.customerId = customerId;
   }

   public List<OrderItem> getOrderItems()
   {
      return orderItems;
   }

   public void setProductId(String productId)
   {
      this.productId = productId;
   }

   public void setProductDescription(String productDescription)
   {
      this.productDescription = productDescription;
   }

   public void setQuantity(int quantity)
   {
      this.quantity = quantity;
   }

   public void setUnitPrice(BigDecimal unitPrice)
   {
      this.unitPrice = unitPrice;
   }

   public void setItemToRemove(int itemToRemove)
   {
      this.itemToRemove = itemToRemove;
   }

   public int getOrderNo()
   {
      return orderNo;
   }

   public void addItem()
   {
      OrderItem item = new OrderItem(productId, productDescription, quantity, unitPrice);
      orderItems.add(item);
   }

   public void removeItem()
   {
      orderItems.remove(itemToRemove);
   }

   public void submitOrder() throws Exception
   {
      Order order = new OrderFactory().createOrder(customerId, orderItems);
      orderNo = order.getNumber();
   }
}
