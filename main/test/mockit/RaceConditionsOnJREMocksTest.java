/*
 * JMockit
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
package mockit;

import java.io.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public final class RaceConditionsOnJREMocksTest
{
   static class StuffReader
   {
      private String getStuffType() { return "stuff"; }

      String readStuff()
      {
         StringWriter out = new StringWriter();

         try {
            String stuffType = getStuffType();
            out.append(stuffType);
         }
         catch (Exception ignore) {
            out.append("Error:can't determine stuff type");
            return out.toString();
         }

         Properties props = new Properties();

         try {
            props.load(new FileInputStream("myfile.properties"));
            out.append(props.getProperty("one"));
            out.append(props.getProperty("two"));
            out.append(props.getProperty("three"));
         }
         catch (FileNotFoundException ignore) {
            out.append(" FileNotFoundException");
         }
         catch (IOException ignore) {
            out.append(" IOException");
         }

         return out.toString();
      }
   }

   static final StuffReader stuffHandler = new StuffReader();

   @Test
   public void throwsExceptionFromGetStuffType()
   {
      new Expectations(stuffHandler)
      {
         {
            invoke(stuffHandler, "getStuffType"); throwsException(new Exception());
         }
      };

      String result = stuffHandler.readStuff();

      assertEquals("Error:can't determine stuff type", result);
   }

   @Test
   public void throwsFileNotFoundExceptionWhenOpeningInputFile() throws Exception
   {
      new Expectations(stuffHandler)
      {
         // TODO: when everything is mocked, an obscure exception occurs
         @Mocked("(String)") FileInputStream mockFIS;

         {
            invoke(stuffHandler, "getStuffType"); returns("*mocked*");
            new FileInputStream(anyString); throwsException(new FileNotFoundException());
         }
      };

      String result = stuffHandler.readStuff();

      assertEquals("*mocked* FileNotFoundException", result);
   }

   @Test
   public void throwsIOExceptionWhileReadingProperties()
   {
      new Expectations(stuffHandler)
      {
         // TODO: mocking everything in Properties also leads to failure
         @Mocked("load") Properties props;
         @Mocked("(String)") FileInputStream mockFIS;

         {
            invoke(stuffHandler, "getStuffType"); returns("*mocked*");

            invoke(props, "load", withAny(FileInputStream.class));
            throwsException(new IOException());
         }
      };

      String result = stuffHandler.readStuff();

      assertEquals("*mocked* IOException", result);
   }

   @Test
   public void getCompleteStuff()
   {
      new Expectations(stuffHandler)
      {
         @NonStrict @Mocked({"load", "getProperty"}) Properties props;
         @Mocked("(String)") FileInputStream mockFIS;

         {
            invoke(stuffHandler, "getStuffType"); returns("*mocked*");
            invoke(props, "getProperty", withAny("")); returns(" *mocked*");
         }
      };

      String result = stuffHandler.readStuff();

      assertEquals("*mocked* *mocked* *mocked* *mocked*", result);
   }
}
