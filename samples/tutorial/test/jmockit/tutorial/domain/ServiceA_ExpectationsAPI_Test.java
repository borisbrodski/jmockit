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

import java.math.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

import jmockit.tutorial.infrastructure.*;

public final class ServiceA_ExpectationsAPI_Test
{
   @Mocked final Database unused = null;
   @Mocked ServiceB serviceB;

   @Test
   public void doBusinessOperationXyz() throws Exception
   {
      final EntityX data = new EntityX(5, "abc", "5453-1");
      final BigDecimal total = new BigDecimal("125.40");

      new Expectations()
      {
         {
            List<?> items = new ArrayList<Object>();
            Database.find(withSubstring("select"), null); returns(items);

            new ServiceB().computeTotal(items); returns(total);
            Database.save(data);
         }
      };

      new ServiceA().doBusinessOperationXyz(data);

      assertEquals(total, data.getTotal());
   }

   @Test(expected = InvalidItemStatus.class)
   public void doBusinessOperationXyzWithInvalidItemStatus() throws Exception
   {
      new Expectations()
      {
         {
            Database.find(anyString, anyString);

            new ServiceB().computeTotal((List<?>) withNotNull());
            throwsException(new InvalidItemStatus());
         }
      };

      EntityX data = new EntityX(5, "abc", "5453-1");
      new ServiceA().doBusinessOperationXyz(data);
   }
}
