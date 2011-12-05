/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
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
//   private final List<MockedType> mockedTypes;

   TestedClasses()
   {
      testedFields = new LinkedList<Field>();
      injectableFields = new ArrayList<MockedType>();
//      mockedTypes = new ArrayList<MockedType>();
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

            // TODO: move the commented code out before release 1.0
//            if (mockedType.isMockField()) {
//               mockedTypes.add(mockedType);
//            }
         }
      }

      return !testedFields.isEmpty();
   }

/*
   void redefineTestedClasses()
   {
      for (Field testedField : testedFields) {
         redefineTestedClass(testedField.getType());
      }
   }

   private void redefineTestedClass(Class<?> testedClass)
   {
      ClassReader cr = ClassFile.createClassFileReader(testedClass.getName());
      TestedClassModifier modifier = new TestedClassModifier(cr, mockedTypes);
      cr.accept(modifier, 0);
      byte[] modifiedClass = modifier.toByteArray();

      ClassDefinition classDefinition = new ClassDefinition(testedClass, modifiedClass);
      RedefinitionEngine.redefineClasses(classDefinition);
   }
*/
}
