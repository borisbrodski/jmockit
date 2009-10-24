/*
 * JMockit Expectations
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
import mockit.*;
import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class TextFileUsingExpectationsTest
{
   @Test
   public void createTextFile() throws Exception
   {
      new Expectations()
      {
         DefaultTextReader reader;

         // Records TextFile#TextFile(String, int):
         {
            new DefaultTextReader("file");
         }
      };

      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByStubbingOutTheTextReaderClass() throws Exception
   {
      Mockit.stubOutClass(DefaultTextReader.class.getName());

      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByCapturingTheTextReaderClassThroughItsBaseType() throws Exception
   {
      new Expectations()
      {
         @Mocked(capture = 1)
         TextReader reader;
      };

      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByMockingTheTextReaderClassThroughItsName() throws Exception
   {
      new Expectations()
      {
         @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader")
         final Object reader = null;
      };

      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByRecordingTheConstructorInvocationThroughReflection() throws Exception
   {
      new Expectations()
      {
         @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader")
         final Object reader =
            newInstance("integrationTests.textFile.TextFile$DefaultTextReader", "file");

         {
            invoke(reader, "close");
         }
      };

      new TextFile("file", 0).closeReader();
   }

   @Test
   public void createTextFileByRecordingNonStrictInvocationsThroughReflection() throws Exception
   {
      new Expectations()
      {
         @NonStrict @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader")
         Object reader;

         {
            invoke(reader, "close"); repeats(1);
         }
      };

      new TextFile("file", 0).closeReader();
   }

   @Test
   public void parseTextFileUsingConcreteClass() throws Exception
   {
      new Expectations()
      {
         final DefaultTextReader reader = new DefaultTextReader("file");

         // Records TextFile#parse():
         {
            reader.skip(200); returns(200L);
            reader.readLine(); returns("line1", "another,line", null);
            reader.close();
         }
      };

      TextFile textFile = new TextFile("file", 200);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);
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
   public void parseTextFileUsingInterface(final TextReader reader) throws Exception
   {
      new Expectations()
      {
         // Records TextFile#parse():
         {
            reader.skip(200); returns(200L);
            reader.readLine(); returns("line1", "another,line", null);
            reader.close();
         }
      };

      // Replays recorded invocations while verifying expectations:
      TextFile textFile = new TextFile(reader, 200);
      List<String[]> result = textFile.parse();

      // Verifies result:
      assertResultFromTextFileParsing(result);
   }

   @Test
   public void parseTextFileUsingBufferedReader() throws Exception
   {
      new Expectations()
      {
         @Mocked("(InputStream)") private final InputStreamReader inputStreamReader = null;
         @Mocked("(String)") private final FileReader fileReader = null;
         @Mocked({"(Reader)", "skip", "readLine", "close"}) private BufferedReader reader;

         // Records TextFile#TextFile(String):
         {
            //noinspection IOResourceOpenedButNotSafelyClosed
            new BufferedReader(new FileReader("file"));
         }

         // Records TextFile#parse():
         {
            reader.skip(0); returns(0L);
            reader.readLine(); returns("line1"); returns("another,line"); returns(null);
            reader.close();
         }
      };

      TextFile textFile = new TextFile("file");
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);
   }
}
