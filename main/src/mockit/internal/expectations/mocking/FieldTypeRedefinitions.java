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

import java.lang.reflect.*;
import static java.lang.reflect.Modifier.*;

import static mockit.external.asm.Opcodes.*;

public abstract class FieldTypeRedefinitions extends TypeRedefinitions
{
   private static final int FIELD_ACCESS_MASK = ACC_SYNTHETIC + ACC_STATIC;
   private static final String EXCLUDED_BASE_CLASSES =
      "mockit.Expectations mockit.integration.junit4.JMockitTest " +
      "mockit.integration.junit3.JMockitTestCase";

   protected Field field;
   protected MockedType typeMetadata;
   protected boolean finalField;

   protected FieldTypeRedefinitions(Object objectWithMockFields)
   {
      super(objectWithMockFields);
   }

   protected final void redefineFieldTypes(Class<?> classWithMockFields, boolean isTestClass)
   {
      Class<?> superClass = classWithMockFields.getSuperclass();

      if (
         superClass != null && superClass != Object.class &&
         !EXCLUDED_BASE_CLASSES.contains(superClass.getName())
      ) {
         redefineFieldTypes(superClass, isTestClass);
      }

      Field[] fields = classWithMockFields.getDeclaredFields();

      for (Field candidateField : fields) {
         int fieldModifiers = candidateField.getModifiers();

         if ((fieldModifiers & FIELD_ACCESS_MASK) == 0) {
            field = candidateField;
            redefineFieldType(isTestClass, fieldModifiers);
            field = null;
         }
      }
   }

   private void redefineFieldType(boolean fromTestClass, int modifiers)
   {
      typeMetadata = new MockedType(field, fromTestClass);

      if (typeMetadata.isMockField()) {
         finalField = isFinal(modifiers);

         TypeRedefinition typeRedefinition = redefineTypeForMockField();
         typeMetadata.mockingCfg = typeRedefinition.mockingCfg;
         typeMetadata.mockConstructorInfo = typeRedefinition.mockConstructorInfo;
         typesRedefined++;

         registerCaptureOfNewInstances();
      }

      typeMetadata = null;
   }

   protected abstract TypeRedefinition redefineTypeForMockField();

   @Override
   public final CaptureOfNewInstancesForFields getCaptureOfNewInstances()
   {
      return (CaptureOfNewInstancesForFields) captureOfNewInstances;
   }

   private void registerCaptureOfNewInstances()
   {
      if (typeMetadata.getMaxInstancesToCapture() <= 0) {
         return;
      }

      if (captureOfNewInstances == null) {
         captureOfNewInstances = new CaptureOfNewInstancesForFields();
      }

      getCaptureOfNewInstances().registerCaptureOfNewInstances(typeMetadata);
   }

   /**
    * Returns true iff the mock instance concrete class is not mocked in some test, ie it's a class
    * which only appears in the code under test.
    */
   public abstract boolean captureNewInstanceForApplicableMockField(Object mock);
}
