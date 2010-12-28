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
import mockit.internal.state.*;
import mockit.internal.util.*;

class CaptureOfNewInstances extends CaptureOfImplementations
{
   static final class Capture
   {
      final MockedType typeMetadata;
      private Object originalMockInstance;
      private final List<Object> instancesCaptured;

      private Capture(MockedType typeMetadata, Object originalMockInstance)
      {
         this.typeMetadata = typeMetadata;
         this.originalMockInstance = originalMockInstance;
         instancesCaptured = new ArrayList<Object>(4);
      }

      private boolean isInstanceAlreadyCaptured(Object mock)
      {
         return instancesCaptured.contains(mock);
      }

      private boolean captureInstance(Object fieldOwner, Object instance)
      {
         if (instancesCaptured.size() < typeMetadata.getMaxInstancesToCapture()) {
            if (fieldOwner != null && originalMockInstance == null) {
               originalMockInstance = Utilities.getFieldValue(typeMetadata.field, fieldOwner);
            }

            instancesCaptured.add(instance);
            return true;
         }

         return false;
      }

      void reset()
      {
         instancesCaptured.clear();
      }
   }

   final Map<Class<?>, List<Capture>> baseTypeToCaptures;
   private MockedType typeMetadata;
   Capture captureFound;

   CaptureOfNewInstances()
   {
      baseTypeToCaptures = new HashMap<Class<?>, List<Capture>>();
   }

   @Override
   public final ClassWriter createModifier(ClassLoader cl, ClassReader cr, String baseTypeDesc)
   {
      ExpectationsModifier modifier = new ExpectationsModifier(cl, cr, typeMetadata);
      modifier.setClassNameForCapturedInstanceMethods(baseTypeDesc);

      if (typeMetadata.injectable) {
         modifier.useDynamicMockingForInstanceMethods(typeMetadata);
      }

      return modifier;
   }

   final void registerCaptureOfNewInstances(MockedType typeMetadata, Object mockInstance)
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

      captures.add(new Capture(typeMetadata, mockInstance));
   }

   final boolean captureNewInstance(Object fieldOwner, Object mock)
   {
      captureFound = null;

      Class<?> mockedClass = mock.getClass();
      List<Capture> captures = baseTypeToCaptures.get(mockedClass);
      boolean constructorModifiedForCaptureOnly = captures == null;

      if (constructorModifiedForCaptureOnly) {
         captures = findCaptures(mockedClass);

         if (captures == null) {
            return false;
         }
      }

      for (Capture capture : captures) {
         if (capture.isInstanceAlreadyCaptured(mock)) {
            break;
         }
         else if (capture.captureInstance(fieldOwner, mock)) {
            captureFound = capture;
            break;
         }
      }

      if (typeMetadata.injectable) {
         if (captureFound != null) {
            TestRun.getExecutingTest().addCapturedInstanceForInjectableMock(captureFound.originalMockInstance, mock);
         }

         constructorModifiedForCaptureOnly = true;
      }

      return constructorModifiedForCaptureOnly;
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