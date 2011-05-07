/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.Method;
import java.lang.reflect.*;
import java.util.*;

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.util.*;

final class SubclassGenerationModifier extends BaseClassModifier
{
   private static final int CLASS_ACCESS_MASK = 0xFFFF - ACC_ABSTRACT;

   private final MockingConfiguration mockingCfg;
   private final Class<?> abstractClass;
   private final String subclassName;
   private String superClassName;
   private String superClassOfSuperClass;
   private String[] initialSuperInterfaces;
   private final List<String> implementedMethods;

   SubclassGenerationModifier(
      MockingConfiguration mockingConfiguration, Class<?> abstractClass, ClassReader classReader, String subclassName)
   {
      super(classReader);
      mockingCfg = mockingConfiguration;
      this.abstractClass = abstractClass;
      this.subclassName = subclassName.replace('.', '/');
      implementedMethods = new ArrayList<String>();
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access & CLASS_ACCESS_MASK, subclassName, signature, name, null);
      superClassName = name;
      superClassOfSuperClass = superName;
      initialSuperInterfaces = interfaces;
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
      if (!"<init>".equals(name)) {
         // Inherits from super-class when non-abstract.
         // Otherwise, creates implementation for abstract method with call to "recordOrReplay".
         generateImplementationIfAbstractMethod(superClassName, access, name, desc, signature, exceptions);
      }

      return null;
   }

   private void generateImplementationIfAbstractMethod(
      String className, int access, String name, String desc, String signature, String[] exceptions)
   {
      String methodNameAndDesc = name + desc;

      if (!implementedMethods.contains(methodNameAndDesc)) {
         if (Modifier.isAbstract(access)) {
            generateMethodImplementation(className, access, name, desc, signature, exceptions);
         }

         implementedMethods.add(methodNameAndDesc);
      }
   }

   private void generateMethodImplementation(
      String className, int access, String name, String desc, String signature, String[] exceptions)
   {
      mw = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);

      boolean noFiltersToMatch = mockingCfg == null;

      if (
         noFiltersToMatch && !isMethodFromObject(name, desc) ||
         !noFiltersToMatch && mockingCfg.matchesFilters(name, desc)
      ) {
         generateDirectCallToHandler(className, access, name, desc, signature, exceptions, 0);
         generateReturnWithObjectAtTopOfTheStack(desc);
         mw.visitMaxs(1, 0);
      }
      else {
         generateEmptyImplementation(desc);
      }
   }

   @Override
   public void visitEnd()
   {
      generateImplementationsForInheritedAbstractMethods(superClassOfSuperClass);

      for (String superInterface : initialSuperInterfaces) {
         generateImplementationsForInterfaceMethods(superInterface);
      }
   }

   private void generateImplementationsForInheritedAbstractMethods(String superName)
   {
      if (!"java/lang/Object".equals(superName)) {
         new MethodModifierForSuperclass(superName);
      }
   }

   private void generateImplementationsForInterfaceMethods(String superName)
   {
      if (!"java/lang/Object".equals(superName)) {
         new MethodModifierForImplementedInterface(superName);
      }
   }

   private class BaseMethodModifier extends EmptyVisitor
   {
      final String typeName;

      BaseMethodModifier(String typeName)
      {
         this.typeName = typeName;
         ClassFile.visitClass(typeName, this);
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
      {
         return null;
      }
   }

   private final class MethodModifierForSuperclass extends BaseMethodModifier
   {
      String superName;

      MethodModifierForSuperclass(String className) { super(className); }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         this.superName = superName;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
      {
         generateImplementationIfAbstractMethod(typeName, access, name, desc, signature, exceptions);
         return null;
      }

      @Override
      public void visitEnd()
      {
         generateImplementationsForInheritedAbstractMethods(superName);
      }
   }

   private final class MethodModifierForImplementedInterface extends BaseMethodModifier
   {
      String[] superInterfaces;

      MethodModifierForImplementedInterface(String interfaceName) { super(interfaceName); }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
      {
         superInterfaces = interfaces;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
      {
         generateImplementationForInterfaceMethodIfMissing(access, name, desc, signature, exceptions);
         return null;
      }

      private void generateImplementationForInterfaceMethodIfMissing(
         int access, String name, String desc, String signature, String[] exceptions)
      {
         String methodNameAndDesc = name + desc;

         if (!implementedMethods.contains(methodNameAndDesc)) {
            if (!hasMethodImplementation(name, desc)) {
               generateMethodImplementation(typeName, access, name, desc, signature, exceptions);
            }

            implementedMethods.add(methodNameAndDesc);
         }
      }

      private boolean hasMethodImplementation(String name, String desc)
      {
         Class<?>[] paramTypes = Utilities.getParameterTypes(desc);

         try {
            Method method = abstractClass.getMethod(name, paramTypes);
            return !method.getDeclaringClass().isInterface();
         }
         catch (NoSuchMethodException ignore) {
            return false;
         }
      }

      @Override
      public void visitEnd()
      {
         for (String superName : superInterfaces) {
            generateImplementationsForInterfaceMethods(superName);
         }
      }
   }
}
