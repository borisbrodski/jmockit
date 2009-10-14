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
package powermock.examples.tutorial.domainmocking.impl;

import powermock.examples.tutorial.domainmocking.*;
import powermock.examples.tutorial.domainmocking.domain.*;
import powermock.examples.tutorial.domainmocking.impl.*;
import mockit.*;
import mockit.integration.junit4.*;
import org.junit.*;

public final class SampleServiceImpl_JMockit_Test extends JMockitTest
{
   @Mocked private PersonService personService;
   @Mocked private EventService eventService;

   @Test
   public void testCreatePerson()
   {
      final String firstName = "firstName";
      final String lastName = "lastName";

      new Expectations()
      {
         BusinessMessages businessMessages;
         Person person;

         {
            businessMessages = new BusinessMessages();
            Person person = new Person(firstName, lastName);
            personService.create(person, businessMessages);
            businessMessages.hasErrors(); returns(false);
         }
      };

      SampleService sampleService = new SampleServiceImpl(personService, eventService);
      assertTrue(sampleService.createPerson(firstName, lastName));
   }

   @Test
   public void testCreatePersonWithBusinessError()
   {
      final String firstName = "firstName";
      final String lastName = "lastName";

      new Expectations()
      {
         BusinessMessages businessMessages;
         Person person;

         {
            businessMessages = new BusinessMessages();
            Person person = new Person(firstName, lastName);
            personService.create(person, businessMessages);
            businessMessages.hasErrors(); returns(true);
            eventService.sendErrorEvent(person, businessMessages);
         }
      };

      SampleService sampleService = new SampleServiceImpl(personService, eventService);
      assertFalse(sampleService.createPerson(firstName, lastName));
   }

   // Notice that this test does not in fact need any mocking, but just for demonstration...
   @Test(expected = SampleServiceException.class)
   public void testCreatePersonWithIllegalName()
   {
      final String firstName = "firstName";
      final String lastName = "lastName";

      new Expectations()
      {
         Person person;

         {
            new Person(firstName, lastName); throwsException(new IllegalArgumentException("test"));
         }
      };

      SampleService sampleService = new SampleServiceImpl(personService, eventService);
      sampleService.createPerson(firstName, lastName);
   }
}
