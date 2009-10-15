/*
 * JMockit Verifications
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
package integrationTests.textFile;

import java.io.*;
import java.util.*;

import integrationTests.textFile.TextFile.*;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;

@RunWith(JMockit.class)
public final class TextFileUsingVerificationsTest
{
   @Test
   public void createTextFile(TextReader reader) throws Exception
   {
      assertNotNull(reader);

      new TextFile("file", 0);

      new Verifications()
      {
         {
            new TextReader("file");
         }
      };
   }

   @Test
   public void createTextFileByCapturingTheTextReaderClassThroughItsBaseType(
      @Capturing ITextReader reader) throws Exception
   {
      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByMockingTheTextReaderClassThroughItsName(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$TextReader") Object reader)
      throws Exception
   {
      new TextFile("file", 0);
   }

   @Test
   public void createTextFileWhileVerifyingTheCreatedTextReaderIsClosed(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$TextReader")
      final ITextReader reader) throws Exception
   {
      new TextFile("file", 0).closeReader();

      new Verifications()
      {
         {
            reader.close();
         }
      };
   }

   @Test
   public void createTextFileVerifyingInvocationsThroughReflection(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$TextReader") final Object reader)
      throws Exception
   {
      new TextFile("file", 0).closeReader();

      new FullVerificationsInOrder()
      {
         {
            newInstance("integrationTests.textFile.TextFile$TextReader", "file");
            invoke(reader, "close");
         }
      };
   }

   @Test
   public void parseTextFileUsingConcreteClass(final TextReader reader) throws Exception
   {
      new NonStrictExpectations()
      {
         {
            reader.readLine(); returns("line1", "another,line", null);
         }
      };

      TextFile textFile = new TextFile("file", 200);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new Verifications()
      {
         {
            reader.close();
         }
      };
   }

   private void assertResultFromTextFileParsing(List<String[]> result)
   {
      assertEquals(2, result.size());
      String[] line1 = result.get(0);
      assertEquals(1, line1.length);
      assertEquals("line1", line1[0]);
      String[] line2 = result.get(1);
      assertEquals(2, line2.length);
      assertEquals("another", line2[0]);
      assertEquals("line", line2[1]);
   }

   @Test
   public void parseTextFileUsingInterface(@NonStrict final ITextReader reader) throws Exception
   {
      new Expectations()
      {
         {
            reader.readLine(); returns("line1", "another,line", null);
         }
      };

      TextFile textFile = new TextFile(reader, 100);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new VerificationsInOrder()
      {
         {
            reader.skip(100);
            reader.close();
         }
      };
   }

   @Test
   public void parseTextFileUsingBufferedReader(final BufferedReader reader) throws Exception
   {
      new NonStrictExpectations()
      {
         @Mocked("(InputStream)") final InputStreamReader inputStreamReader = null;
         @Mocked("(String)") final FileReader fileReader = null;

         {
            reader.readLine(); returns("line1", "another,line", null);
         }
      };

      TextFile textFile = new TextFile("file");
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new Verifications()
      {
         {
            reader.close();
         }
      };
   }
}