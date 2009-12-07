/*
 * JMockit Expectations
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
package mockit.internal.expectations.mocking;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;
import mockit.internal.filtering.*;

import static mockit.external.asm.Opcodes.*;

final class SubclassGenerationModifier extends BaseClassModifier
{
   private static final int CLASS_ACCESS_MASK = 0xFFFF - ACC_ABSTRACT;

   private final MockingConfiguration mockingConfiguration;
   private final String subclassName;
   private String superclassName;
   private String superClassOfSuperClass;
   private final MockConstructorInfo mockConstructorInfo;
   private boolean defaultConstructorCreated;
   private final List<String> implementedMethods = new ArrayList<String>();

   SubclassGenerationModifier(
      MockConstructorInfo mockConstructorInfo, MockingConfiguration mockingConfiguration,
      ClassReader classReader, String subclassName)
   {
      super(classReader);
      this.mockingConfiguration = mockingConfiguration;
      this.subclassName = subclassName.replace('.', '/');
      this.mockConstructorInfo = mockConstructorInfo;
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access & CLASS_ACCESS_MASK, subclassName, signature, name, null);
      superclassName = name;
      superClassOfSuperClass = superName;
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
   public FieldVisitor visitField(
      int access, String name, String desc, String signature, Object value) { return null; }

   @Override
   public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if ("<init>".equals(name)) {
         if (!defaultConstructorCreated) {
            generateConstructor(desc);
            defaultConstructorCreated = true;
         }
      }
      else {
         // Inherits from super-class when non-abstract.
         // Otherwise, creates implementation for abstract method with call to "recordOrReplay".
         generateImplementationIfAbstractMethod(
            superclassName, access, name, desc, signature, exceptions);
      }

      return null;
   }

   private void generateConstructor(String candidateSuperConstructor)
   {
      String newConstructorDesc =
         mockConstructorInfo.isWithSuperConstructor() ? "([Ljava/lang/Object;)V" : "()V";

      mw = super.visitMethod(ACC_PUBLIC, "<init>", newConstructorDesc, null, null);
      mw.visitVarInsn(ALOAD, 0);
      String superConstructorDesc = generateSuperConstructorArguments(candidateSuperConstructor);
      mw.visitMethodInsn(INVOKESPECIAL, superclassName, "<init>", superConstructorDesc);
      mw.visitInsn(RETURN);
      mw.visitMaxs(1, 0);
   }

   private String generateSuperConstructorArguments(String superConstructorDesc)
   {
      if (!mockConstructorInfo.isWithSuperConstructor()) {
         pushDefaultValuesForParameterTypes(superConstructorDesc);
         return superConstructorDesc;
      }

      mw.visitVarInsn(ALOAD, 1);

      GeneratorAdapter generator = new GeneratorAdapter(mw, ACC_PUBLIC, "<init>", "()V");
      mockit.external.asm.Type[] paramTypes = mockConstructorInfo.getParameterTypesForSuperConstructor();
      int paramIndex = 0;

      for (mockit.external.asm.Type paramType : paramTypes) {
         generator.push(paramIndex++);
         generator.arrayLoad(paramType);
         generator.unbox(paramType);
      }

      return mockit.external.asm.Type.getMethodDescriptor(mockit.external.asm.Type.VOID_TYPE, paramTypes);
   }

   private void generateImplementationIfAbstractMethod(
      String className, int access, String name, String desc, String signature, String[] exceptions)
   {
      String methodNameAndDesc = name + desc;

      if (!implementedMethods.contains(methodNameAndDesc)) {
         if (Modifier.isAbstract(access)) {
            mw = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);

            if (mockingConfiguration.matchesFilters(name, desc)) {
               generateDirectCallToRecordOrReplay(className, access, name, desc);
               generateReturnWithObjectAtTopOfTheStack(desc);
               mw.visitMaxs(1, 0);
            }
            else {
               generateEmptyImplementation(desc);
            }
         }

         implementedMethods.add(methodNameAndDesc);
      }
   }

   @Override
   public void visitEnd()
   {
      generateImplementationsForInheritedAbstractMethods(superClassOfSuperClass);
   }

   private void generateImplementationsForInheritedAbstractMethods(String superClassName)
   {
      if (!"java/lang/Object".equals(superClassName)) {
         new MethodModifierForSuperclass(superClassName);
      }
   }

   private final class MethodModifierForSuperclass extends EmptyVisitor
   {
      private final String className;
      
      MethodModifierForSuperclass(String className)
      {
         this.className = className;

         ClassReader cr;

         try {
            cr = new ClassReader(ClassLoader.getSystemResourceAsStream(className + ".class"));
         }
         catch (IOException e) {
            throw new RuntimeException(e);
         }

         cr.accept(this, true);
      }

      @Override
      public void visit(
         int version, int access, String name, String signature, String superName,
         String[] interfaces)
      {
         generateImplementationsForInheritedAbstractMethods(superName);
      }

      @Override
      public FieldVisitor visitField(
         int access, String name, String desc, String signature, Object value) { return null; }

      @Override
      public MethodVisitor visitMethod(
         int access, String name, String desc, String signature, String[] exceptions)
      {
         generateImplementationIfAbstractMethod(
            className, access, name, desc, signature, exceptions);
         return null;
      }
   }
}
