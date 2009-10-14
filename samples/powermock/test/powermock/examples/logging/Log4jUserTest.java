package powermock.examples.logging;

import static org.easymock.EasyMock.expect;

import static org.junit.Assert.assertEquals;

import static org.powermock.api.easymock.PowerMock.createPartialMockAndInvokeDefaultConstructor;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.api.easymock.mockpolicies.Log4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest(Log4jUser.class)
@MockPolicy(Log4jMockPolicy.class)
public class Log4jUserTest
{
   @Test
   public void assertThatLog4jMockPolicyWorks() throws Exception
   {
      Log4jUser tested =
         createPartialMockAndInvokeDefaultConstructor(Log4jUser.class, "getMessage");

      String firstMessage = "first message and ";
      expect(tested.getMessage()).andReturn(firstMessage);
      replayAll();

      String otherMessage = "other message";
      String actual = tested.mergeMessageWith(otherMessage);

      verifyAll();
      assertEquals(firstMessage + otherMessage, actual);
   }
}
