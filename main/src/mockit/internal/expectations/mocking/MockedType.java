/*
 * JMockit Expectations & Verifications
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
import mockit.internal.state.*;

@SuppressWarnings({"ClassWithTooManyFields"})
final class MockedType
{
   final Field field;
   private final boolean fieldFromTestClass;
   private final int accessModifiers;
   private final Mocked mocked;
   final Capturing capturing;
   final Cascading cascading;
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
      capturing = field.getAnnotation(Capturing.class);
      cascading = field.getAnnotation(Cascading.class);
      nonStrict = field.isAnnotationPresent(NonStrict.class);
      declaredType = field.getGenericType();
      mockId = field.getName();
      registerCascadingIfSpecified();
   }

   private void registerCascadingIfSpecified()
   {
      if (cascading != null) {
         String mockedTypeDesc = getClassType().getName().replace('.', '/');
         TestRun.getExecutingTest().addCascadingType(mockedTypeDesc);
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
      declaredType = parameterType;
      mockId = "param" + paramIndex;
      registerCascadingIfSpecified();
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
      boolean mock = mocked != null || capturing != null || cascading != null || nonStrict;

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

   String[] getFilters()
   {
      if (mocked == null) {
         return null;
      }

      String[] filters = mocked.methods();

      if (filters.length == 0) {
         filters = mocked.value();
      }

      return filters;
   }

   boolean hasInverseFilters()
   {
      return mocked != null && mocked.inverse();
   }

   String getConstructorArgsMethod()
   {
      return mocked == null ? "" : mocked.constructorArgsMethod();
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
}
