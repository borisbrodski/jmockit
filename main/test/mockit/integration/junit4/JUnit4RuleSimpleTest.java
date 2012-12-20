package mockit.integration.junit4;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test setup of a mock from a JUnit-Rule within a simple test class without
 * any initialization block, like <code>&#064;Before</code>.
 * 
 * @author Boris Brodski
 */
public class JUnit4RuleSimpleTest extends JUnit4RuleTestBase  {
   public static class MyRule implements TestRule {
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

   @Rule
   public MyRule myRule = new MyRule();

   @Test
   public void testMocking1() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
   }

   @Test
   public void testMocking2() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
   }

   @Test
   public void testMocking3() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
   }

   @Test
   public void testMocking4() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
   }

   @Test
   public void testMocking5() {
      assertEquals(!MockingTarget0.alwaysTrue(), mockingTargetMocked[0]);
   }
}
