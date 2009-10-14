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
package powermock.examples.bypassencapsulation;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

import static mockit.Deencapsulation.*;

public class ServiceHolder_JMockit_Test
{
   @Test
   public void testAddService()
   {
      ServiceHolder tested = new ServiceHolder();
      Object service = new Object();

      tested.addService(service);

      Set<String> services = getField(tested, "services");

      assertEquals("Size of the \"services\" Set should be 1", 1, services.size());
      assertSame(
         "The services Set should didn't contain the expect service",
         service, services.iterator().next());
   }

   @Test
   public void testRemoveService()
   {
      ServiceHolder tested = new ServiceHolder();
      Object service = new Object();

      // Get the hash set.
      Set<Object> servicesSet = getField(tested, Set.class);
      servicesSet.add(service);

      tested.removeService(service);

      assertTrue("Set should be empty after removeal.", servicesSet.isEmpty());
   }
}
