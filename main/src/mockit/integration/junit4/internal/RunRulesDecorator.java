package mockit.integration.junit4.internal;

import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;
import mockit.integration.internal.TestRunnerDecorator;
import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.state.TestRun;
import mockit.internal.util.StackTrace;

import org.junit.rules.RunRules;

/**
 * Startup mock that modifies the JUnit 4.9+ rule runner to enable
 * mock setup and expectations specification out of the rule context.
 * <p/>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 * 
 * @author Boris Brodski
 */
@MockClass(realClass = RunRules.class, instantiation = Instantiation.PerMockSetup)
public class RunRulesDecorator extends TestRunnerDecorator {
   public RunRules it;

   @Mock(reentrant = true)
   public void evaluate() throws Throwable {
      Object instance = BlockJUnit4ClassRunnerDecorator.getCurrentTest();
      if (instance == null) {
         it.evaluate();
         return;
      }
      
      Class<? extends Object> testClass = instance.getClass();
      handleMockingOutsideTestMethods(testClass);

      prepareForNextTest();
      JUnit4TestRunnerDecorator.shouldPrepareForNextTest = false;

      TestRun.setRunningIndividualTest(instance);
      TestRun.setSavePointForTestMethod(null);

      try {
         it.evaluate();
      }
      catch (Throwable t) {
         RecordAndReplayExecution.endCurrentReplayIfAny();
         StackTrace.filterStackTrace(t);
         throw t;
      }
   }

   private void handleMockingOutsideTestMethods(Class<?> testClass) {
      TestRun.enterNoMockingZone();
      try {
         updateTestClassState(BlockJUnit4ClassRunnerDecorator.getCurrentTest(), testClass);
      } finally {
         TestRun.exitNoMockingZone();
      }
   }
}
