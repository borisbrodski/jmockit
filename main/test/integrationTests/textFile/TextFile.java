/*
 * JMockit: a Java class library for developer testing with "mock methods"
 * Copyright (c) 2006, 2007 Rogério Liesenfeld
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

public final class TextFile
{
   private final BufferedReader bufferedInput;
   private final ITextReader input;
   private final long headerLength;

   public TextFile(String fileName) throws FileNotFoundException
   {
      bufferedInput = new BufferedReader(new FileReader(fileName));
      input = null;
      headerLength = 0;
   }

   public TextFile(String fileName, long headerLength) throws FileNotFoundException
   {
      this(new TextReader(fileName), headerLength);
   }

   public TextFile(ITextReader input, long headerLength)
   {
      bufferedInput = null;
      this.input = input;
      this.headerLength = headerLength;
   }

   public List<String[]> parse()
   {
      skipHeader();

      List<String[]> result = new ArrayList<String[]>();

      while(true) {
         String  strLine = nextLine();

         if (strLine == null) {
            closeReader();
            break;
         }

         String[] parsedLine = strLine.split(",");
         result.add(parsedLine);
      }

      return result;
   }

   private void skipHeader()
   {
      try {
         if (bufferedInput != null) {
            bufferedInput.skip(headerLength);
         }
         else {
            input.skip(headerLength);
         }
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private String nextLine()
   {
      try {
         return bufferedInput != null ? bufferedInput.readLine() : input.readLine();
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void closeReader()
   {
      try {
         if (bufferedInput != null) {
            bufferedInput.close();
         }
         else {
            input.close();
         }
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public interface ITextReader
   {
      long skip(long n) throws IOException;
      String readLine() throws IOException;
      void close() throws IOException;
   }

   public static final class TextReader implements ITextReader
   {
      private final Reader reader;

      TextReader(String fileName) throws FileNotFoundException
      {
         reader = new FileReader(fileName);
      }

      public long skip(long n) throws IOException
      {
         return reader.skip(n);
      }

      public String readLine() throws IOException
      {
         StringBuilder buf = new StringBuilder();

         while (true) {
            int c = reader.read();

            if (c < 0 || c == '\n') {
               break;
            }

            buf.append((char) c);
         }

         return buf.toString();
      }

      public void close() throws IOException
      {
         reader.close();
      }
   }
}
