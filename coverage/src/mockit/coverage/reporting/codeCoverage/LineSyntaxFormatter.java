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
package mockit.coverage.reporting.codeCoverage;

import java.util.*;

final class LineSyntaxFormatter
{
   private static final List<String> JAVA_KEYWORDS = Arrays.asList(
      "abstract", "assert", "boolean", "break", "byte", "catch", "char", "class", "continue",
      "double", "do", "else", "extends", "false", "finally", "final", "float", "for",
      "if", "implements", "import", "interface", "int", "long", "native", "new", "null",
      "package", "private", "protected", "public", "return", "short", "static", "strictfp",
      "super", "synchronized", "this", "throws", "throw", "true", "try", "void", "volatile",
      "while");

   void format(LineSegment initialSegment)
   {
      for (LineSegment lineSegment : initialSegment) {
         if (lineSegment.isCode()) {
            formatCodeSegment(lineSegment);
         }
         else if (lineSegment.isComment()) {
            lineSegment.wrapBetween("<span class='comment'>", "</span>");
         }
      }
   }

   private void formatCodeSegment(LineSegment segment)
   {
      String text = segment.getText().toString();

      if (JAVA_KEYWORDS.contains(text)) {
         segment.wrapBetween("<span class='keyword'>", "</span>");
      }
      else if (text.charAt(0) == '@') {
         segment.wrapBetween("<span class='annotation'>", "</span>");
      }
   }
}
