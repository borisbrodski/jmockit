package powermock.examples.logging;

import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

import org.powermock.api.easymock.mockpolicies.JclMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import powermock.examples.logging.*;

/**
 * Unit tests that assert that the {@link JclMockPolicy} works.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(JclMockPolicy.class)
public class JclUserTest
{
   @Test
   public void assertJclMockPolicyWorks()
   {
      JclUser tested = new JclUser();

      Log logger = Whitebox.getInternalState(JclUser.class, Log.class);
      assertTrue(Proxy.isProxyClass(logger.getClass()));
      replayAll();

      assertEquals("jcl user", tested.getMessage());
      verifyAll();
   }
}
