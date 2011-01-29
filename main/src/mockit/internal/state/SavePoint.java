/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.util.*;

public final class SavePoint
{
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
      TestRun.setSavePointForTestClass(new SavePoint());
   }

   public static void rollbackForTestClass()
   {
      SavePoint savePoint = TestRun.getSavePointForTestClass();

      if (savePoint != null) {
         savePoint.rollback();
         TestRun.setSavePointForTestClass(null);
      }
   }
}
