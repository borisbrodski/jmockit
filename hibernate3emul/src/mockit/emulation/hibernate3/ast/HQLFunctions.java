package mockit.emulation.hibernate3.ast;

final class HQLFunctions
{
   public static String lower(String s)
   {
      return s.toLowerCase();
   }
   
   public static int abs(Integer n)
   {
      return Math.abs(n);
   }
}