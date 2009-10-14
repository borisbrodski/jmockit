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
package powermock.examples;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;
import powermock.examples.dependencymanagement.*;
import powermock.examples.domain.*;
import powermock.examples.service.*;
import powermock.examples.service.impl.*;

/**
 * This is an example unit test using JUnit 4.5+ for the {@link MyServiceImpl#getAllPersons()}
 * method. The task for PowerMock is to mock the call to {@link DependencyManager#getInstance()}
 * which is not possible without byte-code manipulation.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DependencyManager.class)
public class MyServiceUserTest
{
   private MyServiceUser tested;

   private DependencyManager dependencyManagerMock;
   private MyService myServiceMock;

   @Before
   public void setUp()
   {
      tested = new MyServiceUser();
      dependencyManagerMock = createMock(DependencyManager.class);
      myServiceMock = createMock(MyService.class);
   }

   @After
   public void tearDown()
   {
      tested = null;
      dependencyManagerMock = null;
   }

   /**
    * Unit test for the {@link MyServiceImpl#getAllPersons()} method. This tests demonstrate how to
    * mock the static call to {@link DependencyManager#getInstance()} and returning a mock of the
    * <code>DependencyManager</code> instead of the real instance.
    */
   @Test
   public void testGetNumberOfPersons() throws Exception
   {
      // This is how to tell PowerMock to prepare the DependencyManager class for static mocking.
      mockStatic(DependencyManager.class);

      // Expectations are performed the same for static methods as for instance methods.
      expect(DependencyManager.getInstance()).andReturn(dependencyManagerMock);

      expect(dependencyManagerMock.getMyService()).andReturn(myServiceMock);

      Set<Person> persons = new HashSet<Person>();
      persons.add(new Person("Johan", "Haleby", "MockStreet"));
      persons.add(new Person("Jan", "Kronquist", "MockStreet2"));

      expect(myServiceMock.getAllPersons()).andReturn(persons);

      replayAll();

      int numberOfPersons = tested.getNumberOfPersons();

      verifyAll();

      assertEquals(2, numberOfPersons);
   }
}
