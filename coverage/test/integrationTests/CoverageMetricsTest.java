package integrationTests;

import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class CoverageMetricsTest
{
   private final TestedClass tested = new TestedClass();

   @Test
   public void evalOnlySomeCombinations()
   {
      assertTrue(tested.eval1(true, true, 0));
      assertFalse(tested.eval1(true, false, 0));
   }

   @Test
   public void evalBuggyCombination()
   {
      // Only this combination will fail if the third condition in eval1 is changed to "z < 0",
      // which demonstrates that a more sophisticated metric than branch coverage is needed.
      assertTrue(tested.eval1(true, false, 1));
   }

   @Test
   public void evalAllCombinations()
   {
      assertTrue(tested.eval2(true, true, 0));
      assertTrue(tested.eval2(true, false, 1));
      assertFalse(tested.eval2(true, false, 0));
      assertFalse(tested.eval2(false, true, 0));
   }

   @Test
   public void evalOnlyFirstBranch()
   {
      assertFalse(tested.eval3(false, true, false));
   }

   @Test
   public void evalOnlyFirstAndSecondBranches()
   {
      assertFalse(tested.eval4(false, true, false));
      assertFalse(tested.eval4(false, false, false));
      assertFalse(tested.eval4(false, true, true));
      assertFalse(tested.eval4(false, false, true));
      assertTrue(tested.eval4(true, true, false));
      assertTrue(tested.eval4(true, true, true));
   }
}
