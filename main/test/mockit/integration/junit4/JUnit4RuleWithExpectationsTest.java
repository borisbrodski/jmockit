package mockit.integration.junit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import mockit.Expectations;
import mockit.Mocked;
import mockit.internal.MissingInvocation;
import mockit.internal.UnexpectedInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Tests Expectations defined within JUnit-Rule block.
 * The defined expectations should behave exactly like expectations defined
 * within <code>&#064;Before</code> block.
 * 
 * @author Boris Brodski
 */
public class JUnit4RuleWithExpectationsTest {
   public static class MyRule implements TestRule {
      public Statement apply(final Statement base, final Description description) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
               new Expectations() {
                  @Mocked
                  Dependency dependency;
                  {
                     Dependency.alwaysTrue();
                     result = new boolean[] {true, false, false, true};
                     times = 4;
                  }
               };
               base.evaluate();
            }
         };
      }
   }

   @Rule
   public MyRule myRule = new MyRule();

   @Test
   public void call4Times() {
      assertTrue(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
      assertTrue(Dependency.alwaysTrue());
   }

   @Test(expected = MissingInvocation.class)
   public void callNothing() {
      
   }
   
   @Test(expected = MissingInvocation.class)
   public void call3Times() {
      assertTrue(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
   }
   
   @Test(expected = UnexpectedInvocation.class)
   public void call5Times() {
      assertTrue(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
      assertFalse(Dependency.alwaysTrue());
      assertTrue(Dependency.alwaysTrue());
      Dependency.alwaysTrue();
   }
}
