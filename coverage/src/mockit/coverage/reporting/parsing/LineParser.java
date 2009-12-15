/*
 * JMockit Coverage
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
package mockit.coverage.reporting.parsing;

import mockit.coverage.reporting.parsing.LineElement.*;

/**
 * Parses a source line into one or more consecutive segments, identifying which ones contain Java
 * code and which ones contain only comments.
 * Block comments initiated in a previous line are kept track of until the end of the block is
 * reached.
 */
public final class LineParser
{
   private static final String SEPARATORS = ".,;:()";

   private int lineNum;
   private String line;
   private LineElement initialElement;
   private LineElement currentElement;
   private boolean inComments;

   // Helper variables:
   private int lineLength;
   private int startPos;
   private boolean inCodeElement;
   private int pos;
   private int currChar;

   public int getNumber()
   {
      return lineNum;
   }

   public boolean isBlankLine()
   {
      return line.trim().length() == 0;
   }

   public boolean isInComments()
   {
      return inComments;
   }

   public LineElement getInitialElement()
   {
      return initialElement;
   }

   public void parse(String line)
   {
      lineNum++;
      initialElement = null;
      currentElement = null;
      this.line = line;
      lineLength = line.length();
      startPos = inComments ? 0 : -1;
      inCodeElement = false;

      for (pos = 0; pos < lineLength; pos++) {
         currChar = line.codePointAt(pos);

         if (parseComment()) {
            break;
         }

         parseSeparatorsAndCode();
      }

      if (startPos >= 0) {
         addElement(0);
      }
   }

   private void parseSeparatorsAndCode()
   {
      boolean separator = isSeparator();

      if (!inCodeElement && separator) {
         startNewElementIfNotYetStarted();
      }
      else if (!inCodeElement && !separator) {
         if (startPos >= 0) {
            addElement(pos);
         }

         inCodeElement = true;
         startPos = pos;
      }
      else if (separator) {
         addElement(pos);
         inCodeElement = false;
         startPos = pos;
      }
   }

   private boolean isSeparator()
   {
      return Character.isWhitespace(currChar) || SEPARATORS.indexOf(currChar) >= 0;
   }

   private void startNewElementIfNotYetStarted()
   {
      if (startPos < 0) {
         startPos = pos;
      }
   }

   private boolean parseComment()
   {
      if (inComments && parseUntilEndOfLineOrEndOfComment()) {
         return true;
      }

      while (currChar == '/' && pos < lineLength - 1) {
         int c2 = line.codePointAt(pos + 1);

         if (c2 == '/') {
            endCodeElementIfPending();
            startNewElementIfNotYetStarted();
            inComments = true;
            addElement(0);
            inComments = false;
            startPos = -1;
            return true;
         }
         else if (c2 == '*') {
            endCodeElementIfPending();
            startNewElementIfNotYetStarted();
            inComments = true;
            pos += 2;

            if (parseUntilEndOfLineOrEndOfComment()) {
               return true;
            }
         }
         else {
            break;
         }
      }

      return false;
   }

   private void endCodeElementIfPending()
   {
      if (inCodeElement) {
         addElement(pos);
         startPos = pos;
         inCodeElement = false;
      }
   }

   private boolean parseUntilEndOfLineOrEndOfComment()
   {
      while (pos < lineLength) {
         currChar = line.codePointAt(pos);

         if (currChar == '*' && pos < lineLength - 1 && line.codePointAt(pos + 1) == '/') {
            pos += 2;
            addElement(pos);
            startPos = -1;
            inComments = false;
            break;
         }

         pos++;
      }

      if (pos < lineLength) {
         currChar = line.codePointAt(pos);
         return false;
      }
      else {
         return true;
      }
   }

   private void addElement(int p)
   {
      String text = p > 0 ? line.substring(startPos, p) : line.substring(startPos);
      ElementType type;

      if (inComments) {
         type = ElementType.COMMENT;
      }
      else if (inCodeElement) {
         type = ElementType.CODE;
      }
      else {
         type = ElementType.SEPARATOR;
      }

      LineElement newElement = new LineElement(type, text);

      if (initialElement == null) {
         initialElement = newElement;
         currentElement = newElement;
      }
      else {
         currentElement.setNext(newElement);
      }

      currentElement = newElement;
   }
}
