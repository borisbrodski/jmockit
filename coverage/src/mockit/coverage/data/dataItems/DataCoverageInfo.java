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
package mockit.coverage.data.dataItems;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import mockit.coverage.*;

public final class DataCoverageInfo implements Serializable
{
   private static final long serialVersionUID = -4561686103982673490L;

   public final List<String> allFields = new ArrayList<String>(2);
   public final Map<String, StaticFieldData> staticFieldsData =
      new LinkedHashMap<String, StaticFieldData>();
   public final Map<String, InstanceFieldData> instanceFieldsData =
      new LinkedHashMap<String, InstanceFieldData>();

   private transient int coveredDataItems = -1;

   public void addField(String className, String fieldName, boolean isStatic)
   {
      String classAndField = className + '.' + fieldName;
      allFields.add(classAndField);

      if (isStatic) {
         staticFieldsData.put(classAndField, new StaticFieldData());
      }
      else {
         instanceFieldsData.put(classAndField, new InstanceFieldData());
      }
   }

   public boolean isFieldWithCoverageData(String classAndFieldNames)
   {
      return
         instanceFieldsData.containsKey(classAndFieldNames) ||
         staticFieldsData.containsKey(classAndFieldNames);
   }

   public void registerAssignmentToStaticField(String classAndFieldNames)
   {
      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);
      staticData.registerAssignment();
   }

   public StaticFieldData getStaticFieldData(String classAndFieldNames)
   {
      return staticFieldsData.get(classAndFieldNames);
   }

   public void registerReadOfStaticField(String classAndFieldNames)
   {
      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);
      staticData.registerRead();
   }

   public void registerAssignmentToInstanceField(Object instance, String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);
      instanceData.registerAssignment(instance);
   }

   public InstanceFieldData getInstanceFieldData(String classAndFieldNames)
   {
      return instanceFieldsData.get(classAndFieldNames);
   }

   public void registerReadOfInstanceField(Object instance, String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);
      instanceData.registerRead(instance);
   }

   public boolean hasFields()
   {
      return !allFields.isEmpty();
   }

   public boolean isCovered(String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);

      if (instanceData != null && instanceData.isCovered()) {
         return true;
      }

      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);

      return staticData != null && staticData.isCovered();
   }

   public int getTotalItems()
   {
      return staticFieldsData.size() + instanceFieldsData.size();
   }

   public int getCoveredItems()
   {
      if (coveredDataItems >= 0) {
         return coveredDataItems;
      }

      coveredDataItems = 0;

      for (StaticFieldData staticData : staticFieldsData.values()) {
         if (staticData.isCovered()) {
            coveredDataItems++;
         }
      }

      for (InstanceFieldData instanceData : instanceFieldsData.values()) {
         if (instanceData.isCovered()) {
            coveredDataItems++;
         }
      }

      return coveredDataItems;
   }

   public int getCoveragePercentage()
   {
      int totalFields = getTotalItems();

      if (totalFields == 0) {
         return -1;
      }

      return CoveragePercentage.calculate(getCoveredItems(), totalFields);
   }

   public void mergeInformation(DataCoverageInfo previousInfo)
   {
      addInfoFromPreviousTestRun(staticFieldsData, previousInfo.staticFieldsData);
      addFieldsFromPreviousTestRunIfAbsent(staticFieldsData, previousInfo.staticFieldsData);

      addInfoFromPreviousTestRun(instanceFieldsData, previousInfo.instanceFieldsData);
      addFieldsFromPreviousTestRunIfAbsent(instanceFieldsData, previousInfo.instanceFieldsData);
   }

   private <FI extends FieldData> void addInfoFromPreviousTestRun(
      Map<String, FI> currentInfo, Map<String, FI> previousInfo)
   {
      for (Entry<String, FI> nameAndInfo : currentInfo.entrySet()) {
         String fieldName = nameAndInfo.getKey();
         FieldData previousFieldInfo = previousInfo.get(fieldName);

         if (previousFieldInfo != null) {
            FieldData fieldInfo = nameAndInfo.getValue();
            fieldInfo.addCountsFromPreviousTestRun(previousFieldInfo);
         }
      }
   }

   private <FI extends FieldData> void addFieldsFromPreviousTestRunIfAbsent(
      Map<String, FI> currentInfo, Map<String, FI> previousInfo)
   {
      for (Entry<String, FI> nameAndInfo : previousInfo.entrySet()) {
         String fieldName = nameAndInfo.getKey();

         if (!currentInfo.containsKey(fieldName)) {
            currentInfo.put(fieldName, previousInfo.get(fieldName));
         }
      }
   }
}
