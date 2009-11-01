/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package powermock.examples.tutorial.staticmocking.impl;

import java.util.*;

import powermock.examples.tutorial.staticmocking.osgi.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;
import static org.powermock.reflect.Whitebox.*;
import static org.easymock.EasyMock.expect;

/**
 * Unit test for the {@link ServiceRegistrator} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IdGenerator.class)
public class ServiceRegistratorTest
{
   private ServiceRegistrator tested;
   private BundleContext bundleContextMock;
   private ServiceRegistration serviceRegistrationMock;

   @Before
   public void setUp()
   {
      tested = new ServiceRegistrator();
      bundleContextMock = createMock(BundleContext.class);
      serviceRegistrationMock = createMock(ServiceRegistration.class);
      mockStatic(IdGenerator.class);
   }

   @Test
   public void testRegisterService()
   {
      // Data for the test:
      String name = "a name";
      Object serviceImpl = new Object();
      long expectedId = 42;

      // Inject one of the mocks into the tested object:
      setInternalState(tested, bundleContextMock);

      // Expectations:
      expect(bundleContextMock.registerService(name, serviceImpl, null))
         .andReturn(serviceRegistrationMock);

      expect(IdGenerator.generateNewId()).andReturn(expectedId);

      replayAll();

      // Code under test is exercised (replay phase):
      long actualId = tested.registerService(name, serviceImpl);

      verifyAll();

      // State-based verifications:
      Map<Long, ServiceRegistration> serviceRegistrations = getInternalState(tested, Map.class);

      assertEquals(1, serviceRegistrations.size());
      assertSame(serviceRegistrationMock, serviceRegistrations.get(actualId));
   }

   @Test
   public void testUnregisterService() throws Exception
   {
      Map<Long, ServiceRegistration> serviceRegistrations =
         new HashMap<Long, ServiceRegistration>();
      final long id = 1L;
      serviceRegistrations.put(id, serviceRegistrationMock);

      setInternalState(tested, serviceRegistrations);

      serviceRegistrationMock.unregister();
      expectLastCall().times(1);

      replayAll();

      tested.unregisterService(id);

      verifyAll();

      assertTrue(serviceRegistrations.isEmpty());
   }

   @Test
   public void testUnregisterService_idDoesntExist()
   {
      replayAll();

      try {
         tested.unregisterService(1L);
         fail("Should throw IllegalStateException");
      }
      catch (IllegalStateException e) {
         verifyAll();
      }
   }
}
