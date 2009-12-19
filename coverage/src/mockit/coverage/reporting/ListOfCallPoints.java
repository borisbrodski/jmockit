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
package mockit.coverage.reporting;

import java.util.*;

import mockit.coverage.*;

public final class ListOfCallPoints
{
   private static final String EOL = System.getProperty("line.separator");

   private final StringBuilder content;
   private int n;

   public ListOfCallPoints()
   {
      content = new StringBuilder(100);
   }

   public void insertListOfCallPoints(List<CallPoint> callPoints)
   {
      if (content.length() == 0) {
         content.append(EOL).append("      ");
      }

      content.append("  <ol style='display: none;'>").append(EOL);
      n = 1;

      StackTraceElement previous = null;

      for (CallPoint callPoint : callPoints) {
         StackTraceElement current = callPoint.getStackTraceElement();

         if (previous == null) {
            appendTestMethod(current);
         }
         else if (!isSameTestMethod(current, previous)) {
            appendRepetitionCountIfAny();
            content.append("</li>").append(EOL);
            appendTestMethod(current);
         }
         else if (current.getLineNumber() == previous.getLineNumber()) {
            n++;
         }
         else {
            appendRepetitionCountIfAny();
            content.append(", ").append(current.getLineNumber());
         }

         previous = current;
      }

      content.append("        </ol>").append(EOL).append("      ");
   }

   private void appendTestMethod(StackTraceElement current)
   {
      content.append("          <li>");
      content.append(current.getClassName()).append('#');
      content.append(current.getMethodName().replaceFirst("<", "&lt;")).append(": ");
      content.append(current.getLineNumber());
   }

   private void appendRepetitionCountIfAny()
   {
      if (n > 1) {
         content.append('x').append(n);
         n = 1;
      }
   }

   private boolean isSameTestMethod(StackTraceElement ste1, StackTraceElement ste2)
   {
      return
         ste1 == ste2 ||
         ste1.getClassName().equals(ste2.getClassName()) &&
         ste1.getMethodName().equals(ste2.getMethodName());
   }

   public String getContents()
   {
      String result = content.toString();
      content.setLength(0);
      return result;
   }
}
