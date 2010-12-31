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

@SuppressWarnings({"UseOfObsoleteCollectionType", "CollectionDeclaredAsConcreteClass"})
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
      Foo foo = new Foo();

      new MockUp<File>()
      {
         @Mock
         boolean exists() { return true; }
      };

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

   @Test
   public void attemptToMockNonMockableJREClass()
   {
      new NonStrictExpectations()
      {
         Integer mock;

         {
            assertNull(mock);
         }
      };
   }

   static class ClassWithVector
   {
      final Collection<?> theVector = new Vector<Object>();

      public int getVectorSize() { return theVector.size(); }
   }

   @Test
   public void useMockedVectorDuringClassLoading()
   {
      new NonStrictExpectations()
      {
         Vector<?> mockedVector;

         {
            mockedVector.size(); result = 2;
         }
      };

      assertEquals(2, new ClassWithVector().getVectorSize());
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

   @Mocked URLConnection mockConnection;

   @Test
   public void mockURLAndURLConnectionUsingMockParameterAndMockField(final URL url) throws Exception
   {
      new Expectations()
      {
         {
            url.openConnection(); result = mockConnection;
         }
      };

      URLConnection conn = url.openConnection();
      assertSame(mockConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnectionUsingMockParameters(
      final URL mockUrl, final HttpURLConnection mockHttpConnection) throws Exception
   {
      new NonStrictExpectations()
      {
         {
            mockUrl.openConnection(); result = mockHttpConnection;
         }
      };

      HttpURLConnection conn = (HttpURLConnection) mockUrl.openConnection();
      assertSame(mockHttpConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnectionWithDynamicMockAndLocalMockField() throws Exception
   {
      final URL url = new URL("http://nowhere");

      new NonStrictExpectations(url)
      {
         HttpURLConnection mockHttpConnection;

         {
            url.openConnection(); result = mockHttpConnection;
            mockHttpConnection.getOutputStream(); result = new ByteArrayOutputStream();
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
         HttpURLConnection mockHttpConnection;

         {
            mockHttpConnection.setDoOutput(true);
            mockHttpConnection.setRequestMethod("PUT");
         }
      };
   }

   @Test
   public void mockFileInputStream() throws Exception
   {
      new Expectations(FileInputStream.class)
      {
         {
            new FileInputStream("").close(); result = new IOException();
         }
      };

      try {
         new FileInputStream("").close();
         fail();
      }
      catch (IOException ignore) {
         // OK
      }
   }
}
