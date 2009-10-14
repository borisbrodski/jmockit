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
package orderMngr.domain.customer;

import java.util.*;

import static orderMngr.service.domain.DomainUtil.*;
import orderMngr.service.domain.*;
import orderMngr.service.persistence.*;
import static orderMngr.service.persistence.Persistence.*;

public final class CustomerManager
{
   public Customer findById(String id)
   {
      return load(Customer.class, id);
   }

   public void create(Customer data) throws MissingEntityId, MissingRequiredData, DuplicateCustomer
   {
      if (data.getId() == null) {
         throw new MissingEntityId();
      }

      validateRequiredData(data.getFirstName(), data.getLastName());
      validateUniqueness("", data.getFirstName(), data.getLastName());

      persist(data);
   }

   private void validateUniqueness(String id, String firstName, String lastName)
      throws DuplicateCustomer
   {
      boolean customerWithSameNameExists = Persistence.exists(
         "from Customer c where c.firstName=? and c.lastName=? and c.id <> ?",
         firstName, lastName, id);

      if (customerWithSameNameExists) {
         throw new DuplicateCustomer();
      }
   }

   public void changeNameAndAddress(Customer customer, String newFirstName, String newLastName,
      String newAddress) throws MissingRequiredData, DuplicateCustomer
   {
      validateRequiredData(newFirstName, newLastName);
      validateUniqueness(customer.getId(), newFirstName, newLastName);
      
      customer.firstName = newFirstName;
      customer.lastName = newLastName;
      customer.deliveryAddress = newAddress;
   }

   public void remove(Customer customer)
   {
      delete(customer);
   }

   public List<Customer> findByName(String fullOrPartialName)
   {
      return find(
         "select c from Customer c where lower(c.firstName || ' ' || c.lastName) like ?",
         "%" + fullOrPartialName.toLowerCase() + "%");
   }
}
