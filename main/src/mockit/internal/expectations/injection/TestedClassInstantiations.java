/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.injection;

import java.lang.reflect.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static mockit.internal.util.Utilities.*;

import mockit.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

public final class TestedClassInstantiations
{
   private final List<Field> testedFields;
   private final List<MockedType> injectableFields;
   private List<MockedType> injectables;
   private final List<MockedType> consumedInjectables;
   private Object testClassInstance;

   public TestedClassInstantiations()
   {
      testedFields = new LinkedList<Field>();
      injectableFields = new ArrayList<MockedType>();
      consumedInjectables = new ArrayList<MockedType>();
   }

   public boolean findTestedAndInjectableFields(Class<?> testClass)
   {
      new ParameterNameExtractor(true).extractNames(testClass);

      Field[] fieldsInTestClass = testClass.getDeclaredFields();

      for (Field field : fieldsInTestClass) {
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

   public void assignNewInstancesToTestedFields(Object testClassInstance)
   {
      this.testClassInstance = testClassInstance;

      buildListsOfInjectables();

      TestedObjectCreation testedObjectCreation = null;

      for (Field testedField : testedFields) {
         Object testedObject = getFieldValue(testedField, testClassInstance);

         if (testedObject == null && !isFinal(testedField.getModifiers())) {
            if (testedObjectCreation == null) {
               testedObjectCreation = new TestedObjectCreation();
            }

            testedObject = testedObjectCreation.create(testedField);
            setFieldValue(testedField, testClassInstance, testedObject);
         }

         if (testedObject != null) {
            new FieldInjection().injectIntoFieldsThatAreStillNull(testedObject);
         }

         consumedInjectables.clear();
      }
   }

   private void buildListsOfInjectables()
   {
      ParameterTypeRedefinitions paramTypeRedefs = TestRun.getExecutingTest().getParameterTypeRedefinitions();

      if (paramTypeRedefs == null) {
         injectables = injectableFields;
      }
      else {
         injectables = new ArrayList<MockedType>(injectableFields);
         injectables.addAll(paramTypeRedefs.getInjectableParameters());
      }
   }

   Object getValueToInject(MockedType injectable)
   {
      if (consumedInjectables.contains(injectable)) {
         return null;
      }

      Object value = injectable.getValueToInject(testClassInstance);

      if (value != null) {
         consumedInjectables.add(injectable);
      }

      return value;
   }

   private final class TestedObjectCreation
   {
      private Class<?> testedClass;
      private Constructor<?> constructor;
      private List<MockedType> injectablesForConstructor;
      private Type[] parameterTypes;
      private Type constructorParameterType;

      Object create(Field testedField)
      {
         testedClass = testedField.getType();

         new ConstructorSearch().findSingleConstructorAccordingToClassVisibilityAndAvailableInjectables();

         if (constructor == null) {
            throw new IllegalArgumentException(
               "No constructor in " + testedClass + " that can be satisfied by available injectables");
         }

         return new ConstructorInjection().instantiate();
      }

      MockedType findInjectableForConstructorParameter(String targetParameterName)
      {
         boolean multipleInjectablesFound = false;
         MockedType found = null;

         for (MockedType injectable : injectables) {
            if (injectable.declaredType == constructorParameterType) {
               if (found == null) {
                  found = injectable;
               }
               else {
                  multipleInjectablesFound = true;

                  if (targetParameterName.equals(injectable.mockId)) {
                     return injectable;
                  }
               }
            }
         }

         if (multipleInjectablesFound && !targetParameterName.equals(found.mockId)) {
            return null;
         }

         return found;
      }

      MockedType findNextInjectableForVarargsParameter()
      {
         for (MockedType injectable : injectables) {
            if (injectable.declaredType == constructorParameterType && !consumedInjectables.contains(injectable)) {
               return injectable;
            }
         }

         return null;
      }

      private final class ConstructorSearch
      {
         private final String testedClassDesc;

         ConstructorSearch()
         {
            testedClassDesc = new ParameterNameExtractor(false).extractNames(testedClass);
            injectablesForConstructor = new ArrayList<MockedType>();
         }

         void findSingleConstructorAccordingToClassVisibilityAndAvailableInjectables()
         {
            constructor = null;
            boolean publicClass = isPublic(testedClass.getModifiers());
            Constructor<?>[] constructors =
               publicClass ? testedClass.getConstructors() : testedClass.getDeclaredConstructors();

            for (Constructor<?> c : constructors) {
               if (publicClass || !isPrivate(c.getModifiers())) {
                  List<MockedType> injectablesFound = findAvailableInjectablesForConstructor(c);

                  if (injectablesFound != null && injectablesFound.size() >= injectablesForConstructor.size()) {
                     injectablesForConstructor = injectablesFound;
                     constructor = c;
                  }
               }
            }
         }

         private List<MockedType> findAvailableInjectablesForConstructor(Constructor<?> candidate)
         {
            parameterTypes = candidate.getGenericParameterTypes();
            int n = parameterTypes.length;
            List<MockedType> injectablesFound = new ArrayList<MockedType>(n);
            boolean varArgs = candidate.isVarArgs();

            if (varArgs) {
               n--;
            }

            String constructorDesc = "<init>" + mockit.external.asm4.Type.getConstructorDescriptor(candidate);

            for (int i = 0; i < n; i++) {
               constructorParameterType = parameterTypes[i];
               String parameterName = ParameterNames.getName(testedClassDesc, constructorDesc, i);
               MockedType injectable = findInjectableForConstructorParameter(parameterName);

               if (injectable == null) {
                  return null;
               }

               injectablesFound.add(injectable);
            }

            if (varArgs) {
               MockedType injectable = hasInjectedValuesForVarargsParameter(n);

               if (injectable != null) {
                  injectablesFound.add(injectable);
               }
            }

            return injectablesFound;
         }

         private MockedType hasInjectedValuesForVarargsParameter(int varargsParameterIndex)
         {
            constructorParameterType = ((Class<?>) parameterTypes[varargsParameterIndex]).getComponentType();
            return findNextInjectableForVarargsParameter();
         }
      }

      private final class ConstructorInjection
      {
         Object instantiate()
         {
            parameterTypes = constructor.getGenericParameterTypes();
            int n = parameterTypes.length;
            Object[] arguments = new Object[n];
            boolean varArgs = constructor.isVarArgs();

            if (varArgs) {
               n--;
            }

            for (int i = 0; i < n; i++) {
               MockedType injectable = injectablesForConstructor.get(i);
               arguments[i] = getArgumentValueToInject(injectable);
            }

            if (varArgs) {
               arguments[n] = obtainInjectedVarargsArray(n);
            }

            return invoke(constructor, arguments);
         }

         private Object obtainInjectedVarargsArray(int varargsIndex)
         {
            constructorParameterType = ((Class<?>) parameterTypes[varargsIndex]).getComponentType();
            List<Object> varargValues = new ArrayList<Object>();
            MockedType injectable;

            while ((injectable = findNextInjectableForVarargsParameter()) != null) {
               Object value = getValueToInject(injectable);

               if (value != null) {
                  varargValues.add(value);
               }
            }

            int elementCount = varargValues.size();
            Object varargArray = Array.newInstance((Class<?>) constructorParameterType, elementCount);

            for (int i = 0; i < elementCount; i++) {
               Array.set(varargArray, i, varargValues.get(i));
            }

            return varargArray;
         }

         private Object getArgumentValueToInject(MockedType injectable)
         {
            Object argument = getValueToInject(injectable);

            if (argument == null) {
               throw new IllegalArgumentException(
                  "No injectable value available" + missingInjectableDescription(injectable.mockId));
            }

            return argument;
         }

         private String missingInjectableDescription(String name)
         {
            String classDesc = mockit.external.asm4.Type.getInternalName(constructor.getDeclaringClass());
            String constructorDesc = "<init>" + mockit.external.asm4.Type.getConstructorDescriptor(constructor);
            String constructorDescription = new MethodFormatter(classDesc, constructorDesc).toString();

            return
               " for parameter \"" + name + "\" in constructor " +
               constructorDescription.replace("java.lang.", "");
         }
      }
   }

   private final class FieldInjection
   {
      private Type injectedFieldType;

      void injectIntoFieldsThatAreStillNull(Object testedObject)
      {
         injectIntoFieldsThatAreStillNull(testedObject.getClass(), testedObject);
      }

      private void injectIntoFieldsThatAreStillNull(Class<?> testedClass, Object testedObject)
      {
         Class<?> superClass = testedClass.getSuperclass();

         if (superClass != null && superClass.getProtectionDomain() == testedClass.getProtectionDomain()) {
            injectIntoFieldsThatAreStillNull(superClass, testedObject);
         }

         for (Field field : testedClass.getDeclaredFields()) {
            if (isUninitialized(field, testedObject) && !isFinal(field.getModifiers())) {
               Object value = getValueForFieldIfAvailable(field);

               if (value != null) {
                  setFieldValue(field, testedObject, value);
               }
            }
         }
      }

      private boolean isUninitialized(Field field, Object fieldOwner)
      {
         Object fieldValue = getFieldValue(field, fieldOwner);

         if (fieldValue == null) {
            return true;
         }

         Class<?> fieldType = field.getType();

         if (!fieldType.isPrimitive()) {
            return false;
         }

         Object defaultValue = DefaultValues.defaultValueForPrimitiveType(fieldType);

         return fieldValue.equals(defaultValue);
      }

      private Object getValueForFieldIfAvailable(Field fieldToBeInjected)
      {
         injectedFieldType = fieldToBeInjected.getGenericType();

         String targetFieldName = fieldToBeInjected.getName();
         MockedType mockedType = findInjectable(targetFieldName);

         return mockedType == null ? null : getValueToInject(mockedType);
      }

      private MockedType findInjectable(String targetFieldName)
      {
         boolean multipleInjectablesFound = false;
         MockedType found = null;

         for (MockedType injectable : injectables) {
            if (injectable.declaredType == injectedFieldType) {
               if (found == null) {
                  found = injectable;
               }
               else {
                  multipleInjectablesFound = true;

                  if (targetFieldName.equals(injectable.mockId)) {
                     return injectable;
                  }
               }
            }
         }

         if (multipleInjectablesFound && !targetFieldName.equals(found.mockId)) {
            return null;
         }

         return found;
      }
   }
}
