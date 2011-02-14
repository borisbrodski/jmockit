/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
   private static final String SEPARATORS = ".,;()";

   private int lineNum;
   private String line;
   private LineElement initialElement;
   private boolean inComments;

   // Helper fields:
   private LineElement currentElement;
   private int lineLength;
   private int startPos;
   private boolean inCodeElement;
   private int pos;
   private int currChar;

   public int getNumber()
   {
      return lineNum;
   }

   public boolean isInComments()
   {
      return inComments;
   }

   public boolean isBlankLine()
   {
      int n = line.length();

      for (int i = 0; i < n; i++) {
         char c = line.charAt(i);

         if (!Character.isWhitespace(c)) {
            return false;
         }
      }

      return true;
   }

   public LineElement getInitialElement()
   {
      return initialElement;
   }

   boolean parse(String line)
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

      return !inComments && !isBlankLine();
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
