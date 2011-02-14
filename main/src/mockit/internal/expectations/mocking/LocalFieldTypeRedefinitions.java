/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.state.*;
import mockit.internal.util.*;

public final class LocalFieldTypeRedefinitions extends FieldTypeRedefinitions
{
   private Map<Type, Object> typesAndTargetObjects;

   public LocalFieldTypeRedefinitions(Object objectWithMockFields)
   {
      super(objectWithMockFields);
      typesAndTargetObjects = new HashMap<Type, Object>(2);
   }

   public void redefineTypesForNestedClass(Map<Type, Object> typesAndTargetObjects)
   {
      this.typesAndTargetObjects = typesAndTargetObjects;
      redefineFieldTypes(parentObject.getClass(), false);
      this.typesAndTargetObjects = null;
   }

   @Override
   protected void redefineTypeForMockField()
   {
      TypeRedefinition typeRedefinition = new TypeRedefinition(parentObject, typeMetadata);

      if (finalField) {
         typeRedefinition.redefineTypeForFinalField();
         registerMockedClassIfNonStrict();
         TestRun.getExecutingTest().addFinalLocalMockField(parentObject, typeMetadata);
      }
      else {
         Object mock = typeRedefinition.redefineType();
         Utilities.setFieldValue(field, parentObject, mock);
         registerMock(mock);
      }

      typesAndTargetObjects.put(typeMetadata.declaredType, parentObject);
      targetClasses.add(typeRedefinition.targetClass);
   }

   @Override
   public boolean captureNewInstanceForApplicableMockField(Object mock)
   {
      return
         captureOfNewInstances != null &&
         getCaptureOfNewInstances().captureNewInstanceForApplicableMockField(parentObject, mock);
   }
}
