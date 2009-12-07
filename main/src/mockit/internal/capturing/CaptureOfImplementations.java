/*
 * JMockit
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
package mockit.internal.capturing;

import java.util.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.startup.*;

public abstract class CaptureOfImplementations implements ModifierFactory
{
   private final List<CaptureTransformer> captureTransformers = new ArrayList<CaptureTransformer>();

   protected CaptureOfImplementations() {}

   public final void makeSureAllSubtypesAreModified(Class<?> baseType, Capturing capturing)
   {
      String baseTypeDesc = baseType == null ? null : baseType.getName().replace('.', '/');
      CapturedType captureMetadata = new CapturedType(baseType, capturing);
      Class<?>[] classesLoaded =
         Startup.instrumentation().getInitiatedClasses(getClass().getClassLoader());

      for (Class<?> aClass : classesLoaded) {
         if (captureMetadata.isToBeCaptured(aClass)) {
            redefineClass(aClass, baseTypeDesc);
         }
      }

      createCaptureTransformer(captureMetadata);
   }

   private void redefineClass(Class<?> realClass, String baseTypeDesc)
   {
      // TODO: a mocked field/parameter type will be redefined twice when it could be redefined
      // once, already considering capture in the first redefinition; optimize the second one away
      ClassReader classReader = new ClassFile(realClass, true).getReader();
      ClassWriter modifier = createModifier(realClass.getClassLoader(), classReader, baseTypeDesc);
      classReader.accept(modifier, false);
      byte[] modifiedClass = modifier.toByteArray();

      new RedefinitionEngine(realClass).redefineMethods(null, modifiedClass, true);
   }

   private void createCaptureTransformer(CapturedType captureMetadata)
   {
      CaptureTransformer transformer = new CaptureTransformer(captureMetadata, this);
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
