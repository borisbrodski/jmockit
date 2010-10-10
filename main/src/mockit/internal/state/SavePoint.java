/*
 * JMockit
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
package mockit.internal.state;

import java.util.*;

public final class SavePoint
{
   private static SavePoint savePointForTestClass;

   private final Set<String> previousTransformedClasses;
   private final Set<Class<?>> previousRedefinedClasses;
   private final int previousMockInstancesCount;

   public SavePoint()
   {
      previousTransformedClasses = getCopyOfAllTransformedClasses();
      previousRedefinedClasses = getCopyOfAllRedefinedClasses();
      previousMockInstancesCount = TestRun.getMockClasses().getRegularMocks().getInstanceCount();
   }

   private Set<String> getCopyOfAllTransformedClasses()
   {
      return new HashSet<String>(TestRun.mockFixture().getTransformedClasses());
   }

   private Set<Class<?>> getCopyOfAllRedefinedClasses()
   {
      return new HashSet<Class<?>>(TestRun.mockFixture().getRedefinedClasses());
   }

   public void rollback()
   {
      restoreClassesTransformedAfterSavepoint();
      restoreClassesRedefinedAfterSavepoint();
      TestRun.getMockClasses().getRegularMocks().removeInstances(previousMockInstancesCount);
   }

   private void restoreClassesTransformedAfterSavepoint()
   {
      Set<String> classesToRestore = getCopyOfAllTransformedClasses();
      classesToRestore.removeAll(previousTransformedClasses);

      if (!classesToRestore.isEmpty()) {
         TestRun.mockFixture().restoreAndRemoveTransformedClasses(classesToRestore);
      }
   }

   private void restoreClassesRedefinedAfterSavepoint()
   {
      Set<Class<?>> classesToRestore = getCopyOfAllRedefinedClasses();
      classesToRestore.removeAll(previousRedefinedClasses);

      if (!classesToRestore.isEmpty()) {
         TestRun.mockFixture().restoreAndRemoveRedefinedClasses(classesToRestore);
      }
   }

   public static void registerNewActiveSavePoint()
   {
      savePointForTestClass = new SavePoint();
   }

   public static void rollbackForTestClass()
   {
      if (savePointForTestClass != null) {
         savePointForTestClass.rollback();
         savePointForTestClass = null;
      }
   }
}
