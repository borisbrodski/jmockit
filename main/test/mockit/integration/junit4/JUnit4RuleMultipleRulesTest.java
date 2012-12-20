package mockit.integration.junit4;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test setup of multiple mocks from multiple JUnit-Rules.
 * 
 * @author Boris Brodski
 */
public class JUnit4RuleMultipleRulesTest extends JUnit4RuleTestBase {
   public static class MyRule0 implements TestRule {
      public Statement apply(final Statement base, final Description description) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
               mockTargetEveryOtherTime(0);
               base.evaluate();
            }
         };
      }
   }

   public static class MyRule1 implements TestRule {
      public Statement apply(final Statement base, final Description description) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
               mockTargetEveryOtherTime(1);
               base.evaluate();
            }
         };
      }
   }

   public static class MyRuleX implements TestRule {
      private int number;

      public MyRuleX(int nummer) {
         this.number = nummer;
      }

      public Statement apply(final Statement base, final Description description) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
               mockTargetEveryOtherTime(number);
               base.evaluate();
            }
         };
      }
   }

   @Rule
   public MyRule0 myRule0 = new MyRule0();
   @Rule
   public MyRule1 myRule1 = new MyRule1();
   @Rule
   public MyRuleX myRule2 = new MyRuleX(2);
   @Rule
   public MyRuleX myRule3 = new MyRuleX(3);
   @Rule
   public MyRuleX myRule4 = new MyRuleX(4);
   @Rule
   public MyRuleX myRule5 = new MyRuleX(5);


   @BeforeClass
   public static void beforeClass() {
      init();
   }

   @AfterClass
   public static void afterClass() {
      verifyAllMocks();
   }


   @Test
   public void testMocking1() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
      assertEquals(!MockingTarget1.alwaysTrue(), mockingTargetMocked[1]);
      assertEquals(!MockingTarget2.alwaysTrue(), mockingTargetMocked[2]);
      assertEquals(!MockingTarget3.alwaysTrue(), mockingTargetMocked[3]);
      assertEquals(!MockingTarget4.alwaysTrue(), mockingTargetMocked[4]);
      assertEquals(!MockingTarget5.alwaysTrue(), mockingTargetMocked[5]);
   }

   @Test
   public void testMocking2() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
      assertEquals(!MockingTarget1.alwaysTrue(), mockingTargetMocked[1]);
      assertEquals(!MockingTarget2.alwaysTrue(), mockingTargetMocked[2]);
      assertEquals(!MockingTarget3.alwaysTrue(), mockingTargetMocked[3]);
      assertEquals(!MockingTarget4.alwaysTrue(), mockingTargetMocked[4]);
      assertEquals(!MockingTarget5.alwaysTrue(), mockingTargetMocked[5]);
   }

   @Test
   public void testMocking3() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
      assertEquals(!MockingTarget1.alwaysTrue(), mockingTargetMocked[1]);
      assertEquals(!MockingTarget2.alwaysTrue(), mockingTargetMocked[2]);
      assertEquals(!MockingTarget3.alwaysTrue(), mockingTargetMocked[3]);
      assertEquals(!MockingTarget4.alwaysTrue(), mockingTargetMocked[4]);
      assertEquals(!MockingTarget5.alwaysTrue(), mockingTargetMocked[5]);
   }

   @Test
   public void testMocking4() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
      assertEquals(!MockingTarget1.alwaysTrue(), mockingTargetMocked[1]);
      assertEquals(!MockingTarget2.alwaysTrue(), mockingTargetMocked[2]);
      assertEquals(!MockingTarget3.alwaysTrue(), mockingTargetMocked[3]);
      assertEquals(!MockingTarget4.alwaysTrue(), mockingTargetMocked[4]);
      assertEquals(!MockingTarget5.alwaysTrue(), mockingTargetMocked[5]);
   }

   @Test
   public void testMocking5() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
      assertEquals(!MockingTarget1.alwaysTrue(), mockingTargetMocked[1]);
      assertEquals(!MockingTarget2.alwaysTrue(), mockingTargetMocked[2]);
      assertEquals(!MockingTarget3.alwaysTrue(), mockingTargetMocked[3]);
      assertEquals(!MockingTarget4.alwaysTrue(), mockingTargetMocked[4]);
      assertEquals(!MockingTarget5.alwaysTrue(), mockingTargetMocked[5]);
   }
}
