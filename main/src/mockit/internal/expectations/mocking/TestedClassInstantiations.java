/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static mockit.internal.util.Utilities.*;

public final class TestedClassInstantiations
{
   private final List<Field> testedFields;
   private final List<MockedType> injectableFields;

   TestedClassInstantiations(TestedClassRedefinitions redefinitions)
   {
      testedFields = redefinitions.testedFields;
      injectableFields = redefinitions.injectableFields;
   }

   public void assignNewInstancesToTestedFields(Object objectWithFields)
   {
      for (Field testedField : testedFields) {
         Object originalFieldValue = getFieldValue(testedField, objectWithFields);

         if (originalFieldValue == null) {
            new TestedObjectCreation(objectWithFields, testedField).create();
         }
      }
   }

   private final class TestedObjectCreation
   {
      final Object objectWithFields;
      final Field testedField;
      final Class<?> testedClass;
      Constructor<?> constructor;
      Type[] parameterTypes;
      Type declaredType;

      TestedObjectCreation(Object objectWithFields, Field testedField)
      {
         this.objectWithFields = objectWithFields;
         this.testedField = testedField;
         testedClass = testedField.getType();
      }

      void create()
      {
         findSingleConstructorAccordingToClassVisibility();

         if (constructor != null) {
            Object testedObject = instantiateUsingConstructor();
            injectIntoFieldsThatAreStillNull(testedClass, testedObject);
            setFieldValue(testedField, objectWithFields, testedObject);
         }
      }

      private void findSingleConstructorAccordingToClassVisibility()
      {
         Constructor<?>[] constructors;

         if (isPublic(testedClass.getModifiers())) {
            constructors = testedClass.getConstructors();

            if (constructors.length == 1) {
               constructor = constructors[0];
               return;
            }
         }

         constructors = testedClass.getDeclaredConstructors();

         for (Constructor<?> c : constructors) {
            if (c.getModifiers() == 0) {
               if (constructor == null) {
                  constructor = c;
               }
               else {
                  constructor = null;
                  return;
               }
            }
         }
      }

      private Object instantiateUsingConstructor()
      {
         parameterTypes = constructor.getGenericParameterTypes();
         Object[] arguments = obtainInjectedConstructorArguments();
         return invoke(constructor, arguments);
      }

      private Object[] obtainInjectedConstructorArguments()
      {
         int n = parameterTypes.length;
         Object[] parameterValues = new Object[n];

         if (constructor.isVarArgs()) {
            n--;
         }

         for (int i = 0; i < n; i++) {
            declaredType = parameterTypes[i];
            int position = findRelativePositionForInjectableField(i);
            parameterValues[i] = getArgumentValueToInject(position);
         }

         if (constructor.isVarArgs()) {
            parameterValues[n] = obtainInjectedValuesForVarargsParameter(n);
         }

         return parameterValues;
      }

      private Object obtainInjectedValuesForVarargsParameter(int varargsParameterIndex)
      {
         declaredType = ((Class<?>) parameterTypes[varargsParameterIndex]).getComponentType();
         int position = findRelativePositionForInjectableField(varargsParameterIndex) + 1;
         List<Object> varargValues = new ArrayList<Object>();
         MockedType injectableField;

         while ((injectableField = findInjectableFieldForConstructorParameter(position)) != null) {
            Object value = getArgumentValueToInject(injectableField, position);
            varargValues.add(value);
            position++;
         }

         int elementCount = varargValues.size();
         Object varargArray = Array.newInstance((Class<?>) declaredType, elementCount);

         for (int i = 0; i < elementCount; i++) {
            Array.set(varargArray, i, varargValues.get(i));
         }

         return varargArray;
      }

      private int findRelativePositionForInjectableField(int currentParameterIndex)
      {
         int pos = 0;

         for (int i = 0; i <= currentParameterIndex; i++) {
            if (parameterTypes[i] == declaredType) {
               pos++;
            }
         }

         return pos;
      }

      private Object getArgumentValueToInject(int atPosition)
      {
         MockedType injectableField = findInjectableFieldForConstructorParameter(atPosition);

         if (injectableField == null) {
            throw new IllegalArgumentException("No injectable field" + missingFieldDescription(atPosition));
         }

         return getArgumentValueToInject(injectableField, atPosition);
      }

      private Object getArgumentValueToInject(MockedType injectableField, int atPosition)
      {
         Object argument = getFieldValue(injectableField.field, objectWithFields);

         if (argument == null) {
            throw new IllegalArgumentException("No injectable value available" + missingFieldDescription(atPosition));
         }

         return argument;
      }

      private MockedType findInjectableFieldForConstructorParameter(int atPosition)
      {
         int currentPosition = 0;

         for (MockedType injectableField : injectableFields) {
            if (injectableField.declaredType == declaredType) {
               currentPosition++;

               if (currentPosition == atPosition) {
                  return injectableField;
               }
            }
         }

         return null;
      }

      private String missingFieldDescription(int position)
      {
         String typeDesc = declaredType.toString();

         if (declaredType instanceof Class<?>) {
            Class<?> declaredClass = (Class<?>) declaredType;

            if (declaredClass.isArray()) {
               typeDesc = "type " + mockit.external.asm4.Type.getType(declaredClass).getClassName();
            }
            else if (declaredClass.isPrimitive()) {
               typeDesc = "type " + typeDesc;
            }
         }

         return " of " + typeDesc + " for " + position + "-th corresponding constructor parameter in " + testedClass;
      }

      private void injectIntoFieldsThatAreStillNull(Class<?> testedClass, Object tested)
      {
         Class<?> superClass = testedClass.getSuperclass();

         if (superClass != null && superClass.getProtectionDomain() == testedClass.getProtectionDomain()) {
            injectIntoFieldsThatAreStillNull(superClass, tested);
         }

         for (Field field : testedClass.getDeclaredFields()) {
            if (getFieldValue(field, tested) == null) {
               Object value = getFieldValueIfAvailable(field);
               setFieldValue(field, tested, value);
            }
         }
      }

      private Object getFieldValueIfAvailable(Field fieldToBeInjected)
      {
         MockedType mockedType = findInjectableMockedType(fieldToBeInjected);
         return mockedType == null ? null : getFieldValue(mockedType.field, objectWithFields);
      }

      private MockedType findInjectableMockedType(Field fieldToBeInjected)
      {
         declaredType = fieldToBeInjected.getGenericType();
         String fieldName = fieldToBeInjected.getName();
         boolean multipleFieldsOfSameTypeFound = false;
         MockedType found = null;

         for (MockedType injectableField : injectableFields) {
            if (injectableField.declaredType == declaredType) {
               if (found == null) {
                  found = injectableField;
               }
               else {
                  multipleFieldsOfSameTypeFound = true;

                  if (fieldName.equals(injectableField.field.getName())) {
                     return injectableField;
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
}
