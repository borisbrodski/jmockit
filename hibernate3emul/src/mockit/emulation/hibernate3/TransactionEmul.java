package mockit.emulation.hibernate3;

import javax.transaction.*;

import org.hibernate.Transaction;

final class TransactionEmul implements Transaction
{
   public void begin()
   {
   }

   public void commit()
   {
   }

   public void rollback()
   {
   }

   public boolean wasRolledBack()
   {
      return false;
   }

   public boolean wasCommitted()
   {
      return false;
   }

   public boolean isActive()
   {
      return false;
   }

   public void registerSynchronization(Synchronization synchronization)
   {
   }

   public void setTimeout(int seconds)
   {
   }
}
