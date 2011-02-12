/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.instrument.*;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;

import static mockit.internal.util.Utilities.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;

public final class TestedClassRedefinitions
{
   private final List<Field> testedFields;
   private final List<MockedType> mockedTypes;

   public TestedClassRedefinitions()
   {
      testedFields = new LinkedList<Field>();
      mockedTypes = new ArrayList<MockedType>();
   }

   public boolean redefineTestedClasses(Object objectWithTestedFields)
   {
      for (Field field: objectWithTestedFields.getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(Tested.class)) {
            testedFields.add(field);
         }
         else {
            MockedType mockedType = new MockedType(field, true);

            if (mockedType.isMockField()) {
               mockedTypes.add(mockedType);
            }
         }
      }

      for (Field testedField : testedFields) {
         redefineTestedClass(testedField.getType());
      }

      return !testedFields.isEmpty();
   }

   private void redefineTestedClass(Class<?> testedClass)
   {
      ClassReader cr = ClassFile.createClassFileReader(testedClass.getName());
      TestedClassModifier modifier = new TestedClassModifier(cr, mockedTypes);
      cr.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      ClassDefinition classDefinition = new ClassDefinition(testedClass, modifiedClass);
      RedefinitionEngine.redefineClasses(classDefinition);
   }

   public void assignNewInstancesToTestedFields(Object objectWithMockFields)
   {
      for (Field testedField : testedFields) {
         Object testedObject = getFieldValue(testedField, objectWithMockFields);

         if (testedObject == null) {
            Class<?> testedClass = testedField.getType();
            Constructor<?>[] publicConstructors = testedClass.getConstructors();

            if (publicConstructors.length == 1) {
               Object newTestedObject = instantiateWithPublicConstructor(objectWithMockFields, publicConstructors[0]);
               injectMocksIntoFieldsThatAreStillNull(objectWithMockFields, testedClass, newTestedObject);
               setFieldValue(testedField, objectWithMockFields, newTestedObject);
            }
         }
      }
   }

   private Object instantiateWithPublicConstructor(Object objectWithMockFields, Constructor<?> constructor)
   {
      Object[] mockArguments = obtainInjectableMocks(objectWithMockFields, constructor.getGenericParameterTypes());
      return invoke(constructor, mockArguments);
   }

   private Object[] obtainInjectableMocks(Object parentObject, Type[] parameterTypes)
   {
      int n = parameterTypes.length;
      Object[] parameterValues = new Object[n];

      for (int i = 0; i < n; i++) {
         parameterValues[i] = getRequiredMockObject(parentObject, parameterTypes[i]);
      }

      return parameterValues;
   }

   private Object getRequiredMockObject(Object parentObject, Type declaredType)
   {
      MockedType mockedType = findInjectableMockedType(declaredType);

      if (mockedType == null) {
         throw new IllegalArgumentException("No injectable mock field of " + declaredType);
      }

      Object mock = getFieldValue(mockedType.field, parentObject);

      if (mock == null) {
         throw new IllegalArgumentException("No injectable mock instance available of " + declaredType);
      }

      return mock;
   }

   private MockedType findInjectableMockedType(Type declaredType)
   {
      for (MockedType mockedType : mockedTypes) {
         if (mockedType.injectable && mockedType.declaredType == declaredType) {
            return mockedType;
         }
      }

      return null;
   }

   private void injectMocksIntoFieldsThatAreStillNull(Object objectWithMockFields, Class<?> testedClass, Object tested)
   {
      Class<?> superClass = testedClass.getSuperclass();

      if (superClass != null && superClass.getProtectionDomain() == testedClass.getProtectionDomain()) {
         injectMocksIntoFieldsThatAreStillNull(objectWithMockFields, superClass, tested);
      }

      for (Field field : testedClass.getDeclaredFields()) {
         if (getFieldValue(field, tested) == null) {
            Object mock = getMockObjectIfAvailable(objectWithMockFields, field);
            setFieldValue(field, tested, mock);
         }
      }
   }

   private Object getMockObjectIfAvailable(Object parentObject, Field fieldToBeInjected)
   {
      MockedType mockedType = findInjectableMockedType(fieldToBeInjected);
      return mockedType == null ? null : getFieldValue(mockedType.field, parentObject);
   }

   private MockedType findInjectableMockedType(Field fieldToBeInjected)
   {
      Type declaredType = fieldToBeInjected.getGenericType();
      String fieldName = fieldToBeInjected.getName();
      boolean multipleFieldsOfSameTypeFound = false;
      MockedType found = null;

      for (MockedType mockedType : mockedTypes) {
         if (mockedType.injectable && mockedType.declaredType == declaredType) {
            if (found == null) {
               found = mockedType;
            }
            else {
               multipleFieldsOfSameTypeFound = true;

               if (fieldName.equals(mockedType.field.getName())) {
                  return mockedType;
               }
            }
         }
      }

      if (multipleFieldsOfSameTypeFound && !fieldName.equals(found.field.getName())) {
         return null;
      }

      return found;
   }
}
