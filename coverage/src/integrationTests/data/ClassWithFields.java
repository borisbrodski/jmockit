package integrationTests.data;

public class ClassWithFields
{
   private static int static1;
   private static String static2;
   private static long static3;

   private boolean instance1;
   private Boolean instance2;
   private double instance3;

   public static int getStatic1()
   {
      return static1;
   }

   public static void setStatic1(int static1)
   {
      ClassWithFields.static1 = static1;
   }

   public static void setStatic2(String static2)
   {
      ClassWithFields.static2 = static2;
   }

   public static long getStatic3()
   {
      return static3;
   }

   public static void setStatic3(long static3)
   {
      ClassWithFields.static3 = static3;
   }

   public boolean isInstance1()
   {
      return instance1;
   }

   public void setInstance1(boolean instance1)
   {
      this.instance1 = instance1;
   }

   public void setInstance2(Boolean instance2)
   {
      this.instance2 = instance2;
   }

   public double getInstance3()
   {
      return instance3;
   }

   public void setInstance3(double instance3)
   {
      this.instance3 = instance3;
   }
}
