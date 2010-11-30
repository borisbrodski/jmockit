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

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;

final class InterfaceImplementationGenerator extends BaseClassModifier
{
   private final List<String> implementedMethods = new ArrayList<String>();
   private final String implementationClassName;
   private String interfaceName;
   private String[] initialSuperInterfaces;

   InterfaceImplementationGenerator(ClassReader classReader, String implementationClassName)
   {
      super(classReader);
      this.implementationClassName = implementationClassName.replace('.', '/');
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      interfaceName = name;
      initialSuperInterfaces = interfaces;

      String[] implementedInterfaces = {name};

      super.visit(
         version, ACC_PUBLIC + ACC_FINAL, implementationClassName, signature, superName, implementedInterfaces);

      generateConstructor();
   }

   private void generateConstructor()
   {
      mw = super.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
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
      generateMethodImplementation(access, name, desc, signature, exceptions);
      return null;
   }

   private void generateMethodImplementation(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      String methodNameAndDesc = name + desc;

      if (!implementedMethods.contains(methodNameAndDesc)) {
         mw = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
         generateDirectCallToHandler(interfaceName, access, name, desc, 0);
         generateReturnWithObjectAtTopOfTheStack(desc);
         mw.visitMaxs(1, 0);

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

   private final class MethodGeneratorForImplementedSuperInterface extends EmptyVisitor
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
         generateMethodImplementation(access, name, desc, signature, exceptions);
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
