/*
 * JMockit Coverage
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
package mockit.coverage.reporting.parsing;

import static java.util.Arrays.*;
import java.util.*;

public final class LineElement implements Iterable<LineElement>
{
   private static final List<String> CONDITIONAL_OPERATORS = asList("||", "&&", ":");
   private static final List<String> CONDITIONAL_INSTRUCTIONS = asList("if", "for", "while");

   enum ElementType { CODE, COMMENT, SEPARATOR }

   private final ElementType type;
   private final String text;
   private String openingTag;
   private String closingTag;
   private LineElement next;

   private boolean underConditionalStatement;
   private int parenthesesBalance;

   LineElement(ElementType type, String text)
   {
      this.type = type;
      this.text = text.replaceAll("<", "&lt;");
   }

   public boolean isCode() { return type == ElementType.CODE; }
   public boolean isComment() { return type == ElementType.COMMENT; }

   public boolean isKeyword(String keyword)
   {
      return isCode() && text.equals(keyword);
   }

   public boolean isDotSeparator()
   {
      return type == ElementType.SEPARATOR && text.charAt(0) == '.';
   }

   public String getText()
   {
      return text;
   }

   public LineElement getNext()
   {
      return next;
   }

   public LineElement getNextCodeElement()
   {
      for (LineElement element : next) {
         if (element.isCode()) {
            return element;
         }
      }
      
      return null;
   }

   public void wrapText(String openingTag, String closingTag)
   {
      this.openingTag = openingTag;
      this.closingTag = closingTag;
   }

   void setNext(LineElement next)
   {
      this.next = next;
   }

   public LineElement appendUntilNextCodeElement(StringBuilder line)
   {
      LineElement element = this;

      while (!element.isCode()) {
         element.appendText(line);
         element = element.next;

         if (element == null) {
            break;
         }

         copyConditionalTrackingState(element);
      }

      return element;
   }

   private void copyConditionalTrackingState(LineElement destination)
   {
      destination.underConditionalStatement = underConditionalStatement;
      destination.parenthesesBalance = parenthesesBalance;
   }

   private void appendText(StringBuilder line)
   {
      if (openingTag == null) {
         line.append(text);
      }
      else {
         line.append(openingTag).append(text).append(closingTag);
      }
   }

   public LineElement findNextBranchingPoint()
   {
      if (!underConditionalStatement) {
         underConditionalStatement = isConditionalStatement();
      }

      if (isBranchingElement()) {
         if (next != null) {
            copyConditionalTrackingState(next);
         }

         return this;
      }

      if (underConditionalStatement) {
         int balance = getParenthesisBalance();
         parenthesesBalance += balance;

         if (balance != 0 && parenthesesBalance == 0) {
            return next;
         }
      }

      if (next == null) {
         return null;
      }

      copyConditionalTrackingState(next);
      return next.findNextBranchingPoint();
   }

   private boolean isConditionalStatement()
   {
      return CONDITIONAL_INSTRUCTIONS.contains(text);
   }

   public boolean isBranchingElement()
   {
      return "else".equals(text) || CONDITIONAL_OPERATORS.contains(text);
   }

   private int getParenthesisBalance()
   {
      if (text.indexOf('(') >= 0) {
         return 1;
      }
      else if (text.indexOf(')') >= 0) {
         return -1;
      }

      return 0;
   }

   public LineElement findWord(String word)
   {
      for (LineElement element : this) {
         if (element.isCode() && word.equals(element.text)) {
            return element;
         }
      }

      return null;
   }

   public int getBraceBalanceUntilEndOfLine()
   {
      int balance = 0;

      for (LineElement element : this) {
         balance += element.getBraceBalance();
      }

      return balance;
   }

   private int getBraceBalance()
   {
      if (isCode() && text.length() == 1) {
         char c = text.charAt(0);

         if (c == '{') {
            return 1;
         }
         else if (c == '}') {
            return -1;
         }
      }

      return 0;
   }

   public void appendAllBefore(StringBuilder line, LineElement elementToStopBefore)
   {
      LineElement elementToPrint = this;

      do {
         elementToPrint.appendText(line);
         elementToPrint = elementToPrint.next;
      }
      while (elementToPrint != elementToStopBefore);
   }

   public Iterator<LineElement> iterator()
   {
      return new Iterator<LineElement>()
      {
         private LineElement current = LineElement.this;

         public boolean hasNext()
         {
            return current != null;
         }

         public LineElement next()
         {
            if (current == null) {
               throw new NoSuchElementException();
            }

            LineElement next = current;
            current = current.next;

            return next;
         }

         public void remove() {}
      };
   }

   @Override
   public String toString()
   {
      StringBuilder line = new StringBuilder(200);

      for (LineElement element : this) {
         element.appendText(line);
      }

      return line.toString();
   }
}
