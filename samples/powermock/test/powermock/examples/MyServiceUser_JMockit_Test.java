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
package powermock.examples;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;
import powermock.examples.dependencymanagement.*;
import powermock.examples.domain.*;
import powermock.examples.service.*;

@RunWith(JMockit.class)
public class MyServiceUser_JMockit_Test
{
   private MyServiceUser tested;

   @Mocked private DependencyManager dependencyManagerMock;
   @Mocked private MyService myServiceMock;

   @Before
   public void setUp()
   {
      tested = new MyServiceUser();
   }

   @After // this method does nothing useful...
   public void tearDown()
   {
      tested = null;
      dependencyManagerMock = null;
   }

   @Test
   public void testGetNumberOfPersons() throws Exception
   {
      new Expectations()
      {
         {
            DependencyManager.getInstance(); returns(dependencyManagerMock);

            dependencyManagerMock.getMyService(); returns(myServiceMock);

            Set<Person> persons = new HashSet<Person>();
            persons.add(new Person("Rogério", "Liesenfeld", "MockStreet"));
            persons.add(new Person("John", "Doe", "MockStreet2"));

            myServiceMock.getAllPersons(); returns(persons);
         }
      };

      int numberOfPersons = tested.getNumberOfPersons();

      assertEquals(2, numberOfPersons);
   }
}
