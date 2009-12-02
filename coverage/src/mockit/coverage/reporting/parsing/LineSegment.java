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

public final class LineSegment implements Iterable<LineSegment>
{
   private static final List<String> RELATIONAL_OPERATORS =
      asList("==", "!=", "<", ">", "<=", ">=", "equals");
   private static final List<String> CONDITIONAL_OPERATORS = asList("||", "&&");
   private static final List<String> CONDITIONAL_INSTRUCTIONS = asList("if", "for", "while");

   enum SegmentType { CODE, COMMENT, SEPARATOR }

   private final SegmentType type;
   private final String unformattedText;
   private final boolean containsConditionalOperator;
   private final boolean containsConditionalInstruction;
   private LineSegment next;

   LineSegment(SegmentType type, String text)
   {
      this.type = type;
      unformattedText = text.replaceAll("<", "&lt;");

      String trimmedText = text.trim();
      containsConditionalOperator =
         RELATIONAL_OPERATORS.contains(trimmedText) || CONDITIONAL_OPERATORS.contains(trimmedText);
      containsConditionalInstruction = CONDITIONAL_INSTRUCTIONS.contains(trimmedText);
   }

   public boolean isCode() { return type == SegmentType.CODE; }
   public boolean isComment() { return type == SegmentType.COMMENT; }
   boolean isSeparator() { return type == SegmentType.SEPARATOR; }

   public boolean containsConditionalOperator()
   {
      return containsConditionalOperator;
   }

   public static boolean isRelationalOperator(String source)
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

      for (int p = 0; p < unformattedText.length(); p++) {
         char c = unformattedText.charAt(p);

         if (c == paren) {
            count++;
         }
      }

      return count;
   }

   public CharSequence getText()
   {
      return unformattedText;
   }

   public LineSegment getNext()
   {
      return next;
   }

   void setNext(LineSegment next)
   {
      this.next = next;
   }

   public boolean before(LineSegment other)
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

         public void remove() {}
      };
   }

   @Override
   public String toString()
   {
      StringBuilder line = new StringBuilder(200);

      for (LineSegment segment : this) {
         line.append(segment.unformattedText);
      }

      return line.toString();
   }
}
