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
package mockit.coverage.output;

import java.io.*;
import java.util.*;

import mockit.coverage.*;

public final class FullXmlWriter extends XmlWriter
{
   public FullXmlWriter(CoverageData coverageData)
   {
      super(coverageData);
   }

   @Override
   protected boolean writeChildElementsForLine(LineCoverageData lineData) throws IOException
   {
      if (lineData.containsCallPoints()) {
         output.write("'>");
         output.newLine();
         writeChildElementsForCallPoints(lineData.getCallPoints(), "");
         return true;
      }
      
      return false;
   }

   @Override
   protected void writeEndTagForSegment(BranchCoverageData data) throws IOException
   {
      List<CallPoint> callPoints = data.getCallPoints();

      if (callPoints == null) {
         output.write("/>");
      }
      else {
         output.write(">");
         output.newLine();
         writeChildElementsForCallPoints(callPoints, "  ");
         output.write("      </segment>");
      }
   }

   private void writeChildElementsForCallPoints(List<CallPoint> callPoints, String indent)
      throws IOException
   {
      for (CallPoint callPoint : callPoints) {
         StackTraceElement ste = callPoint.getStackTraceElement();

         output.write(indent);
         output.write("      <callPoint class='");
         output.write(ste.getClassName());
         output.write("' method='");
         output.write(ste.getMethodName());
         output.write("' line='");
         writeInt(ste.getLineNumber());
         output.write("'/>");
         output.newLine();
      }
   }
}
