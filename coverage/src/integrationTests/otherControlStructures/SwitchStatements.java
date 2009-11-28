package integrationTests.otherControlStructures;

public final class SwitchStatements
{
   void switchStatementWithSparseCases(char c)
   {
      switch (c) {
         case 'A':
            System.gc();
            break;
         case 'f':
         {
            boolean b = true;
            System.gc();
            System.runFinalization();
            break;
         }
         case '\0': return;
         default:
            throw new IllegalArgumentException();
      }
   }

   void switchStatementWithCompactCases(int i)
   {
      switch (i) {
         case 1:
            System.gc();
            break;
         case 2:
         {
            boolean b = true;
            System.gc();
            System.runFinalization();
            break;
         }
         case 4: return;
         default:
            throw new IllegalArgumentException();
      }
   }
}