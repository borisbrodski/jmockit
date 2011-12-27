/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import java.util.*;

import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;

public abstract class CaptureOfImplementations
{
   private final List<CaptureTransformer> captureTransformers;

   protected CaptureOfImplementations() { captureTransformers = new ArrayList<CaptureTransformer>(); }

   protected abstract ClassVisitor createModifier(ClassLoader cl, ClassReader cr, String capturedTypeDesc);

   public final void makeSureAllSubtypesAreModified(MockedType typeMetadata)
   {
      Class<?> baseType = typeMetadata.getClassType();

      if (baseType == null) {
         throw new IllegalArgumentException("Capturing implementations of multiple base types is not supported");
      }

      String baseTypeDesc = baseType.getName().replace('.', '/');
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
         ClassReader classReader = new ClassFile(realClass, false).getReader();
         ClassVisitor modifier = createModifier(realClass.getClassLoader(), classReader, baseTypeDesc);
         classReader.accept(modifier, 0);
         byte[] modifiedClass = modifier.toByteArray();

         new RedefinitionEngine(realClass).redefineMethods(null, modifiedClass);
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
