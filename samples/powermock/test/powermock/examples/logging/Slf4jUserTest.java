package powermock.examples.logging;

import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.lang.reflect.Proxy;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

import org.powermock.api.easymock.mockpolicies.*;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

/**
 * Unit tests that assert that the {@link Slf4jMockPolicy} works.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
public class Slf4jUserTest
{
   @Test
   public void assertSlf4jMockPolicyWorks() throws Exception
   {
      Slf4jUser tested = new Slf4jUser();

      Logger logger = Whitebox.getInternalState(Slf4jUser.class, Logger.class);
      assertTrue(Proxy.isProxyClass(logger.getClass()));
      replayAll();

      assertEquals("sl4j user", tested.getMessage());
      verifyAll();
   }
}

