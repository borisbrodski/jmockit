/*
 * JMockit Expectations
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
import java.util.*;

import mockit.internal.util.*;

final class CaptureOfNewInstancesForFields extends CaptureOfNewInstances
{
   CaptureOfNewInstancesForFields() {}

   boolean captureNewInstanceForApplicableMockField(Object fieldOwner, Object mock)
   {
      boolean constructorModifiedForCaptureOnly = captureNewInstance(fieldOwner, mock);

      if (captureFound != null) {
         Field mockField = captureFound.typeMetadata.field;
         Utilities.setFieldValue(mockField, fieldOwner, mock);
      }

      return constructorModifiedForCaptureOnly;
   }

   void resetCaptureCount(Field mockField)
   {
      for (List<Capture> fieldsWithCapture : baseTypeToCaptures.values()) {
         resetCaptureCount(mockField, fieldsWithCapture);
      }
   }

   private void resetCaptureCount(Field mockField, List<Capture> fieldsWithCapture)
   {
      for (Capture fieldWithCapture : fieldsWithCapture) {
         if (fieldWithCapture.typeMetadata.field == mockField) {
            fieldWithCapture.reset();
         }
      }
   }
}
