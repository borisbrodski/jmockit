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

import java.lang.instrument.*;
import java.security.*;

import mockit.internal.*;
import mockit.internal.state.*;

import org.objectweb.asm2.*;
import org.objectweb.asm2.commons.*;

final class CaptureTransformer implements ClassFileTransformer
{
   private static final class VisitInterruptedException extends RuntimeException {}
   private static final VisitInterruptedException INTERRUPT_VISIT = new VisitInterruptedException();

   private final CapturedType metadata;
   private final String capturedType;
   private final ModifierFactory modifierFactory;
   private final SuperTypeCollector superTypeCollector;
   private boolean inactive;

   CaptureTransformer(CapturedType metadata, ModifierFactory modifierFactory)
   {
      this.metadata = metadata;
      capturedType = metadata.baseType.getName().replace('.', '/');
      this.modifierFactory = modifierFactory;
      superTypeCollector = new SuperTypeCollector();
   }

   void deactivate()
   {
      inactive = true;
   }

   public byte[] transform(
      ClassLoader loader, String internalClassName, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
   {
      if (inactive || classBeingRedefined != null || TestRun.getCurrentTestInstance() == null) {
         return null;
      }

      String className = internalClassName.replace('/', '.');

      if (!metadata.isToBeCaptured(className)) {
         return null;
      }

      ClassReader cr = new ClassReader(classfileBuffer);
      byte[] modifiedBytecode = null;

      try {
         cr.accept(superTypeCollector, true);
      }
      catch (VisitInterruptedException ignore) {
         if (superTypeCollector.classExtendsCapturedType) {
            modifiedBytecode = modifyAndRegisterClass(loader, className, cr);
         }
      }

      return modifiedBytecode;
   }

   private byte[] modifyAndRegisterClass(ClassLoader loader, String className, ClassReader cr)
   {
      ClassWriter modifier = modifierFactory.createModifier(loader, cr);
      cr.accept(modifier, false);

      TestRun.mockFixture().addTransformedClass(className, cr.b);

      return modifier.toByteArray();
   }

   private final class SuperTypeCollector extends EmptyVisitor
   {
      boolean classExtendsCapturedType;

      @Override
      public void visit(
         int version, int access, String name, String signature, String superName,
         String[] interfaces)
      {
         classExtendsCapturedType = false;

         if (capturedType.equals(superName)) {
            classExtendsCapturedType = true;
         }
         else {
            for (String itfc : interfaces) {
               if (capturedType.equals(itfc)) {
                  classExtendsCapturedType = true;
                  break;
               }
            }
         }

         if (!classExtendsCapturedType && !"java/lang/Object".equals(superName)) {
            String superClassName = superName.replace('/', '.');
            ClassReader cr = ClassFile.createClassFileReader(superClassName);
            cr.accept(superTypeCollector, true);
         }

         throw INTERRUPT_VISIT;
      }
   }
}
