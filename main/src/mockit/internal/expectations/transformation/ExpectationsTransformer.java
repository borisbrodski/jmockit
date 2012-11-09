/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import java.lang.instrument.*;
import java.security.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;

import mockit.*;
import mockit.external.asm4.*;
import mockit.internal.*;
import mockit.internal.startup.*;
import mockit.internal.util.*;

public final class ExpectationsTransformer implements ClassFileTransformer
{
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
      findAndModifyOtherBaseSubclasses(alreadyLoaded);
      modifyFinalSubclasses(alreadyLoaded);
   }

   private void findAndModifyOtherBaseSubclasses(Class<?>[] alreadyLoaded)
   {
      for (Class<?> aClass : alreadyLoaded) {
         if (!isFinalClass(aClass) && isExpectationsOrVerificationsSubclassFromUserCode(aClass)) {
            String classInternalName = Type.getInternalName(aClass);
            baseSubclasses.add(classInternalName);
            modifyInvocationsSubclass(aClass, false);
         }
      }
   }

   private boolean isFinalClass(Class<?> aClass)
   {
      return isFinal(aClass.getModifiers()) || ClassNaming.isAnonymousClass(aClass);
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
            modifyInvocationsSubclass(aClass, true);
         }
      }
   }

   private void modifyInvocationsSubclass(Class<?> aClass, boolean isFinalClass)
   {
      ClassReader cr = ClassFile.createClassFileReader(aClass);
      EndOfBlockModifier modifier = new EndOfBlockModifier(cr, aClass.getClassLoader(), isFinalClass);

      try {
         cr.accept(modifier, 0);
      }
      catch (VisitInterruptedException ignore) {
         return;
      }

      byte[] modifiedClassfile = modifier.toByteArray();
      Startup.redefineMethods(aClass, modifiedClassfile);
   }

   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
   {
      if (classBeingRedefined == null && protectionDomain != null) {
         ClassReader cr = new ClassReader(classfileBuffer);
         String superClassName = cr.getSuperName();

         if (
            !baseSubclasses.contains(superClassName) &&
            !superClassName.endsWith("Expectations") && !superClassName.endsWith("Verifications")
         ) {
            return null;
         }

         boolean isAnonymousClass = ClassNaming.isAnonymousClass(className);

         try {
            EndOfBlockModifier modifier = new EndOfBlockModifier(cr, loader, isAnonymousClass);
            cr.accept(modifier, 0);
            return modifier.toByteArray();
         }
         catch (VisitInterruptedException ignore) {}
         catch (Throwable e) { e.printStackTrace(); }
      }

      return null;
   }

   private final class EndOfBlockModifier extends ClassVisitor
   {
      private final ClassLoader loader;
      private boolean isFinalClass;
      private MethodVisitor mw;
      private String classDesc;

      EndOfBlockModifier(ClassReader cr, ClassLoader loader, boolean isFinalClass)
      {
         super(new ClassWriter(cr, ClassWriter.COMPUTE_MAXS));
         this.loader = loader;
         this.isFinalClass = isFinalClass;
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         if (isFinal(access)) {
            isFinalClass = true;
         }

         boolean superClassIsKnownInvocationsSubclass = baseSubclasses.contains(superName);
         boolean modifyTheClass = false;

         if (isFinalClass) {
            if (superClassIsKnownInvocationsSubclass) {
               modifyTheClass = true;
            }
            else {
               SuperClassAnalyser superClassAnalyser = new SuperClassAnalyser(loader);

               if (superClassAnalyser.classExtendsInvocationsClass(superName)) {
                  modifyTheClass = true;
               }
            }
         }
         else if (superClassIsKnownInvocationsSubclass) {
            baseSubclasses.add(name);
            modifyTheClass = true;
         }

         if (!modifyTheClass) {
            throw VisitInterruptedException.INSTANCE;
         }

         super.visit(version, access, name, signature, superName, interfaces);
         classDesc = name;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
      {
         mw = super.visitMethod(access, name, desc, signature, exceptions);
         return new InvocationBlockModifier(mw, classDesc, isFinalClass && "<init>".equals(name));
      }
   }

   private final class SuperClassAnalyser extends ClassVisitor
   {
      private final ClassLoader loader;
      private boolean classExtendsBaseSubclass;

      private SuperClassAnalyser(ClassLoader loader) { this.loader = loader; }

      boolean classExtendsInvocationsClass(String classOfInterest)
      {
         if ("java/lang/Object".equals(classOfInterest)) {
            return false;
         }

         ClassReader cr = ClassFile.createClassFileReader(loader, classOfInterest);

         try { cr.accept(this, ClassReader.SKIP_DEBUG); } catch (VisitInterruptedException ignore) {}

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
