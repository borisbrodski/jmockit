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

import java.io.*;

import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;
import org.powermock.reflect.*;

@RunWith(JMockit.class)
public class Logger_JMockit_Test
{
   @Test(expected = IllegalStateException.class)
   public void testException() throws Exception
   {
      new NonStrictExpectations()
      {
         final FileWriter fileWriter;

         {
            fileWriter = new FileWriter("target/logger.log"); throwsException(new IOException());
         }
      };

      new Logger();
   }

   @Test
   public void testLogger() throws Exception
   {
      new NonStrictExpectations()
      {
         FileWriter fileWriter;   // can also be final with value "new FileWriter(withAny(""))"
         PrintWriter printWriter; // can also be final with value "new PrintWriter(fileWriter)"

         {
            printWriter.println("qwe");
         }
      };

      Logger logger = new Logger();
      logger.log("qwe");
   }

   @Test
   public void testLogger2(final Logger logger)
   {
      new Expectations(logger)
      {
         PrintWriter printWriter;

         {
            Whitebox.setInternalState(logger, printWriter);

            printWriter.println("qwe");
         }
      };

      logger.log("qwe");
   }
}
