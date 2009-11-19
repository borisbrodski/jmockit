/*
 * JMockit Annotations
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

import java.util.*;

import org.junit.*;

import mockit.*;

import integrationTests.textFile.TextFile.*;
import static mockit.Mockit.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

public final class TextFileUsingAnnotatedMockClassesTest
{
   @Test
   public void createTextFileUsingAnnotatedMockClass() throws Exception
   {
      setUpMocks(MockTextReader.class);

      new TextFile("file", 0);
   }

   @MockClass(realClass = DefaultTextReader.class)
   static class MockTextReader
   {
      @Mock(invocations = 1)
      MockTextReader(String fileName)
      {
         assertThat(fileName, equalTo("file"));
      }
   }

   @Test
   public void createTextFileByMockingTheTextReaderThroughItsClassName() throws Exception
   {
      setUpMock("integrationTests.textFile.TextFile$DefaultTextReader", MockTextReader.class);

      new TextFile("file", 0);
   }

   @Test
   public void parseTextFileUsingConcreteClass() throws Exception
   {
      setUpMocks(MockTextReader.class, new MockTextReaderForParse());

      TextFile textFile = new TextFile("file", 200);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);
   }

   @MockClass(realClass = DefaultTextReader.class)
   public static class MockTextReaderForParse
   {
      private static final String[] LINES = { "line1", "another,line", null};
      int invocation;

      @Mock(invocations = 1)
      public long skip(long n)
      {
         assertEquals(200, n);
         return n;
      }

      @Mock(invocations = 3)
      public String readLine() { return LINES[invocation++]; }

      @Mock(invocations = 1)
      public void close() {}
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
   public void parseTextFileUsingInterface() throws Exception
   {
      TextReader textReader = newEmptyProxy(TextReader.class);
      setUpMock(textReader.getClass(), new MockTextReaderForParse());

      TextFile textFile = new TextFile(textReader, 200);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);
   }
}
