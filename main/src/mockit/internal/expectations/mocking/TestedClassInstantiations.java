/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static mockit.internal.util.Utilities.*;

import mockit.internal.state.*;

public final class TestedClassInstantiations
{
   private final List<Field> testedFields;
   private final List<MockedType> injectableFields;

   TestedClassInstantiations(List<Field> testedFields, List<MockedType> injectableFields)
   {
      this.testedFields = testedFields;
      this.injectableFields = injectableFields;
   }

   public void assignNewInstancesToTestedFields(Object objectWithFields)
   {
      TestedObjectCreation testedObjectCreation = new TestedObjectCreation(objectWithFields);

      for (Field testedField : testedFields) {
         Object originalFieldValue = getFieldValue(testedField, objectWithFields);

         if (originalFieldValue == null) {
            testedObjectCreation.create(testedField);
         }
      }
   }

   private final class TestedObjectCreation
   {
      private final Object objectWithFields;
      private Class<?> testedClass;
      private Constructor<?> constructor;
      private Type declaredType;
      private final List<MockedType> injectableParameters;
      private final List<MockedType> injectables;

      TestedObjectCreation(Object objectWithFields)
      {
         this.objectWithFields = objectWithFields;

         ParameterTypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterTypeRedefinitions();

         if (paramTypeRedefinitions == null) {
            injectableParameters = Collections.emptyList();
            injectables = injectableFields;
         }
         else {
            injectableParameters = paramTypeRedefinitions.getInjectableParameters();
            injectables = new ArrayList<MockedType>(injectableFields);
            injectables.addAll(injectableParameters);
         }
      }

      void create(Field testedField)
      {
         testedClass = testedField.getType();
         new ConstructorSearch().findSingleConstructorAccordingToClassVisibilityAndAvailableInjectables();

         if (constructor != null) {
            Object testedObject = new ConstructorInjection().instantiate();
            new FieldInjection().injectIntoFieldsThatAreStillNull(testedClass, testedObject);
            setFieldValue(testedField, objectWithFields, testedObject);
         }
      }

      private MockedType findInjectableForConstructorParameter(int atPosition)
      {
         int currentPosition = 0;

         for (MockedType injectable : injectables) {
            if (injectable.declaredType == declaredType) {
               currentPosition++;

               if (currentPosition == atPosition) {
                  return injectable;
               }
            }
         }

         return null;
      }

      private final class ConstructorSearch
      {
         private int numberOfApplicableInjectables = -1;

         private void findSingleConstructorAccordingToClassVisibilityAndAvailableInjectables()
         {
            constructor = null;
            boolean publicClass = isPublic(testedClass.getModifiers());
            Constructor<?>[] constructors =
               publicClass ? testedClass.getConstructors() : testedClass.getDeclaredConstructors();

            for (Constructor<?> c : constructors) {
               if (publicClass || !isPrivate(c.getModifiers())) {
                  if (constructor == null || isBetterFitForAvailableInjectables(c)) {
                     constructor = c;
                  }
               }
            }
         }

         private boolean isBetterFitForAvailableInjectables(Constructor<?> candidate)
         {
            if (numberOfApplicableInjectables < 0) {
               numberOfApplicableInjectables = numberOfApplicableInjectables(constructor);
            }

            int n = numberOfApplicableInjectables(candidate);

            if (n > numberOfApplicableInjectables) {
               numberOfApplicableInjectables = n;
               return true;
            }

            return false;
         }

         private Type[] parameterTypes;

         private int numberOfApplicableInjectables(Constructor<?> c)
         {
            parameterTypes = c.getGenericParameterTypes();
            int n = parameterTypes.length;
            boolean varArgs = c.isVarArgs();

            if (varArgs) {
               n--;
            }

            int parametersWithApplicableInjectables = 0;

            for (int i = 0; i < n; i++) {
               declaredType = parameterTypes[i];
               int position = findRelativePositionForInjectable(i);

               if (hasApplicableInjectable(position)) {
                  parametersWithApplicableInjectables++;
               }
            }

            if (varArgs && hasInjectedValuesForVarargsParameter(n)) {
               parametersWithApplicableInjectables++;
            }

            return parametersWithApplicableInjectables;
         }

         private int findRelativePositionForInjectable(int currentParameterIndex)
         {
            int pos = 0;

            for (int i = 0; i <= currentParameterIndex; i++) {
               if (parameterTypes[i] == declaredType) {
                  pos++;
               }
            }

            return pos;
         }

         private boolean hasApplicableInjectable(int atPosition)
         {
            return findInjectableForConstructorParameter(atPosition) != null;
         }

         private boolean hasInjectedValuesForVarargsParameter(int varargsParameterIndex)
         {
            declaredType = ((Class<?>) parameterTypes[varargsParameterIndex]).getComponentType();
            int position = findRelativePositionForInjectable(varargsParameterIndex) + 1;
            return findInjectableForConstructorParameter(position) != null;
         }
      }

      private final class ConstructorInjection
      {
         private Type[] parameterTypes;

         private Object instantiate()
         {
            parameterTypes = constructor.getGenericParameterTypes();
            Object[] arguments = obtainInjectedConstructorArguments(constructor.isVarArgs());
            return invoke(constructor, arguments);
         }

         private Object[] obtainInjectedConstructorArguments(boolean varArgs)
         {
            int n = parameterTypes.length;
            Object[] parameterValues = new Object[n];

            if (varArgs) {
               n--;
            }
   
            for (int i = 0; i < n; i++) {
               declaredType = parameterTypes[i];
               int position = findRelativePositionForInjectable(i);
               parameterValues[i] = getArgumentValueToInject(position);
            }
   
            if (varArgs) {
               parameterValues[n] = obtainInjectedValuesForVarargsParameter(n);
            }
   
            return parameterValues;
         }

         private int findRelativePositionForInjectable(int currentParameterIndex)
         {
            int pos = 0;

            for (int i = 0; i <= currentParameterIndex; i++) {
               if (parameterTypes[i] == declaredType) {
                  pos++;
               }
            }

            return pos;
         }

         private Object obtainInjectedValuesForVarargsParameter(int varargsParameterIndex)
         {
            declaredType = ((Class<?>) parameterTypes[varargsParameterIndex]).getComponentType();
            int position = findRelativePositionForInjectable(varargsParameterIndex) + 1;
            List<Object> varargValues = new ArrayList<Object>();
            MockedType injectableField;

            while ((injectableField = findInjectableForConstructorParameter(position)) != null) {
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

         private Object getArgumentValueToInject(int atPosition)
         {
            MockedType injectable = findInjectableForConstructorParameter(atPosition);

            if (injectable == null) {
               throw new IllegalArgumentException(
                  "No injectable field or parameter" + missingInjectableDescription(atPosition));
            }

            return getArgumentValueToInject(injectable, atPosition);
         }

         private Object getArgumentValueToInject(MockedType injectableField, int atPosition)
         {
            Object argument = injectableField.getValueToInject(objectWithFields);

            if (argument == null) {
               throw new IllegalArgumentException(
                  "No injectable value available" + missingInjectableDescription(atPosition));
            }

            return argument;
         }

         private String missingInjectableDescription(int position)
         {
            String typeDesc = declaredType.toString();

            if (declaredType instanceof Class<?>) {
               Class<?> declaredClass = (Class<?>) declaredType;

               if (declaredClass.isArray()) {
                  typeDesc = mockit.external.asm4.Type.getType(declaredClass).getClassName();
               }
               else if (!declaredClass.isPrimitive()) {
                  typeDesc = '"' + declaredClass.getSimpleName() + '"';
               }
            }

            String posDesc =
               position == 1 ? "first" : position == 2 ? "second" : position == 3 ? "third" : position + "th";

            String constructorDescription = constructor.toGenericString();
            int lp = constructorDescription.indexOf('(');
            int rp = constructorDescription.indexOf(')');
            int mp = constructorDescription.substring(0, lp).lastIndexOf(' ');
            int sp = mp > 0 ? mp + 1 : 0;

            return
               " for " + posDesc + " parameter of type " + typeDesc + " in constructor " +
               constructorDescription.substring(sp, rp + 1).replace("java.lang.", "");
         }
      }

      private final class FieldInjection
      {
         private void injectIntoFieldsThatAreStillNull(Class<?> testedClass, Object tested)
         {
            Class<?> superClass = testedClass.getSuperclass();

            if (superClass != null && superClass.getProtectionDomain() == testedClass.getProtectionDomain()) {
               injectIntoFieldsThatAreStillNull(superClass, tested);
            }

            for (Field field : testedClass.getDeclaredFields()) {
               if (getFieldValue(field, tested) == null) {
                  Object value = getValueForFieldIfAvailable(field);

                  if (value != null) {
                     setFieldValue(field, tested, value);
                  }
               }
            }
         }

         private Object getValueForFieldIfAvailable(Field fieldToBeInjected)
         {
            declaredType = fieldToBeInjected.getGenericType();
            MockedType mockedType = findInjectableField(fieldToBeInjected.getName());
            
            if (mockedType == null) {
               mockedType = findInjectableParameter();
            }

            return mockedType == null ? null : mockedType.getValueToInject(objectWithFields);
         }

         private MockedType findInjectableField(String fieldName)
         {
            boolean multipleInjectableFieldsFound = false;
            MockedType found = null;

            for (MockedType injectable : injectableFields) {
               if (injectable.declaredType == declaredType) {
                  if (found == null) {
                     found = injectable;
                  }
                  else {
                     multipleInjectableFieldsFound = true;

                     if (fieldName.equals(injectable.field.getName())) {
                        return injectable;
                     }
                  }
               }
            }

            if (multipleInjectableFieldsFound && !fieldName.equals(found.field.getName())) {
               return null;
            }

            return found;
         }

         private MockedType findInjectableParameter()
         {
            for (MockedType injectable : injectableParameters) {
               if (injectable.declaredType == declaredType) {
                  return injectable;
               }
            }

            return null;
         }
      }
   }
}
