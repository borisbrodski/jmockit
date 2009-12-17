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

import static java.util.Arrays.*;
import java.util.*;

public final class LineElement implements Iterable<LineElement>
{
   private static final List<String> CONDITIONAL_OPERATORS = asList("||", "&&", ":");
   private static final List<String> CONDITIONAL_INSTRUCTIONS = asList("if", "for", "while");

   enum ElementType { CODE, COMMENT, SEPARATOR }

   private final ElementType type;
   private final String text;
   private LineElement next;

   LineElement(ElementType type, String text)
   {
      this.type = type;
      this.text = text.replaceAll("<", "&lt;");
   }

   public boolean isCode() { return type == ElementType.CODE; }
   public boolean isComment() { return type == ElementType.COMMENT; }

   public String getText()
   {
      return text;
   }

   public LineElement getNext()
   {
      return next;
   }

   void setNext(LineElement next)
   {
      this.next = next;
   }

   public LineElement appendUntilNextCodeElement(StringBuilder line)
   {
      LineElement element = this;

      while (element != null && !element.isCode()) {
         line.append(element.text);
         element = element.next;
      }

      return element;
   }

   public LineElement findNextBranchingPoint()
   {
      boolean underConditionalStatement = false;
      int parenthesesBalance = -1;
      LineElement element = this;

      while (element != null) {
         if (!underConditionalStatement) {
            underConditionalStatement = isConditionalStatement();
            parenthesesBalance = 0;
         }

         if (element.isBranchingElement()) {
            break;
         }

         if (underConditionalStatement) {
            int balance = element.getParenthesisBalance();
            parenthesesBalance += balance;

            if (balance != 0 && parenthesesBalance == 0) {
               return element.next;
            }
         }

         element = element.next;
      }

      return element;
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

   public void appendAllBefore(StringBuilder line, LineElement elementToStopBefore)
   {
      LineElement elementToPrint = this;

      do {
         line.append(elementToPrint.text);
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
         line.append(element.text);
      }

      return line.toString();
   }
}
