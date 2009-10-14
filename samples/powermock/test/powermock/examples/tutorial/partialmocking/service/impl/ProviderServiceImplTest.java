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
package powermock.examples.tutorial.partialmocking.service.impl;

import java.util.*;

import powermock.examples.tutorial.partialmocking.dao.*;
import powermock.examples.tutorial.partialmocking.dao.domain.impl.*;
import powermock.examples.tutorial.partialmocking.domain.*;
import static org.easymock.EasyMock.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;
import static org.powermock.reflect.Whitebox.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProviderServiceImpl.class)
public class ProviderServiceImplTest {

	private ProviderServiceImpl tested;
	private ProviderDao providerDaoMock;

	@Before
	public void setUp() {
		tested = new ProviderServiceImpl();
		providerDaoMock = createMock(ProviderDao.class);

		setInternalState(tested, providerDaoMock);
	}

	@After
	public void tearDown() {
		tested = null;
		providerDaoMock = null;
	}

	@Test
	public void testGetAllServiceProviders() throws Exception {
		final String methodNameToMock = "getAllServiceProducers";
		final Set<ServiceProducer> expectedServiceProducers = new HashSet<ServiceProducer>();
		expectedServiceProducers.add(new ServiceProducer(1, "mock name"));

		tested = createPartialMock(ProviderServiceImpl.class, methodNameToMock);

		expectPrivate(tested, methodNameToMock).andReturn(expectedServiceProducers);

		replayAll();

		Set<ServiceProducer> actualServiceProviders = tested.getAllServiceProviders();

		verifyAll();

		assertSame(expectedServiceProducers, actualServiceProviders);
	}

	@Test
	public void testGetAllServiceProviders_noServiceProvidersFound() throws Exception {
		final String methodNameToMock = "getAllServiceProducers";
		final Set<ServiceProducer> expectedServiceProducers = new HashSet<ServiceProducer>();

		tested = createPartialMock(ProviderServiceImpl.class, methodNameToMock);

		expectPrivate(tested, methodNameToMock).andReturn(null);

		replayAll();

		Set<ServiceProducer> actualServiceProviders = tested.getAllServiceProviders();

		verifyAll();

		assertNotSame(expectedServiceProducers, actualServiceProviders);
		assertEquals(expectedServiceProducers, actualServiceProviders);
	}

	@Test
	public void testServiceProvider_found() throws Exception {
		final String methodNameToMock = "getAllServiceProducers";
		final int expectedServiceProducerId = 1;
		final ServiceProducer expected = new ServiceProducer(expectedServiceProducerId, "mock name");

		final Set<ServiceProducer> serviceProducers = new HashSet<ServiceProducer>();
		serviceProducers.add(expected);

		tested = createPartialMock(ProviderServiceImpl.class, methodNameToMock);

		expectPrivate(tested, methodNameToMock).andReturn(serviceProducers);

		replayAll();

		ServiceProducer actual = tested.getServiceProvider(expectedServiceProducerId);

		verifyAll();

		assertSame(expected, actual);
	}

	@Test
	public void testServiceProvider_notFound() throws Exception {
		final String methodNameToMock = "getAllServiceProducers";
		final int expectedServiceProducerId = 1;

		tested = createPartialMock(ProviderServiceImpl.class, methodNameToMock);

		expectPrivate(tested, methodNameToMock).andReturn(new HashSet<ServiceProducer>());

		replayAll();

		assertNull(tested.getServiceProvider(expectedServiceProducerId));

		verifyAll();

	}

	@Test
	@SuppressWarnings("unchecked")
	public void getAllServiceProducers() throws Exception {
		final String expectedName = "mock name";
		final int expectedId = 1;

		final Set<ServiceArtifact> serviceArtifacts = new HashSet<ServiceArtifact>();
		serviceArtifacts.add(new ServiceArtifact(expectedId, expectedName));

		expect(providerDaoMock.getAllServiceProducers()).andReturn(serviceArtifacts);

		replayAll();

		Set<ServiceProducer> serviceProducers = (Set<ServiceProducer>) invokeMethod(tested, "getAllServiceProducers");

		verifyAll();

		assertEquals(1, serviceProducers.size());
		assertTrue(serviceProducers.contains(new ServiceProducer(expectedId, expectedName)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getAllServiceProducers_empty() throws Exception {
		expect(providerDaoMock.getAllServiceProducers()).andReturn(new HashSet<ServiceArtifact>());

		replayAll();

		Set<ServiceProducer> actual = (Set<ServiceProducer>) invokeMethod(tested, "getAllServiceProducers");

		verifyAll();

		assertTrue(actual.isEmpty());
	}
}
