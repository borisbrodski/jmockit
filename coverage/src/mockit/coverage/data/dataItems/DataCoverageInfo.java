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
package mockit.coverage.data.dataItems;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.internal.state.TestRun;

public final class DataCoverageInfo implements Serializable
{
   private static final long serialVersionUID = -4561686103982673490L;

   public final Map<String, Map<Integer, Boolean>> staticFieldsData =
      new LinkedHashMap<String, Map<Integer, Boolean>>();
   public final Map<String, Map<Integer, List<Integer>>> instanceFieldsData =
      new LinkedHashMap<String, Map<Integer, List<Integer>>>();

   private transient int coveredDataItems = -1;

   public void addField(String className, String fieldName, boolean isStatic)
   {
      String classAndFieldNames = className + '.' + fieldName;

      if (isStatic) {
         staticFieldsData.put(classAndFieldNames, new HashMap<Integer, Boolean>());
      }
      else {
         instanceFieldsData.put(classAndFieldNames, new HashMap<Integer, List<Integer>>());
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
      setStaticFieldData(classAndFieldNames, Boolean.TRUE);
   }

   public void registerReadOfStaticField(String classAndFieldNames)
   {
      setStaticFieldData(classAndFieldNames, null);
   }

   private void setStaticFieldData(String classAndFieldNames, Boolean data)
   {
      Map<Integer, Boolean> testToFieldData = staticFieldsData.get(classAndFieldNames);
      int testId = TestRun.getTestId();
      testToFieldData.put(testId, data);
   }

   public void registerAssignmentToInstanceField(Object instance, String classAndFieldNames)
   {
      List<Integer> fieldData = getInstanceFieldData(classAndFieldNames);
      Integer instanceId = System.identityHashCode(instance);

      if (!fieldData.contains(instanceId)) {
         fieldData.add(instanceId);
      }
   }

   private List<Integer> getInstanceFieldData(String classAndFieldNames)
   {
      Map<Integer, List<Integer>> testToFieldData = instanceFieldsData.get(classAndFieldNames);
      int testId = TestRun.getTestId();
      List<Integer> fieldData = testToFieldData.get(testId);

      if (fieldData == null) {
         fieldData = new LinkedList<Integer>();
         testToFieldData.put(testId, fieldData);
      }

      return fieldData;
   }

   public void registerReadOfInstanceField(Object instance, String classAndFieldNames)
   {
      List<Integer> fieldData = getInstanceFieldData(classAndFieldNames);
      Integer instanceId = System.identityHashCode(instance);
      fieldData.remove(instanceId);
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

      for (Map<Integer, Boolean> withUnreadValue : staticFieldsData.values()) {
         for (Boolean unread : withUnreadValue.values()) {
            if (unread == null) {
               coveredDataItems++;
               break;
            }
         }
      }

      for (Map<Integer, List<Integer>> withUnreadValue : instanceFieldsData.values()) {
         for (List<Integer> unreadInstances : withUnreadValue.values()) {
            if (unreadInstances.isEmpty()) {
               coveredDataItems++;
               break;
            }
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

      return CoveragePercentage.calculate(coveredDataItems, totalFields);
   }
}