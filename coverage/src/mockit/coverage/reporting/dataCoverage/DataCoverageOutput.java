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
package mockit.coverage.reporting.dataCoverage;

import mockit.coverage.data.dataItems.*;
import mockit.coverage.reporting.parsing.*;

public final class DataCoverageOutput
{
   private static final String COVERED = "<span class='covered'>";
   private static final String UNCOVERED = "<span class='uncovered'>";

   private final DataCoverageInfo coverageInfo;
   private int nextField;

   public DataCoverageOutput(DataCoverageInfo coverageInfo)
   {
      this.coverageInfo = coverageInfo;
   }

   public void writeCoverageInfoIfLineStartsANewFieldDeclaration(LineParser lineParser)
   {
      if (nextField < coverageInfo.allFields.size()) {
         String classAndFieldNames = coverageInfo.allFields.get(nextField);
         int p = classAndFieldNames.indexOf('.');
         String className = classAndFieldNames.substring(0, p);
         String fieldName = classAndFieldNames.substring(p + 1);

         LineElement elementWithFieldName = lineParser.getInitialElement().findWord(fieldName);

         if (elementWithFieldName != null) {
            String openingTag = coverageInfo.isCovered(classAndFieldNames) ? COVERED : UNCOVERED;
            elementWithFieldName.wrapText(openingTag, "</span>");
            nextField++;
         }
      }
   }
}
