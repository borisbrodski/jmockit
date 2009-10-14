/*
 * JMockit: a Java class library for developer testing with "mock methods"
 * Copyright (c) 2006, 2007 Rogério Liesenfeld
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
package orderMngr.domain.customer;

import javax.persistence.*;

@Entity
public class Customer
{
   @Id
   private String id;

   String firstName;

   String lastName;

   String deliveryAddress;

   public Customer()
   {
   }

   public Customer(String id, String firstName, String lastName, String deliveryAddress)
   {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.deliveryAddress = deliveryAddress;
   }

   public String getId()
   {
      return id;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public String getDeliveryAddress()
   {
      return deliveryAddress;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Customer customer = (Customer) o;

      return id.equals(customer.id);
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
}
