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

import java.util.*;

import mockit.external.asm.*;
import mockit.internal.capturing.*;
import mockit.internal.util.*;

class CaptureOfNewInstances extends CaptureOfImplementations
{
   static class Capture
   {
      final MockedType typeMetadata;
      int instancesCaptured;

      private Capture(MockedType typeMetadata) { this.typeMetadata = typeMetadata; }

      private boolean isInstanceAlreadyCaptured(Object fieldOwner, Object mock)
      {
         return mock == Utilities.getFieldValue(typeMetadata.field, fieldOwner);
      }

      private boolean captureInstance()
      {
         if (instancesCaptured < typeMetadata.getMaxInstancesToCapture()) {
            instancesCaptured++;
            return true;
         }

         return false;
      }
   }

   final Map<Class<?>, List<Capture>> baseTypeToCaptures;
   Capture captureFound;

   private MockedType typeMetadata;

   CaptureOfNewInstances()
   {
      baseTypeToCaptures = new HashMap<Class<?>, List<Capture>>();
   }

   @Override
   public final ClassWriter createModifier(ClassLoader cl, ClassReader cr, String baseTypeDesc)
   {
      ExpectationsModifier modifier = new ExpectationsModifier(cl, cr, typeMetadata.mockingCfg);
      modifier.setStubOutClassInitialization(typeMetadata);
      modifier.setClassNameForCapturedInstanceMethods(baseTypeDesc);
      return modifier;
   }

   final void registerCaptureOfNewInstances(MockedType typeMetadata)
   {
      this.typeMetadata = typeMetadata;

      Class<?> baseType = typeMetadata.getClassType();

      if (!typeMetadata.isFinalFieldOrParameter()) {
         makeSureAllSubtypesAreModified(baseType, typeMetadata.capturing);
      }

      List<Capture> captures = baseTypeToCaptures.get(baseType);

      if (captures == null) {
         captures = new ArrayList<Capture>();
         baseTypeToCaptures.put(baseType, captures);
      }

      captures.add(new Capture(typeMetadata));
   }

   final boolean captureNewInstance(Object fieldOwner, Object mock)
   {
      captureFound = null;

      Class<?> mockedClass = mock.getClass();
      List<Capture> captures = baseTypeToCaptures.get(mockedClass);
      boolean implementationClassModifiedForCaptureOnly = captures == null;

      if (implementationClassModifiedForCaptureOnly) {
         captures = findCaptures(mockedClass);

         if (captures == null) {
            return false;
         }
      }

      for (Capture capture : captures) {
         if (fieldOwner != null && capture.isInstanceAlreadyCaptured(fieldOwner, mock)) {
            break;
         }
         else if (capture.captureInstance()) {
            captureFound = capture;
            break;
         }
      }

      return implementationClassModifiedForCaptureOnly;
   }

   private List<Capture> findCaptures(Class<?> mockedClass)
   {
      Class<?>[] interfaces = mockedClass.getInterfaces();

      for (Class<?> anInterface : interfaces) {
         List<Capture> found = baseTypeToCaptures.get(anInterface);

         if (found != null) {
            return found;
         }
      }

      Class<?> superclass = mockedClass.getSuperclass();

      if (superclass == Object.class) {
         return null;
      }

      List<Capture> found = baseTypeToCaptures.get(superclass);

      return found != null ? found : findCaptures(superclass);
   }

   @Override
   public final void cleanUp()
   {
      super.cleanUp();
      baseTypeToCaptures.clear();
   }
}