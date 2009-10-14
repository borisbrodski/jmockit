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
public final class TextFileTestUsingExpectations
{
   @SuppressWarnings({"JUnitTestMethodWithNoAssertions"})
   @Test
   public void createTextFile() throws Exception
   {
      new Expectations()
      {
         TextReader reader;

         // Records TextFile#TextFile(String, int):
         {
            new TextReader("file");
         }
      };

      new TextFile("file", 0);
   }

   @Test
   public void parseTextFileUsingConcreteClass() throws Exception
   {
      new Expectations()
      {
         final TextReader reader = new TextReader("file");

         // Records TextFile#parse():
         {
            reader.skip(200); returns(200L);
            reader.readLine(); returns("line1");
            reader.readLine(); returns("another,line");
            reader.readLine(); returns(null);
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
   public void parseTextFileUsingInterface(final ITextReader reader) throws Exception
   {
      new Expectations()
      {
         // Records TextFile#parse():
         {
            reader.skip(200); returns(200L);
            reader.readLine(); returns("line1");
            reader.readLine(); returns("another,line");
            reader.readLine(); returns(null);
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
            reader.readLine(); returns("line1");
            reader.readLine(); returns("another,line");
            reader.readLine(); returns(null);
            reader.close();
         }
      };

      TextFile textFile = new TextFile("file");
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);
   }
}
