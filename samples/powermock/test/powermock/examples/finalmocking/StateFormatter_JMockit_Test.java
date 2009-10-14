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
package powermock.examples.finalmocking;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public class StateFormatter_JMockit_Test
{
   @Test
   public void testGetFormattedState_actualStateExists(final StateHolder stateHolderMock)
   {
      final String expectedState = "state";
      StateFormatter tested = new StateFormatter(stateHolderMock);

      new Expectations()
      {
         {
            stateHolderMock.getState(); returns(expectedState);
         }
      };

      String actualState = tested.getFormattedState();

      assertEquals(expectedState, actualState);
   }

   @Test
   public void testGetFormattedState_noStateExists(final StateHolder stateHolderMock)
   {
      StateFormatter tested = new StateFormatter(stateHolderMock);

      new Expectations()
      {
         {
            stateHolderMock.getState(); returns(null);
         }
      };

      String actualState = tested.getFormattedState();

      assertEquals("State information is missing", actualState);
   }
}
