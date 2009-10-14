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
import javax.persistence.*;

@Entity
public class OrderItem
{
   @Id @GeneratedValue
   private int id;

   @ManyToOne
   private Order order;

   private String productId;

   private String productDescription;

   private int quantity;

   private BigDecimal unitPrice;

   public OrderItem() {}

   public OrderItem(String productId, String productDescription, int quantity, BigDecimal unitPrice)
   {
      this(null, productId, productDescription, quantity, unitPrice);
   }

   public OrderItem(
      Order order, String productId, String productDescription, int quantity, BigDecimal unitPrice)
   {
      this.order = order;
      this.productId = productId;
      this.productDescription = productDescription;
      this.quantity = quantity;
      this.unitPrice = unitPrice;
   }

   public int getId()
   {
      return id;
   }

   public Order getOrder()
   {
      return order;
   }

   public String getProductId()
   {
      return productId;
   }

   public String getProductDescription()
   {
      return productDescription;
   }

   public int getQuantity()
   {
      return quantity;
   }

   public BigDecimal getUnitPrice()
   {
      return unitPrice;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      OrderItem orderItem = (OrderItem) o;

      if (!productId.equals(orderItem.productId)) return false;

      return true;
   }

   @Override
   public int hashCode()
   {
      return productId.hashCode();
   }
}
