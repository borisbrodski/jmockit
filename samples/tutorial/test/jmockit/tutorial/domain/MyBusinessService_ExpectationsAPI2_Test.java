/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package jmockit.tutorial.domain;

import java.util.*;

import static java.util.Arrays.*;
import org.apache.commons.mail.*;
import org.junit.*;

import mockit.*;

import jmockit.tutorial.infrastructure.*;

public final class MyBusinessService_ExpectationsAPI2_Test
{
   @Tested MyBusinessService service;

   @Mocked(stubOutClassInitialization = true) final Database unused = null;
   @Mocked SimpleEmail email;

   final EntityX data = new EntityX(5, "abc", "someone@somewhere.com");

   @Test
   public void doBusinessOperationXyz() throws Exception
   {
      new Expectations() {
         @Input final List<EntityX> items = asList(new EntityX(1, "AX5", "someone@somewhere.com"));
      };

      service.doBusinessOperationXyz(data);

      new Verifications() {{
         Database.persist(data);
         email.send();
      }};
   }

   @Test(expected = EmailException.class)
   public void doBusinessOperationXyzWithInvalidEmailAddress() throws Exception
   {
      new Expectations() { @Input EmailException onInvalidEmail; };

      service.doBusinessOperationXyz(data);

      new Verifications() {{ email.send(); times = 0; }};
   }
}
