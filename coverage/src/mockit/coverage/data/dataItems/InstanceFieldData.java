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

import java.util.*;

import mockit.internal.state.*;

public final class InstanceFieldData extends FieldData
{
   private static final long serialVersionUID = 6991762113575259754L;

   private final transient Map<Integer, List<Integer>> testIdsToAssignments =
      new HashMap<Integer, List<Integer>>();

   void registerAssignment(Object instance)
   {
      List<Integer> dataForRunningTest = getDataForRunningTest();
      Integer instanceId = System.identityHashCode(instance);

      if (!dataForRunningTest.contains(instanceId)) {
         dataForRunningTest.add(instanceId);
      }

      writeCount++;
   }

   void registerRead(Object instance)
   {
      List<Integer> dataForRunningTest = getDataForRunningTest();
      Integer instanceId = System.identityHashCode(instance);

      dataForRunningTest.remove(instanceId);
      readCount++;
   }

   private List<Integer> getDataForRunningTest()
   {
      int testId = TestRun.getTestId();
      List<Integer> fieldData = testIdsToAssignments.get(testId);

      if (fieldData == null) {
         fieldData = new LinkedList<Integer>();
         testIdsToAssignments.put(testId, fieldData);
      }

      return fieldData;
   }

   @Override
   void markAsCoveredIfNoUnreadValuesAreLeft()
   {
      for (List<Integer> unreadInstances : testIdsToAssignments.values()) {
         if (unreadInstances.isEmpty()) {
            covered = true;
            break;
         }
      }
   }

   public List<Integer> getOwnerInstancesWithUnreadAssignments()
   {
      if (isCovered()) {
         return Collections.emptyList();
      }

      return testIdsToAssignments.values().iterator().next();
   }
}
