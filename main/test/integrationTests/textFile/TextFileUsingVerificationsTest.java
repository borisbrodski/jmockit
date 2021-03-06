/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests.textFile;

import java.io.*;
import java.util.*;

import org.junit.*;

import mockit.*;

import integrationTests.textFile.TextFile.*;
import static org.junit.Assert.*;

public final class TextFileUsingVerificationsTest
{
   @Test
   public void createTextFile(DefaultTextReader reader) throws Exception
   {
      assertNotNull(reader);

      new TextFile("file", 0);

      new Verifications() {{ new DefaultTextReader("file"); }};
   }

   @Test
   public void createTextFileByCapturingTheTextReaderClassThroughItsBaseType(@Capturing TextReader reader)
      throws Exception
   {
      new TextFile("file", 0);
   }

   @Test
   public void createTextFileByMockingTheTextReaderClassThroughItsName(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader") Object reader)
      throws Exception
   {
      new TextFile("file", 0);
   }

   @Test
   public void createTextFileWhileVerifyingTheCreatedTextReaderIsClosed(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader")
      final TextReader reader) throws Exception
   {
      new TextFile("file", 0).closeReader();

      new Verifications() {{ reader.close(); }};
   }

   @Test
   public void createTextFileVerifyingInvocationsThroughReflection(
      @Mocked(realClassName = "integrationTests.textFile.TextFile$DefaultTextReader") final Object reader)
      throws Exception
   {
      new TextFile("file", 0).closeReader();

      new FullVerificationsInOrder() {{
         newInstance("integrationTests.textFile.TextFile$DefaultTextReader", "file");
         invoke(reader, "close");
      }};
   }

   @Test
   public void parseTextFileUsingConcreteClass(final DefaultTextReader reader) throws Exception
   {
      new NonStrictExpectations() {{
         reader.readLine(); returns("line1", "another,line", null);
      }};

      TextFile textFile = new TextFile("file", 200);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new Verifications() {{ reader.close(); }};
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
   public void parseTextFileUsingInterface(@NonStrict final TextReader reader) throws Exception
   {
      new Expectations() {{
         reader.readLine(); returns("line1", "another,line", null);
      }};

      TextFile textFile = new TextFile(reader, 100);
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new VerificationsInOrder() {{
         reader.skip(100);
         reader.close();
      }};
   }

   @Test
   public void parseTextFileUsingBufferedReader(final BufferedReader reader) throws Exception
   {
      new NonStrictExpectations() {
         final FileReader fileReader = null;

         {
            reader.readLine(); returns("line1", "another,line", null);
         }
      };

      TextFile textFile = new TextFile("file");
      List<String[]> result = textFile.parse();

      assertResultFromTextFileParsing(result);

      new Verifications() {{ reader.close(); }};
   }
}