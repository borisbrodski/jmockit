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

   void nonBranchingMethodWithUnreachableLines()
   {
      int a = 1;
      assert false;
      System.gc();
   }

   void branchingMethodWithUnreachableLines(int a)
   {
      if (a > 0) {
         assert a < 0;
         System.gc();
      }

      System.runFinalization();
   }

   int methodWithFourDifferentPathsAndSimpleLines(boolean b, int i)
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
      else {
         System.runFinalization();
      }

      return i;
   }

   void methodWithFourDifferentPathsAndSegmentedLines(boolean b, int i)
   {
      if (b) { System.gc(); } else { System.runFinalization(); }

      if (i > 0) { System.gc(); }
      else { System.runFinalization(); }
   }

   boolean singleLineMethodWithMultiplePaths(boolean a, boolean b, boolean c)
   {
      return a || b;
   }
}