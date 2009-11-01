/*
 * JMockit Samples
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package powermock.examples.tutorial.staticmocking.impl;

import powermock.examples.tutorial.staticmocking.osgi.*;

import java.util.*;

import mockit.*;
import static mockit.Deencapsulation.getField;
import mockit.integration.junit4.*;

import org.junit.*;

/**
 * Unit tests using the JMockit API for the
 * {@link powermock.examples.tutorial.staticmocking.impl.ServiceRegistrator} class.
 */
public final class ServiceRegistrator_JMockit_Test extends JMockitTest
{
   private ServiceRegistrator tested;
   @Mocked private ServiceRegistration serviceRegistrationMock;
   @Mocked private final IdGenerator unused = null;

   @Before
   public void setUp()
   {
      tested = new ServiceRegistrator();
   }

   @Test
   public void testRegisterService(final BundleContext bundleContextMock)
   {
      // Data for the test:
      final String name = "a name";
      final Object serviceImpl = new Object();
      final long expectedId = 42;

      new Expectations()
      {
         {
            // Inject one of the mocks into the tested object:
            setField(tested, bundleContextMock);

            bundleContextMock.registerService(name, serviceImpl, null);
            returns(serviceRegistrationMock);

            IdGenerator.generateNewId(); returns(expectedId);
         }
      };

      // Code under test is exercised (replay phase):
      long actualId = tested.registerService(name, serviceImpl);

      // No need to tell JMockit to verify missing expectations, since it's done automatically.

      // State-based verifications (simplified):
      Map<Long, ServiceRegistration> serviceRegistrations = getField(tested, Map.class);

      assertEquals(1, serviceRegistrations.size());
      assertSame(serviceRegistrationMock, serviceRegistrations.get(actualId));
   }

   @Test
   public void testUnregisterService()
   {
      final Map<Long, ServiceRegistration> serviceRegistrations =
         new HashMap<Long, ServiceRegistration>();
      long id = 1L;
      serviceRegistrations.put(id, serviceRegistrationMock);

      new Expectations()
      {
         {
            setField(tested, serviceRegistrations);

            serviceRegistrationMock.unregister();
         }
      };

      tested.unregisterService(id);

      assertTrue(serviceRegistrations.isEmpty());
   }

   @Test(expected = IllegalStateException.class)
   public void testUnregisterServiceWithIdWhichDoesntExist()
   {
      // No invocation on any mock is expected.
      new Expectations() {};

      tested.unregisterService(1L);
   }
}
