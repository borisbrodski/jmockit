package mockit.integration.junit4;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test setup of the mocks from a JUnit-Rule within a test class with:
 * <ul>
 *   <li><code>&#064;BeforeClass</code>-block
 *   <li><code>&#064;Before</code>-block
 *   <li><code>&#064;After</code>-block
 *   <li><code>&#064;AfterClass</code>-block
 * </ul>
 * 
 * 
 * @author Boris Brodski
 */
public class JUnit4RuleWithBeforesAndAftersTest extends JUnit4RuleTestBase {
   public static class MyRule implements TestRule {
      public Statement apply(final Statement base, final Description description) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
               mockTargetEveryOtherTime(0);
               mockTargetEveryOtherTime(1);
               base.evaluate();
            }
         };
      }
   }

   @Rule
   public MyRule myRule = new MyRule();

   @BeforeClass
   public static void beforeClass() {
      init();
      mockTargetEveryOtherTime(2);
      mockTargetEveryOtherTime(3);
   }

   @AfterClass
   public static void afterClass() {
      verifyAllMocks();
   }

   @Before
   public void before() {
      mockTargetEveryOtherTime(4);
      mockTargetEveryOtherTime(5);
   }

   @After
   public void after() {
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
