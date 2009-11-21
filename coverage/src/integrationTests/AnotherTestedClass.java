package integrationTests;

@SuppressWarnings({"ControlFlowStatementWithoutBraces"})
public final class AnotherTestedClass
{
   void simpleIf(boolean b)
   {
      if (b) {
         System.out.print("");
      }
   }

   void ifAndElse(boolean b)
   {
      if (b) {
         System.out.print("");
      }
      else {
         System.out.print("other");
      }
   }

   void singleLineIf(boolean b)
   {
      if (b) System.out.print("");
   }

   void singleLineIfAndElse(boolean b)
   {
      if (b) System.out.print(""); else System.out.println("other");
   }
}