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

final class ListOfCallPoints
{
   private static final String EOL = System.getProperty("line.separator");

   void insertListOfCallPoints(StringBuilder formattedLine, List<CallPoint> callPoints)
   {
      formattedLine.append("        <ol style='display: none;'>").append(EOL);

      for (CallPoint callPoint : callPoints) {
         StackTraceElement ste = callPoint.getStackTraceElement();

         formattedLine.append("          <li>");
         formattedLine.append(ste.getClassName()).append('#');
         formattedLine.append(ste.getMethodName().replaceFirst("<", "&lt;")).append(':');
         formattedLine.append(ste.getLineNumber());
         formattedLine.append("</li>").append(EOL);
      }

      formattedLine.append("        </ol>").append(EOL);
   }
}
