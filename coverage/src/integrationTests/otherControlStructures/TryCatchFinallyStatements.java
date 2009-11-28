package integrationTests.otherControlStructures;

public final class TryCatchFinallyStatements
{
   void tryCatch()
   {
      try {
         System.gc();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   boolean tryCatchWhichThrowsAndCatchesException()
   {
      try {
         throw new RuntimeException("testing");
      }
      catch (RuntimeException e) {
         return true;
      }
   }
}