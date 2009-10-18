/*
 * JMockit Expectations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
package mockit.internal.expectations.mocking;

import java.lang.annotation.*;
import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;

import mockit.*;
import mockit.internal.filtering.*;

@SuppressWarnings({"deprecation"})
final class MockedType
{
   final Field field;
   private final boolean fieldFromTestClass;
   private final int accessModifiers;
   private final Mocked mocked;
   private final MockField mockField;
   final Capturing capturing;
   final boolean nonStrict;
   final Type declaredType;
   final String mockId;
   MockingConfiguration mockingCfg;
   MockConstructorInfo mockConstructorInfo;

   MockedType(Field field, boolean fromTestClass)
   {
      this.field = field;
      fieldFromTestClass = fromTestClass;
      accessModifiers = field.getModifiers();
      mocked = field.getAnnotation(Mocked.class);
      mockField = mocked == null ? field.getAnnotation(MockField.class) : null;
      capturing = field.getAnnotation(Capturing.class);
      nonStrict = field.isAnnotationPresent(NonStrict.class);
      declaredType = field.getGenericType();
      mockId = field.getName();
   }

   MockedType(int paramIndex, Type parameterType, Annotation[] annotationsOnParameter)
   {
      field = null;
      fieldFromTestClass = false;
      accessModifiers = 0;
      mocked = getAnnotation(annotationsOnParameter, Mocked.class);
      mockField = null;
      capturing = getAnnotation(annotationsOnParameter, Capturing.class);
      nonStrict = getAnnotation(annotationsOnParameter, NonStrict.class) != null;
      declaredType = parameterType;
      mockId = "param" + paramIndex;
   }

   private <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotation)
   {
      for (Annotation paramAnnotation : annotations) {
         if (paramAnnotation.annotationType() == annotation) {
            //noinspection unchecked
            return (A) paramAnnotation;
         }
      }

      return null;
   }

   Class<?> getClassType()
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
      boolean mock = mocked != null || mockField != null || capturing != null || nonStrict;

      if (!mock && (fieldFromTestClass || isPrivate(accessModifiers))) {
         return false;
      }

      if (declaredType instanceof Class) {
         Class<?> classType = (Class<?>) declaredType;
         return !classType.isPrimitive() && !classType.isArray();
      }

      return true;
   }

   boolean isFinalFieldOrParameter()
   {
      return isFinal(accessModifiers);
   }

   String[] getFilters()
   {
      String[] filters = null;

      if (mocked != null) {
         filters = mocked.methods();

         if (filters.length == 0) {
            filters = mocked.value();
         }
      }
      else if (mockField != null) {
         filters = mockField.methods();

         if (filters.length == 0) {
            filters = mockField.value();
         }
      }

      return filters;
   }

   boolean hasInverseFilters()
   {
      if (mocked != null) {
         return mocked.inverse();
      }
      else if (mockField != null) {
         return mockField.inverse();
      }

      return false;
   }

   String getConstructorArgsMethod()
   {
      if (mocked != null) {
         return mocked.constructorArgsMethod();
      }
      else if (mockField != null) {
         return mockField.constructorArgsMethod();
      }

      return "";
   }

   int getMaxInstancesToCapture()
   {
      if (capturing != null) {
         return capturing.maxInstances();
      }
      else if (mocked != null) {
         return mocked.capture();
      }
      else if (mockField != null) {
         return mockField.capture();
      }

      return 0;
   }

   String getRealClassName()
   {
      if (mocked != null) {
         return mocked.realClassName();
      }
      else if (mockField != null) {
         return mockField.realClassName();
      }

      return "";
   }
}
