/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
