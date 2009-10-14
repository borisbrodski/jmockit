package jbossaop.testing.bank;

import jbossaop.testing.customer.*;

public final class BankBusiness
{
   private final BankAccountDAO bankAccountDAO;

   public BankBusiness()
   {
      // Note that this dependency could be obtained by directly instantiating a DAO implementation,
      // and it still could be easily unit tested with JMockit. Therefore, if enabling unit testing
      // was the only reason to have the DAO interfaces and the DAO factories, the application
      // architecture could be significantly simplified by getting rid of them all, and using
      // concrete DAO implementation classes. Or even better, consider NOT using entity-specific
      // DAOs at all, instead simply using JPA or another ORM API directly or (preferentially) from
      // behind a static facade.
      bankAccountDAO = BankAccountDAOFactory.getBankAccountDAOSerializer();
   }

   public boolean creditCheck(Customer c, double amount)
   {
      return getSumOfAllAccounts(c) < amount * 0.4;
   }

   public double calculateInterest(BankAccount account)
   {
      int balance = account.getBalance();

      if (balance < 1000) {
         return 0.01;
      }
      else if (balance < 10000) {
         return 0.02;
      }
      else if (balance < 100000) {
         return 0.03;
      }
      else if (balance < 1000000) {
         return 0.05;
      }
      else {
         return 0.06;
      }
   }

   public double getSumOfAllAccounts(Customer c)
   {
      double sum = 0;

      for (long accountNo : c.getAccounts()) {
         BankAccount a = bankAccountDAO.getBankAccount(accountNo);

         if (a != null) {
            sum += a.getBalance();
         }
      }

      return sum;
   }
}
