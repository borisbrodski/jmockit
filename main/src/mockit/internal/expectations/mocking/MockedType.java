/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.annotation.*;
import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;
import static mockit.internal.util.Utilities.getAnnotation;

import mockit.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;

@SuppressWarnings({"ClassWithTooManyFields", "EqualsAndHashcode"})
public final class MockedType
{
   @SuppressWarnings({"UnusedDeclaration"})
   @Mocked private static final Object DUMMY = null;
   private static final int DUMMY_HASHCODE;

   static
   {
      int h = 0;

      try {
         h = MockedType.class.getDeclaredField("DUMMY").getAnnotation(Mocked.class).hashCode();
      }
      catch (NoSuchFieldException ignore) {}

      DUMMY_HASHCODE = h;
   }

   public final Field field;
   public final boolean fieldFromTestClass;
   private final int accessModifiers;
   private final Mocked mocked;
   public final Capturing capturing;
   final Cascading cascading;
   public final boolean nonStrict;
   public final boolean injectable;
   final Type declaredType;
   final String mockId;
   MockingConfiguration mockingCfg;

   MockedType(Field field, boolean fromTestClass)
   {
      this.field = field;
      fieldFromTestClass = fromTestClass;
      accessModifiers = field.getModifiers();
      mocked = field.getAnnotation(Mocked.class);
      capturing = field.getAnnotation(Capturing.class);
      cascading = field.getAnnotation(Cascading.class);
      nonStrict = field.isAnnotationPresent(NonStrict.class);
      injectable = field.isAnnotationPresent(Injectable.class);
      declaredType = field.getGenericType();
      mockId = field.getName();
      registerCascadingIfSpecified();
   }

   private void registerCascadingIfSpecified()
   {
      if (cascading != null) {
         String mockedTypeDesc = getClassType().getName().replace('.', '/');
         TestRun.getExecutingTest().addCascadingType(mockedTypeDesc, fieldFromTestClass);
      }
   }

   MockedType(int paramIndex, Type parameterType, Annotation[] annotationsOnParameter)
   {
      field = null;
      fieldFromTestClass = false;
      accessModifiers = 0;
      mocked = getAnnotation(annotationsOnParameter, Mocked.class);
      capturing = getAnnotation(annotationsOnParameter, Capturing.class);
      cascading = getAnnotation(annotationsOnParameter, Cascading.class);
      nonStrict = getAnnotation(annotationsOnParameter, NonStrict.class) != null;
      injectable = getAnnotation(annotationsOnParameter, Injectable.class) != null;
      declaredType = parameterType;
      mockId = "param" + paramIndex;
      registerCascadingIfSpecified();
   }

   MockedType(Class<?> cascadedType)
   {
      field = null;
      fieldFromTestClass = false;
      accessModifiers = 0;
      mocked = null;
      capturing = null;
      cascading = null;
      nonStrict = true;
      injectable = true;
      declaredType = cascadedType;
      mockId = "cascaded_" + cascadedType.getName();
   }

   public Class<?> getClassType()
   {
      if (declaredType instanceof Class) {
         return (Class<?>) declaredType;
      }

      if (declaredType instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType) declaredType;
         return (Class<?>) parameterizedType.getRawType();
      }

      return null;
   }

   boolean isMockField()
   {
      boolean mock = mocked != null || capturing != null || cascading != null || nonStrict || injectable;

      return (mock || !fieldFromTestClass && !isPrivate(accessModifiers)) && isMockableType();
   }

   private boolean isMockableType()
   {
      if (declaredType instanceof Class) {
         Class<?> classType = (Class<?>) declaredType;
         return !classType.isPrimitive() && !classType.isArray() && classType != Integer.class;
      }

      return true;
   }

   boolean isMockParameter()
   {
      return isMockableType();
   }

   boolean isFinalFieldOrParameter()
   {
      return field == null || isFinal(accessModifiers);
   }

   void buildMockingConfiguration()
   {
      if (mocked == null) {
         return;
      }

      String[] filters = getFilters();

      if (filters.length > 0) {
         mockingCfg = new MockingConfiguration(filters, !mocked.inverse());
      }
   }

   private String[] getFilters()
   {
      String[] filters = mocked.methods();

      if (filters.length == 0) {
         filters = mocked.value();
      }

      return filters;
   }

   boolean isClassInitializationToBeStubbedOut()
   {
      return mocked != null && mocked.stubOutClassInitialization();
   }

   int getMaxInstancesToCapture()
   {
      if (capturing != null) {
         return capturing.maxInstances();
      }
      else if (mocked != null) {
         return mocked.capture();
      }

      return 0;
   }

   String getRealClassName()
   {
      return mocked == null ? "" : mocked.realClassName();
   }

   @Override
   public int hashCode()
   {
      int result = declaredType.hashCode();

      if (isFinal(accessModifiers)) {
         result *= 31;
      }

      if (injectable) {
         result *= 37;
      }

      if (mocked != null) {
         int h = mocked.hashCode();

         if (h != DUMMY_HASHCODE) {
            result = 31 * result + h;
         }
      }

      return result;
   }
}
