package integrationTests.data;

import org.junit.*;

import integrationTests.*;

public final class ClassWithFieldsTest extends CoverageTest
{
   ClassWithFields tested;

   @Test
   public void setGetStatic1()
   {
      ClassWithFields.setStatic1(1);
      ClassWithFields.setStatic1(2);
      assert ClassWithFields.getStatic1() == 2;

      assertFieldCovered("static1");
   }

   @Test
   public void setStatic2()
   {
      ClassWithFields.setStatic2("test");

      assertFieldUncovered("static2");
   }

   @Test
   public void setGetSetStatic3()
   {
      ClassWithFields.setStatic3(1);
      assert ClassWithFields.getStatic3() == 1;
      ClassWithFields.setStatic3(2);

      assertFieldUncovered("static3");
   }

   @Test
   public void setGetInstance1()
   {
      tested.setInstance1(true);
      assert tested.isInstance1();

      assertFieldCovered("instance1", tested);
   }

   @Test
   public void setInstance2()
   {
      tested.setInstance2(false);

      assertFieldUncovered("instance2", tested);
   }

   @Test
   public void setGetSetInstance3()
   {
      tested.setInstance3(2.5);
      assert tested.getInstance3() >= 2.5;
      tested.setInstance3(-0.9);

      assertFieldUncovered("instance3", tested);
   }

   @AfterClass
   public static void verifyDataCoverage()
   {
      verifyDataCoverage(6, 4, 67);
   }
}
