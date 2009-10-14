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
import java.util.*;

public final class OrderFactory
{
   // Just for simplicity. In a real application, a more robust mechanism would be used for
   // generating sequential order numbers, such as a database sequence or identity column.
   private static int nextOrderNo = 1;

   public Order createOrder(String customerId, List<OrderItem> items)
      throws MissingOrderItems, InvalidOrdemItem, DuplicateOrdemItem
   {
      if (items.isEmpty()) {
         throw new MissingOrderItems();
      }

      validateOrderItems(items);

      Order order = new Order(nextOrderNo++, customerId);
      order.getItems().addAll(items);

      new OrderRepository().create(order);

      return order;
   }

   private void validateOrderItems(List<OrderItem> items)
      throws InvalidOrdemItem, DuplicateOrdemItem
   {
      for (OrderItem item : items) {
         if (item.getQuantity() <= 0 || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrdemItem(
               "Quantity=" + item.getQuantity() + ", Unit Price=" + item.getUnitPrice());
         }

         if (new HashSet<OrderItem>(items).size() < items.size()) {
            throw new DuplicateOrdemItem();
         }
      }
   }
}
