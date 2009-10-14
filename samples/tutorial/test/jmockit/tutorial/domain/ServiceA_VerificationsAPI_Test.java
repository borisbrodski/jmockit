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
package jmockit.tutorial.domain;

import java.util.*;

import org.junit.*;

import mockit.*;
import mockit.integration.junit4.*;

import jmockit.tutorial.infrastructure.*;

public final class ServiceA_VerificationsAPI_Test extends JMockitTest
{
   @Mocked private final Database onlyStatics = null;
   @Mocked private final ServiceB unused = null;

   @Test
   public void doBusinessOperationXyzSavesData() throws Exception
   {
      final EntityX data = new EntityX(5, "abc", "5453-1");

      // No expectations in this case.
      
      new ServiceA().doBusinessOperationXyz(data);

      new Verifications()
      {
         { Database.save(data); }
      };
   }

   @Test
   public void doBusinessOperationXyzFindsItemsAndComputesTotal() throws Exception
   {
      EntityX data = new EntityX(5, "abc", "5453-1");
      final List<?> items = Arrays.asList(1, 2, 3);

      // Invocations that produce a result are recorded, but only those we care about.
      new NonStrictExpectations()
      {
         {
            Database.find(withSubstring("select"), withAny("")); returns(items);
         }
      };

      new ServiceA().doBusinessOperationXyz(data);

      new Verifications()
      {
         {
            new ServiceB().computeTotal(items);
         }
      };
   }

   @Test(expected = InvalidItemStatus.class)
   public void doBusinessOperationXyzWithInvalidItemStatus() throws Exception
   {
      new NonStrictExpectations()
      {
         {
            new ServiceB().computeTotal((List<?>) withNotNull());
            throwsException(new InvalidItemStatus());
         }
      };

      EntityX data = new EntityX(5, "abc", "5453-1");
      new ServiceA().doBusinessOperationXyz(data);

      // Nothing left to verify at this point.
   }
}
