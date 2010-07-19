/*
 * JMockit
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
package mockit;

import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class ClassLoadingAndJREMocksTest
{
   static class Foo
   {
      boolean checkFile(String filePath)
      {
         File f = new File(filePath);
         return f.exists();
      }
   }

   @Test
   public void recordExpectationForFileUsingLocalMockField()
   {
      new Expectations()
      {
         File file;

         {
            new File("filePath").exists(); result = true;
         }
      };

      Foo foo = new Foo();
      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void recordExpectationForFileUsingMockParameter(@Mocked File file)
   {
      new Expectations()
      {
         {
            new File("filePath").exists(); result = true;
         }
      };

      Foo foo = new Foo();
      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void mockUpFile()
   {
      // TODO: this test fails when run alone; mock classes should also support conditional mocking
      // for JRE classes
      new MockUp<File>()
      {
         @Mock
         boolean exists() { return true; }
      };

      Foo foo = new Foo();
      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void mockFileOutputStreamInstantiation() throws Exception
   {
      new Expectations()
      {
         @Mocked("helperMethod") TestedUnitUsingIO tested;
         FileOutputStream mockOS;

         {
            invoke(tested, "helperMethod", withAny(FileOutputStream.class));
         }
      };

      new TestedUnitUsingIO().doSomething();
   }

   static class TestedUnitUsingIO
   {
      void doSomething() throws FileNotFoundException
      {
         helperMethod(new FileOutputStream("test"));
      }

      private void helperMethod(OutputStream output)
      {
         // Won't happen:
         throw new IllegalStateException(output.toString());
      }
   }

   @Test
   public void mockEntireAbstractListClass()
   {
      new NonStrictExpectations()
      {
         AbstractList<?> c;
      };
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToMockNonMockableJREClass()
   {
      new NonStrictExpectations()
      {
         Integer mock;

         {
            //noinspection UnnecessaryUnboxing
            mock.intValue(); result = 123;
         }
      };
   }

   @Test
   public void mockHashtable()
   {
      Properties props = new Properties();

      new Expectations()
      {
         Properties mock;

         {
            mock.remove(anyString); result = 123;
            mock.getProperty("test"); result = "mock";
         }
      };

      assertEquals(123, props.remove(""));
      assertEquals("mock", props.getProperty("test"));
   }

   @Test
   public void mockURLAndHttpURLConnection() throws Exception
   {
      // Several different ways to write this test don't work: 1) mocking the URL class without
      // dynamic mocking; 2) using a mock URL parameter with dynamic mocking; 3) mocking
      // HttpURLConnection with a mock parameter or mock field of the test class.
      final URL url = new URL("http://nowhere");

      new NonStrictExpectations(url)
      {
         HttpURLConnection mockConnection;

         {
            url.openConnection(); result = mockConnection;
            mockConnection.getOutputStream(); result = new ByteArrayOutputStream();
         }
      };

      // Code under test:
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("PUT");
      OutputStream out = conn.getOutputStream();

      assertNotNull(out);

      new Verifications()
      {
         HttpURLConnection mockConnection;

         {
            mockConnection.setDoOutput(true);
            mockConnection.setRequestMethod("PUT");
         }
      };
   }

   @Ignore @Test // causes infinite loop
   public void mockFileInputStream() throws Exception
   {
      new Expectations(FileInputStream.class)
      {
         {
            new FileInputStream("").close(); result = new IOException();
         }
      };

      new FileInputStream("").close();
   }
}
