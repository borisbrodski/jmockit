package integrationTests;

@SuppressWarnings({"ControlFlowStatementWithoutBraces"})
public final class AnotherTestedClass
{
   void simpleIf(boolean b)
   {
      if (b) {
         System.gc();
      }
   }

   void ifAndElse(boolean b)
   {
      if (b) {
         System.gc();
      }
      else {
         System.runFinalization();
      }
   }

   void singleLineIf(boolean b)
   {
      if (b) System.gc();
   }

   void singleLineIfAndElse(boolean b)
   {
      if (b) System.gc(); else System.runFinalization();
   }
}