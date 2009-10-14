package integrationTests;

public final class TestedClass
{
   public boolean eval1(boolean x, boolean y, int z)
   {
      return x && (y || z > 0);
   }

   public boolean eval2(boolean x, boolean y, int z)
   {
      return x && (y || z > 0);
   }

   public boolean eval3(boolean x, boolean y, boolean z)
   {
      return x && (y || z);
   }

   public boolean eval4(boolean x, boolean y, boolean z)
   {
      return x && (y || z);
   }
}
