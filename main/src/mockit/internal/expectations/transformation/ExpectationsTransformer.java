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
package mockit.internal.expectations.transformation;

import java.lang.instrument.*;
import static java.lang.reflect.Modifier.*;
import java.security.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.state.*;
import mockit.internal.*;
import mockit.internal.util.*;
import mockit.*;

public final class ExpectationsTransformer implements ClassFileTransformer
{
   private final SuperClassAnalyser superClassAnalyser = new SuperClassAnalyser();
   private final List<String> baseSubclasses;

   public ExpectationsTransformer(Instrumentation instrumentation)
   {
      baseSubclasses = new ArrayList<String>();
      baseSubclasses.add("mockit/Expectations");
      baseSubclasses.add("mockit/NonStrictExpectations");
      baseSubclasses.add("mockit/Verifications");
      baseSubclasses.add("mockit/FullVerifications");
      baseSubclasses.add("mockit/VerificationsInOrder");
      baseSubclasses.add("mockit/FullVerificationsInOrder");

      Class<?>[] alreadyLoaded = instrumentation.getInitiatedClasses(getClass().getClassLoader());
      findOtherBaseSubclasses(alreadyLoaded);
      modifyFinalSubclasses(alreadyLoaded);
   }

   private void findOtherBaseSubclasses(Class<?>[] alreadyLoaded)
   {
      for (Class<?> aClass : alreadyLoaded) {
         if (!isFinalClass(aClass) && isExpectationsOrVerificationsSubclassFromUserCode(aClass)) {
            String classInternalName = aClass.getName().replace('.', '/');
            baseSubclasses.add(classInternalName);
         }
      }
   }

   private boolean isFinalClass(Class<?> aClass)
   {
      return isFinal(aClass.getModifiers()) || Utilities.isAnonymousClass(aClass);
   }

   private boolean isExpectationsOrVerificationsSubclassFromUserCode(Class<?> aClass)
   {
      return
         aClass != Expectations.class && aClass != NonStrictExpectations.class &&
         Expectations.class.isAssignableFrom(aClass) ||
         aClass != Verifications.class && aClass != FullVerifications.class &&
         aClass != VerificationsInOrder.class && aClass != FullVerificationsInOrder.class &&
         Verifications.class.isAssignableFrom(aClass);
   }

   private void modifyFinalSubclasses(Class<?>[] alreadyLoaded)
   {
      for (Class<?> aClass : alreadyLoaded) {
         if (isFinalClass(aClass) && isExpectationsOrVerificationsSubclassFromUserCode(aClass)) {
            ClassReader cr = ClassFile.createClassFileReader(aClass.getName());
            EndOfBlockModifier modifier = new EndOfBlockModifier(cr, true);

            try {
               cr.accept(modifier, false);
            }
            catch (VisitInterruptedException ignore) {
               continue;
            }

            byte[] modifiedClassfile = modifier.toByteArray();
            RedefinitionEngine.redefineMethods(new ClassDefinition(aClass, modifiedClassfile));
         }
      }
   }

   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
   {
      if (classBeingRedefined == null && TestRun.isRunningTestCode(protectionDomain)) {
         int p = className.lastIndexOf('$');

         if (p > 0) {
            boolean isAnonymousClass = Utilities.hasPositiveDigit(className, p);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter modifier = new EndOfBlockModifier(cr, isAnonymousClass);
            cr.accept(modifier, false);
            return modifier.toByteArray();
         }
      }

      return null;
   }

   private final class EndOfBlockModifier extends ClassWriter
   {
      final boolean isAnonymousClass;
      MethodVisitor mw;
      String classDesc;

      EndOfBlockModifier(ClassReader cr, boolean isAnonymousClass)
      {
         super(cr, true);
         this.isAnonymousClass = isAnonymousClass;
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         boolean superClassIsKnownInvocationsSubclass = baseSubclasses.contains(superName);

         if (isFinal(access) || isAnonymousClass) {
            if (superClassIsKnownInvocationsSubclass || superClassAnalyser.classExtendsInvocationsClass(superName)) {
               super.visit(version, access, name, signature, superName, interfaces);
               classDesc = name;
               return; // go on and modify the class
            }
         }
         else if (superClassIsKnownInvocationsSubclass) {
            baseSubclasses.add(name);
         }

         throw VisitInterruptedException.INSTANCE;
      }


      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
      {
         mw = super.visitMethod(access, name, desc, signature, exceptions);

         if ("<init>".equals(name)) {
            return new InvocationBlockModifier((MethodWriter) mw, classDesc);
         }

         return mw;
      }
   }

   private final class SuperClassAnalyser extends EmptyVisitor
   {
      private boolean classExtendsBaseSubclass;

      boolean classExtendsInvocationsClass(String classOfInterest)
      {
         if ("java/lang/Object".equals(classOfInterest)) {
            return false;
         }

         String className = classOfInterest.replace('/', '.');
         ClassReader cr = ClassFile.createClassFileReader(className);

         try { cr.accept(this, true); } catch (VisitInterruptedException ignore) {}

         return classExtendsBaseSubclass;
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         classExtendsBaseSubclass = baseSubclasses.contains(superName);

         if (!classExtendsBaseSubclass && !"java/lang/Object".equals(superName)) {
            classExtendsInvocationsClass(superName);
         }

         throw VisitInterruptedException.INSTANCE;
      }
   }
}
