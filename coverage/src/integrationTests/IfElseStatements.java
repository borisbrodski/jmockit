package integrationTests;

@SuppressWarnings({"ControlFlowStatementWithoutBraces"})
public final class IfElseStatements
{
   void simpleIf(boolean b)
   {
      if (b) {
         System.gc(); System.runFinalization();
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

   void methodWithFourDifferentPathsAndSimpleLines(boolean b, int i)
   {
      if (b) {
         System.gc();
      }
      else {
         System.runFinalization();
      }

      if (i > 0) {
         System.gc();
      }
   }

   void methodWithFourDifferentPathsAndSegmentedLines(boolean b, int i)
   {
      if (b) { System.gc(); } else { System.runFinalization(); }

      if (i > 0) { System.gc(); }
      else { System.runFinalization(); }
   }

   boolean singleLineMethodWithMultiplePaths(boolean a, boolean b)
   {
      //noinspection RedundantIfStatement
      if (a || b)
         return true;
      else
         return false;
   }

   // Must return the same value of x as it was called with. Some paths will fail that requirement.
   @SuppressWarnings({"AssignmentToMethodParameter"})
   public int returnInput(int x, boolean a, boolean b, boolean c)
   {
      if (a) {
         x++;
      }

      if (b) {
         x--;
      }

      if (c) {
         //noinspection SillyAssignment
         x = x;
      }

      return x;
   }
}