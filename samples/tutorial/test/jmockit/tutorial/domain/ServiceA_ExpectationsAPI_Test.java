/*
 * JMockit Samples
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package jmockit.tutorial.domain;

import java.util.*;

import org.apache.commons.mail.*;
import org.junit.*;

import mockit.*;

import jmockit.tutorial.infrastructure.*;

@UsingMocksAndStubs(Database.class) // TODO: stub out static blocks by default in @Mocked classes
public final class ServiceA_ExpectationsAPI_Test
{
   @Mocked final Database unused = null;
   @NonStrict SimpleEmail email; // calls to setters are irrelevant, so we make it non-strict

   @Test
   public void doBusinessOperationXyz() throws Exception
   {
      final EntityX data = new EntityX(5, "abc", "5453-1");
      final List<EntityX> items = new ArrayList<EntityX>();
      items.add(new EntityX(1, "AX5", "someone@somewhere.com"));

      new Expectations()
      {
         {
            // "Database" is mocked strictly, therefore the order of these invocations does matter:
            Database.find(withSubstring("select"), (Object[]) null); result = items;
            Database.persist(data);

            // Since "email" is a non-strict mock, this invocation can be replayed in any order:
            email.send(); times = 1; // a non-strict invocation requires a constraint if expected
         }
      };

      new MyBusinessService().doBusinessOperationXyz(data);
   }

   @Test(expected = EmailException.class)
   public void doBusinessOperationXyzWithInvalidEmailAddress() throws Exception
   {
      new NonStrictExpectations()
      {
         {
            email.addTo((String) withNotNull()); result = new EmailException();
         }
      };

      EntityX data = new EntityX(5, "abc", "5453-1");
      new MyBusinessService().doBusinessOperationXyz(data);
   }
}
