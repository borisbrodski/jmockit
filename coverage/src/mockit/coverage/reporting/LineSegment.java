/*
 * JMockit Coverage
 * Copyright (c) 2007 Rogério Liesenfeld
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
package mockit.coverage.reporting;

import static java.util.Arrays.*;
import java.util.*;

final class LineSegment implements Iterable<LineSegment>
{
   private static final List<String> RELATIONAL_OPERATORS =
      asList("==", "!=", "<", ">", "<=", ">=", "equals");
   private static final List<String> CONDITIONAL_OPERATORS = asList("||", "&&");
   private static final List<String> CONDITIONAL_INSTRUCTIONS = asList("if", "for", "while");

   enum SegmentType { CODE, COMMENT, SEPARATOR }

   private final SegmentType type;
   private final String unformattedText;
   private final StringBuilder text;
   private final boolean containsConditionalOperator;
   private final boolean containsConditionalInstruction;
   private LineSegment next;

   LineSegment(SegmentType type, String text)
   {
      this.type = type;
      unformattedText = text.replaceAll("<", "&lt;");
      this.text = new StringBuilder(unformattedText);

      String trimmedText = text.trim();
      containsConditionalOperator =
         RELATIONAL_OPERATORS.contains(trimmedText) || CONDITIONAL_OPERATORS.contains(trimmedText);
      containsConditionalInstruction = CONDITIONAL_INSTRUCTIONS.contains(trimmedText);
   }

   boolean isCode() { return type == SegmentType.CODE; }
   boolean isComment() { return type == SegmentType.COMMENT; }
   boolean isSeparator() { return type == SegmentType.SEPARATOR; }

   boolean containsConditionalOperator()
   {
      return containsConditionalOperator;
   }

   static boolean isRelationalOperator(String source)
   {
      return RELATIONAL_OPERATORS.contains(source);
   }

   static boolean isConditionalOperator(String source)
   {
      return CONDITIONAL_OPERATORS.contains(source);
   }

   boolean containsConditionalInstruction()
   {
      return containsConditionalInstruction;
   }

   int getCharCount(char paren)
   {
      int count = 0;

      for (int p = 0; p < text.length(); p++) {
         char c = text.charAt(p);

         if (c == paren) {
            count++;
         }
      }

      return count;
   }

   String getUnformattedText()
   {
      return unformattedText;
   }

   CharSequence getText()
   {
      return text;
   }

   void wrapBetween(String before, String after)
   {
      text.insert(0, before);
      text.append(after);
   }

   LineSegment getNext()
   {
      return next;
   }

   void setNext(LineSegment next)
   {
      this.next = next;
   }

   boolean before(LineSegment other)
   {
      for (LineSegment segmentAfter : this) {
         if (segmentAfter == other) {
            return true;
         }
      }

      return false;
   }

   public Iterator<LineSegment> iterator()
   {
      return new Iterator<LineSegment>()
      {
         private LineSegment current = LineSegment.this;

         public boolean hasNext()
         {
            return current != null;
         }

         public LineSegment next()
         {
            if (current == null) {
               throw new NoSuchElementException();
            }

            LineSegment next = current;
            current = current.next;

            return next;
         }

         public void remove()
         {
         }
      };
   }

   @Override
   public String toString()
   {
      StringBuilder line = new StringBuilder(200);

      for (LineSegment segment : this) {
         line.append(segment.text);
      }

      return line.toString();
   }
}
