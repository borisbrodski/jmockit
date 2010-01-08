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
   private final StringBuilder openingTag;
   private final DataCoverageInfo coverageInfo;
   private int nextField;
   private String classAndFieldNames;
   private String className;
   private String fieldName;

   public DataCoverageOutput(DataCoverageInfo coverageInfo)
   {
      openingTag = new StringBuilder(50);
      this.coverageInfo = coverageInfo;
      moveToNextField();
   }

   private void moveToNextField()
   {
      if (nextField >= coverageInfo.allFields.size()) {
         classAndFieldNames = null;
         className = null;
         fieldName = null;
         return;
      }

      classAndFieldNames = coverageInfo.allFields.get(nextField);
      nextField++;

      int p = classAndFieldNames.indexOf('.');
      className = classAndFieldNames.substring(0, p);
      fieldName = classAndFieldNames.substring(p + 1);
   }

   public void writeCoverageInfoIfLineStartsANewFieldDeclaration(FileParser fileParser)
   {
      if (classAndFieldNames != null && className.equals(fileParser.getCurrentlyPendingClass())) {
         LineElement initialLineElement = fileParser.lineParser.getInitialElement();
         LineElement elementWithFieldName = initialLineElement.findWord(fieldName);

         if (elementWithFieldName != null) {
            buildOpeningTagForFieldWrapper();
            elementWithFieldName.wrapText(openingTag.toString(), "</span>");
            moveToNextField();
         }
      }
   }

   private void buildOpeningTagForFieldWrapper()
   {
      openingTag.setLength(0);
      openingTag.append("<span class='");

      StaticFieldData staticData = coverageInfo.getStaticFieldData(classAndFieldNames);
      boolean staticField = staticData != null;
      openingTag.append(staticField ? "static" : "instance");

      openingTag.append(coverageInfo.isCovered(classAndFieldNames) ? " covered" : " uncovered");

      InstanceFieldData instanceData = coverageInfo.getInstanceFieldData(classAndFieldNames);

      if (staticField || instanceData != null) {
         openingTag.append("' title='");
         appendAccessCounts(staticField ? staticData : instanceData);
      }

      openingTag.append("'>");
   }

   private void appendAccessCounts(FieldData fieldData)
   {
      openingTag.append("Reads: ").append(fieldData.getReadCount());
      openingTag.append(" Writes: ").append(fieldData.getWriteCount());
   }
}
