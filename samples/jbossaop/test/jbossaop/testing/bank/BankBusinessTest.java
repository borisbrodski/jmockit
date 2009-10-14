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
package jbossaop.testing.bank;

import jbossaop.testing.customer.*;
import mockit.*;
import mockit.integration.junit3.*;

/**
 * This class is JMockit's version of the JBoss AOP test from the
 * <a href="http://www.jboss.org/jbossaop/docs/2.0.0.GA/docs/aspect-framework/userguide/en/html/testing.html#testing1">Injecting Mock Objects</a>
 * section in the "Testing with AOP" chapter of the user guide for JBoss AOP 2.
 * <p/>
 * Notice how much simpler this is, when compared to the full code from the JBoss AOP original
 * example. With JMockit, there is no need to use the "Mock Maker" tool, or to write the
 * "BankAccountDAOInterceptor" and "MockService" classes; no configuration file
 * (such as jboss-aop.xml) is required, no external bytecode modification tool (such as aopc) is
 * used, and (assuming the use of JDK 1.6 for <strong>running</strong> the tests - but not
 * necessarily for compiling the sources or running the production code) no extra JVM initialization
 * arguments are needed. (Besides having <em>jmockit.jar</em> in the classpath, when using
 * <em>JDK 1.6</em> it is necessary to also have <em>tools.jar</em> in the classpath; with
 * <em>JDK 1.5</em> it is necessary instead to pass the initialization argument
 * <em>"-javaagent:jmockit.jar"</em> to the JVM.)
 */
public final class BankBusinessTest extends JMockitTestCase
{
   private BankAccount account1;
   private BankAccount account2;
   private Customer customer;

   @Override
   public void setUp()
   {
      account1 = new BankAccount(10);
      account1.setBalance(100);

      account2 = new BankAccount(11);
      account2.setBalance(500);

      customer = new Customer("John", "Doe");
      customer.addAccount(10);
      customer.addAccount(11);
   }

   public void testSumOfAllAccounts()
   {
      new Expectations()
      {
         // Mock fields (which could also have been annotated with @Mocked):
         final BankAccountDAOFactory daoFactory = null; // no instance needed
         BankAccountDAO dao; // Proxy class created and instantiated by JMockit

         // Expected method (including static) invocations:
         {
            BankAccountDAOFactory.getBankAccountDAOSerializer(); returns(dao);
            dao.getBankAccount(10); returns(account1);
            dao.getBankAccount(11); returns(account2);
         }
      };

      BankBusiness business = new BankBusiness();
      double sum = business.getSumOfAllAccounts(customer);
      assertEquals(600, sum, 0);

      // Note that all expected invocations are verified to have actually occurred at this point,
      // even without a explicit call to "Expectations.assertSatisfied()". This happens because
      // JMockit provides automatic and transparent integration with the JUnit 3.8 test runner.
   }
}
