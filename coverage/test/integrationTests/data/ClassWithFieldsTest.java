package integrationTests.data;

import org.junit.*;

public final class ClassWithFieldsTest
{
   @Test
   public void setGetStatic1()
   {
      ClassWithFields.setStatic1(1);
      ClassWithFields.setStatic1(2);
      assert ClassWithFields.getStatic1() == 2;
   }

   @Test
   public void setStatic2()
   {
      ClassWithFields.setStatic2("test");
   }

   @Test
   public void setGetSetStatic3()
   {
      ClassWithFields.setStatic3(1);
      assert ClassWithFields.getStatic3() == 1;
      ClassWithFields.setStatic3(2);
   }

   @Test
   public void setGetInstance1()
   {
      ClassWithFields instance = new ClassWithFields();
      instance.setInstance1(true);
      assert instance.isInstance1();
   }

   @Test
   public void setInstance2()
   {
      ClassWithFields instance = new ClassWithFields();
      instance.setInstance2(false);
   }

   @Test
   public void setGetSetInstance3()
   {
      ClassWithFields instance = new ClassWithFields();
      instance.setInstance3(2.5);
      assert instance.getInstance3() >= 2.5;
      instance.setInstance3(-0.9);
   }
}
