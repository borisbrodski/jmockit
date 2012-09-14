/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.util.*;

import mockit.external.asm4.*;
import mockit.internal.*;

import static mockit.external.asm4.Opcodes.*;

public final class InterfaceImplementationGenerator extends BaseClassModifier
{
   private final List<String> implementedMethods;
   private final String implementationClassName;
   private String[] initialSuperInterfaces;

   public InterfaceImplementationGenerator(ClassReader classReader, String implementationClassName)
   {
      super(classReader);
      implementedMethods = new ArrayList<String>();
      this.implementationClassName = implementationClassName.replace('.', '/');
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      initialSuperInterfaces = interfaces;

      String[] implementedInterfaces = {name};

      super.visit(
         version, ACC_PUBLIC + ACC_FINAL, implementationClassName, signature, superName, implementedInterfaces);

      generateDefaultConstructor();
   }

   private void generateDefaultConstructor()
   {
      mw = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mw.visitVarInsn(ALOAD, 0);
      mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      generateEmptyImplementation();
   }

   @Override
   public void visitInnerClass(String name, String outerName, String innerName, int access) {}

   @Override
   public void visitOuterClass(String owner, String name, String desc) {}

   @Override
   public void visitAttribute(Attribute attr) {}

   @Override
   public void visitSource(String file, String debug) {}

   @Override
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) { return null; }

   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if (name.charAt(0) != '<') { // ignores an eventual "<clinit>" class initialization "method"
         generateMethodImplementation(name, desc, signature, exceptions);
      }

      return null;
   }

   private void generateMethodImplementation(String name, String desc, String signature, String[] exceptions)
   {
      String methodNameAndDesc = name + desc;

      if (!implementedMethods.contains(methodNameAndDesc)) {
         mw = cv.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
         generateEmptyImplementation(desc);
         implementedMethods.add(methodNameAndDesc);
      }
   }

   @Override
   public void visitEnd()
   {
      for (String superInterface : initialSuperInterfaces) {
         new MethodGeneratorForImplementedSuperInterface(superInterface);
      }
   }

   private final class MethodGeneratorForImplementedSuperInterface extends ClassVisitor
   {
      String[] superInterfaces;

      MethodGeneratorForImplementedSuperInterface(String interfaceName)
      {
         ClassFile.visitClass(interfaceName, this);
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         superInterfaces = interfaces;
      }

      @Override
      public FieldVisitor visitField(
         int access, String name, String desc, String signature, Object value) { return null; }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
      {
         generateMethodImplementation(name, desc, signature, exceptions);
         return null;
      }

      @Override
      public void visitEnd()
      {
         for (String superInterface : superInterfaces) {
            new MethodGeneratorForImplementedSuperInterface(superInterface);
         }
      }
   }
}
