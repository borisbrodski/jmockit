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

import orderMngr.service.domain.*;
import static orderMngr.service.persistence.Persistence.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.integration.junit4.*;

/**
 * These are integration tests which can optionally be run with Hibernate 3 Emulation, which is an
 * <strong>external mock</strong> (a <strong>fake implementation</strong> of the Hibernate API).
 * <p/>
 * In order for the emulator to be used when running these tests, the usual "-javaagent" JVM
 * argument must be specified as <code>-javaagent:jmockit.jar=hibernate</code> (inserting the
 * correct path to jmockit.jar if necessary), when using a conventional Hibernate configuration
 * (that is, the <code>org.hibernate.Configuration</code> class). Alternativelly, if tools.jar from
 * the JDK 1.6 and jmockit-hibernate3emul.jar are in the classpath, then the emulator will be used
 * automatically, without the need for any JVM initialization parameter. In this last case, the
 * use of the "@RunWith(JMockit.class)" annotation is necessary.
 * <p/>
 * If run without the emulator, the tests should still pass, as long as an appropriate Hibernate
 * session factory and the corresponding relational database are available. The use of the emulator
 * allows the tests to be run "in memory", without any access to the real Hibernate implementation,
 * and therefore to any real database.
 * <p/>
 * All access to the Hibernate API here and in {@link CustomerManager} goes through the
 * {@link orderMngr.service.persistence.Persistence} static facade, which is only a convenience
 * class. Once the emulator is in use, the Hibernate API can be used from anywhere and all calls to
 * it are transparently redirected to the fake implementation.
 */
@RunWith(JMockit.class)
public final class CustomerTest extends DomainTest
{
   private final CustomerManager manager = new CustomerManager();

   @Test
   public void findCustomerById()
   {
      Customer customer = newCustomer();
      String id = customer.getId();

      Customer found = manager.findById(id);

      assertPersisted(id, found);
   }

   private Customer newCustomer()
   {
      Customer customer = newCustomerData();
      persist(customer);
      return customer;
   }

   private Customer newCustomerData()
   {
      return new Customer("C01", "John", "Smith", "123 Fake Street");
   }

   @Test
   public void createCustomer() throws Exception
   {
      Customer data = newCustomerData();

      manager.create(data);

      assertPersisted(data.getId(), data);
   }

   @Test(expected = MissingEntityId.class)
   public void createCustomerWithMissingId() throws Exception
   {
      Customer data = new Customer(null, "John", "Smith", "123 Fake Street");
      manager.create(data);
   }

   @Test(expected = MissingRequiredData.class)
   public void createCustomerWithMissingName() throws Exception
   {
      Customer data = new Customer("GH", "", null, "123 Fake Street");
      manager.create(data);
   }

   @Test(expected = DuplicateCustomer.class)
   public void createCustomerWithDuplicateName() throws Exception
   {
      Customer customer = newCustomer();
      Customer data =
         new Customer(
            customer.getId() + "X", customer.getFirstName(), customer.getLastName(), null);

      manager.create(data);
   }

   @Test
   public void editCustomer() throws Exception
   {
      Customer customer = newCustomer();
      String newAddress = "456 Another Street";

      manager.changeNameAndAddress(customer, "John", "Smith", newAddress);

      assertUpdated(customer);
      assertEquals(newAddress, customer.getDeliveryAddress());
   }

   @Test(expected = DuplicateCustomer.class)
   public void editCustomerWithDuplicateNewName() throws Exception
   {
      Customer customer1 = newCustomer();
      Customer customer2 = new Customer("XYZ", "Another", "Name", null);
      persist(customer2);

      manager.changeNameAndAddress(
         customer2, customer1.getFirstName(), customer1.getLastName(), "");
   }

   @Test
   public void removeCustomer()
   {
      Customer customer = newCustomer();

      manager.remove(customer);

      assertDeleted(customer);
   }

   @Test
   public void findCustomersByName()
   {
      List<Customer> found = manager.findByName("none");
      assertTrue("found when shouldn't", found.isEmpty());

      Customer customer = newCustomer();

      found = manager.findByName(customer.getLastName());
      assertTrue("not found by last name", found.contains(customer));

      found = manager.findByName(customer.getFirstName());
      assertTrue("not found by first name", found.contains(customer));

      found = manager.findByName(customer.getFirstName() + " " + customer.getLastName());
      assertTrue("not found by full name", found.contains(customer));

      found = manager.findByName("");
      assertTrue("not found by any name", found.contains(customer));
   }
}
