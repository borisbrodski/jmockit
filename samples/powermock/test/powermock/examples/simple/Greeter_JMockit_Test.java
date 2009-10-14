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
package powermock.examples.simple;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import static mockit.Deencapsulation.*;
import mockit.*;
import mockit.integration.junit4.*;

@RunWith(JMockit.class)
@UsingMocksAndStubs(SimpleConfig.class)
public class Greeter_JMockit_Test
{
   @Test
   public void testGetMessage() throws Exception
   {
      new Expectations()
      {
         final SimpleConfig unused = null;

         {
            SimpleConfig.getGreeting(); returns("Hi");
            SimpleConfig.getTarget(); returns("All");
         }
      };

      assertEquals("Hi All", invoke(Greeter.class, "getMessage"));
      assertFalse(SimpleConfig.wasInitialized);
   }

   @Test
   public void testRun() throws Exception
   {
      new Expectations()
      {
         Logger logger;

         {
            new Logger();
            logger.log("Hello"); repeats(10);
         }
      };

      invoke(new Greeter(), "run", 10, "Hello");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testRunWhenLoggerThrowsUnexpectedRuntimeException() throws Exception
   {
      new Expectations()
      {
         Logger mock;

         {
            new Logger(); throwsException(new IllegalArgumentException("Unexpected exception"));
         }
      };

      invoke(new Greeter(), "run", 10, "Hello");
   }
}
