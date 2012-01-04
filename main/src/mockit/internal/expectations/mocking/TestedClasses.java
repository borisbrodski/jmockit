/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.*;
import java.util.*;

import mockit.*;

final class TestedClasses
{
   final List<Field> testedFields;
   final List<MockedType> injectableFields;

   TestedClasses()
   {
      testedFields = new LinkedList<Field>();
      injectableFields = new ArrayList<MockedType>();
   }

   boolean findTestedAndInjectableFields(Object objectWithTestedFields)
   {
      for (Field field: objectWithTestedFields.getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(Tested.class)) {
            testedFields.add(field);
         }
         else {
            MockedType mockedType = new MockedType(field, true);

            if (mockedType.injectable) {
               injectableFields.add(mockedType);
            }
         }
      }

      return !testedFields.isEmpty();
   }
}
