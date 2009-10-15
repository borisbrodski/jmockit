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

import java.lang.reflect.*;
import java.util.*;

import mockit.internal.capturing.*;
import mockit.internal.util.*;
import mockit.internal.filtering.*;
import mockit.internal.state.*;

import org.objectweb.asm2.*;

final class CaptureOfNewInstancesForFields extends CaptureOfNewInstances
{
   private static final class FieldWithCapture
   {
      final Field mockField;
      final int instancesToCapture;
      int instancesCaptured;

      FieldWithCapture(Field mockField, int instancesToCapture)
      {
         this.mockField = mockField;
         this.instancesToCapture = instancesToCapture;
      }

      boolean reassignInstance(Object fieldOwner, Object newInstance)
      {
         if (instancesCaptured < instancesToCapture) {
            Object previousInstance = Utilities.getFieldValue(mockField, fieldOwner);
            Utilities.setFieldValue(mockField, fieldOwner, newInstance);
            TestRun.getExecutingTest().substituteMock(previousInstance, newInstance);
            instancesCaptured++;
            return true;
         }

         return false;
      }
   }

   private final Map<Class<?>, List<FieldWithCapture>> fieldTypeToFields =
      new HashMap<Class<?>, List<FieldWithCapture>>();
   private MockingConfiguration mockingCfg;
   private MockConstructorInfo mockConstructorInfo;

   CaptureOfNewInstancesForFields() {}

   public ClassWriter createModifier(ClassLoader classLoader, ClassReader cr)
   {
      ExpectationsModifier modifier =
         new ExpectationsModifier(classLoader, cr, mockingCfg, mockConstructorInfo);

      modifier.setClassNameForInstanceMethods(baseTypeDesc);

      return modifier;
   }

   @SuppressWarnings({"ParameterHidesMemberVariable"})
   void registerCaptureOfNewInstances(
      Field mockField, MockedType typeMetadata,
      MockingConfiguration mockingCfg, MockConstructorInfo mockConstructorInfo)
   {
      this.mockingCfg = mockingCfg;
      this.mockConstructorInfo = mockConstructorInfo;

      Class<?> fieldType = typeMetadata.getClassType();

      if (!typeMetadata.isFinalFieldOrParameter()) {
         makeSureAllSubtypesAreModified(fieldType, typeMetadata.capturing);
      }

      List<FieldWithCapture> fieldsWithCaptureForType = fieldTypeToFields.get(fieldType);

      if (fieldsWithCaptureForType == null) {
         fieldsWithCaptureForType = new ArrayList<FieldWithCapture>();
         fieldTypeToFields.put(fieldType, fieldsWithCaptureForType);
      }

      FieldWithCapture capture =
         new FieldWithCapture(mockField, typeMetadata.getMaxInstancesToCapture());
      fieldsWithCaptureForType.add(capture);
   }

   @Override
   public void cleanUp()
   {
      super.cleanUp();
      fieldTypeToFields.clear();
   }

   boolean captureNewInstanceForApplicableMockField(Object fieldOwner, Object mock)
   {
      Class<?> mockedClass = mock.getClass();
      List<FieldWithCapture> fieldsWithCaptureForType = fieldTypeToFields.get(mockedClass);
      boolean capturedImplementationClassNotMocked = false;

      if (fieldsWithCaptureForType == null) {
         fieldsWithCaptureForType = findFieldsWithCaptureForType(mockedClass);

         if (fieldsWithCaptureForType == null) {
            return false;
         }

         capturedImplementationClassNotMocked = true;
      }

      for (FieldWithCapture fieldWithCapture : fieldsWithCaptureForType) {
         if (fieldWithCapture.reassignInstance(fieldOwner, mock)) {
            break;
         }
      }

      return capturedImplementationClassNotMocked;
   }

   private List<FieldWithCapture> findFieldsWithCaptureForType(Class<?> mockedClass)
   {
      Class<?>[] interfaces = mockedClass.getInterfaces();

      for (Class<?> anInterface : interfaces) {
         List<FieldWithCapture> found = fieldTypeToFields.get(anInterface);

         if (found != null) {
            return found;
         }
      }

      Class<?> superclass = mockedClass.getSuperclass();

      if (superclass == Object.class) {
         return null;
      }

      List<FieldWithCapture> found = fieldTypeToFields.get(superclass);

      return found != null ? found : findFieldsWithCaptureForType(superclass);
   }

   void resetCaptureCount(Field mockField)
   {
      for (List<FieldWithCapture> fieldsWithCapture : fieldTypeToFields.values()) {
         resetCaptureCount(mockField, fieldsWithCapture);
      }
   }

   private void resetCaptureCount(Field mockField, List<FieldWithCapture> fieldsWithCapture)
   {
      for (FieldWithCapture fieldWithCapture : fieldsWithCapture) {
         if (fieldWithCapture.mockField == mockField) {
            fieldWithCapture.instancesCaptured = 0;
         }
      }
   }
}
