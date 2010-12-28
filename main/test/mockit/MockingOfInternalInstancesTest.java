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

import org.junit.*;
import static org.junit.Assert.*;

public final class MockingOfInternalInstancesTest
{
   static final String FILE_NAME = "testFile.out";

   final OutputStream testOutput = new ByteArrayOutputStream();
   final FileIO fileIO = new FileIO();

   @Before
   public void redirectStandardOutput()
   {
      System.setOut(new PrintStream(testOutput));
   }

   @After
   public void restoreStandardOutput()
   {
      System.setOut(null);
   }

   private void assertExpectedFileIO()
   {
      File realFile = new File(FILE_NAME);
      boolean realFileCreated = realFile.exists();
      
      if (realFileCreated) {
         realFile.delete();
      }

      assertFalse("Real file created", realFileCreated);
      assertTrue("File not written", testOutput.toString().startsWith("File written"));
   }

   @SuppressWarnings({"UnusedParameters"})
   @Test
   public void stubOutFileCreationWithMockUps() throws Exception
   {
      new MockUp<FileWriter>() { @Mock void $init(String s) {} };
      new MockUp<OutputStreamWriter>() { @Mock void $init(OutputStream out, String s) {} };
      new MockUp<BufferedWriter>() { @Mock void close() {} };

      fileIO.writeToFile(FILE_NAME);
      assertExpectedFileIO();
   }

   @Test
   public void stubOutFileCreationWithStaticPartialMocking() throws Exception
   {
      new Expectations()
      {
         @Mocked({"(String)", "(OutputStream, String)"}) FileWriter fileWriter;
         @Mocked("close") BufferedWriter bufferedWriter;
      };

      fileIO.writeToFile(FILE_NAME);
      assertExpectedFileIO();
   }

   @Ignore @Test
   public void stubOutFileCreationWithMockingRestrictedToTestedClass(
      @Tested FileIO fileIO, @Mocked FileWriter fileWriter, @Mocked BufferedWriter bufferedWriter) throws Exception
   {
      fileIO.writeToFile(FILE_NAME);
      assertExpectedFileIO();
   }
}
