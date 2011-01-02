/*
 * JMockit
 * Copyright (c) 2006-2011 Rogério Liesenfeld
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
package mockit.internal.capturing;

import java.util.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;

public abstract class CaptureOfImplementations
{
   private final List<CaptureTransformer> captureTransformers;

   protected CaptureOfImplementations()
   {
      captureTransformers = new ArrayList<CaptureTransformer>();
   }

   protected abstract ClassWriter createModifier(ClassLoader cl, ClassReader cr, String capturedTypeDesc);

   public final void makeSureAllSubtypesAreModified(Capturing capturing)
   {
      CapturedType captureMetadata = new CapturedType(null, capturing);
      makeSureAllSubtypesAreModified(captureMetadata, null, true);
   }

   public final void makeSureAllSubtypesAreModified(MockedType typeMetadata)
   {
      Class<?> baseType = typeMetadata.getClassType();
      String baseTypeDesc = baseType == null ? null : baseType.getName().replace('.', '/');
      CapturedType captureMetadata = new CapturedType(baseType, typeMetadata.capturing);
      makeSureAllSubtypesAreModified(captureMetadata, baseTypeDesc, typeMetadata.fieldFromTestClass);
   }

   private void makeSureAllSubtypesAreModified(CapturedType captureMetadata, String baseTypeDesc, boolean forTestClass)
   {
      Class<?>[] classesLoaded = Startup.instrumentation().getAllLoadedClasses();

      for (Class<?> aClass : classesLoaded) {
         if (captureMetadata.isToBeCaptured(aClass)) {
            redefineClass(aClass, baseTypeDesc);
         }
      }

      createCaptureTransformer(captureMetadata, forTestClass);
   }

   private void redefineClass(Class<?> realClass, String baseTypeDesc)
   {
      if (!TestRun.mockFixture().containsRedefinedClass(realClass)) {
         ClassReader classReader = new ClassFile(realClass, true).getReader();
         ClassWriter modifier = createModifier(realClass.getClassLoader(), classReader, baseTypeDesc);
         classReader.accept(modifier, false);
         byte[] modifiedClass = modifier.toByteArray();

         new RedefinitionEngine(realClass).redefineMethods(null, modifiedClass, true);
      }
   }

   private void createCaptureTransformer(CapturedType captureMetadata, boolean forTestClass)
   {
      CaptureTransformer transformer = new CaptureTransformer(captureMetadata, this, forTestClass);
      Startup.instrumentation().addTransformer(transformer);
      captureTransformers.add(transformer);
   }

   public void cleanUp()
   {
      for (CaptureTransformer transformer : captureTransformers) {
         transformer.deactivate();
         Startup.instrumentation().removeTransformer(transformer);
      }

      captureTransformers.clear();
   }
}
