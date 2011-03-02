/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;

import static mockit.external.asm.Opcodes.*;

import mockit.*;
import mockit.external.asm.*;
import mockit.external.asm.commons.*;
import mockit.internal.*;

/**
 * Responsible for collecting the signatures of all methods defined in a given mock class which are explicitly annotated
 * as {@link mockit.Mock mocks}.
 */
public final class AnnotatedMockMethodCollector extends EmptyVisitor
{
   private static final int INVALID_FIELD_ACCESSES = ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC;
   private static final int INVALID_METHOD_ACCESSES = ACC_BRIDGE + ACC_SYNTHETIC + ACC_ABSTRACT + ACC_NATIVE;

   private final AnnotatedMockMethods mockMethods;
   private boolean collectingFromSuperClass;
   private String enclosingClassDescriptor;

   public AnnotatedMockMethodCollector(AnnotatedMockMethods mockMethods)
   {
      this.mockMethods = mockMethods;
   }

   public void collectMockMethods(Class<?> mockClass)
   {
      Class<?> classToCollectMocksFrom = mockClass;

      do {
         ClassReader mcReader = ClassFile.createClassFileReader(classToCollectMocksFrom.getName());
         mcReader.accept(this, true);
         classToCollectMocksFrom = classToCollectMocksFrom.getSuperclass();
         collectingFromSuperClass = true;
      }
      while (classToCollectMocksFrom != Object.class);
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      if (!collectingFromSuperClass) {
         mockMethods.setMockClassInternalName(name);

         int p = name.lastIndexOf('$');

         if (p > 0) {
            enclosingClassDescriptor = "(L" + name.substring(0, p) + ";)V";
         }
      }
   }

   @Override
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
   {
      if ((access & INVALID_FIELD_ACCESSES) == 0 && "it".equals(name)) {
         mockMethods.setWithItField(true);
      }

      return null;
   }

   /**
    * Adds the method specified to the set of mock methods, representing it as <code>name + desc</code>, as long as it's
    * appropriate for such method to be a mock, as indicated by its access modifiers and by the presence of the
    * {@link Mock} annotation.
    *
    * @param signature generic signature for a Java 5 generic method, ignored since redefinition only needs to consider
    * the "erased" signature
    * @param exceptions zero or more thrown exceptions in the method "throws" clause, also ignored
    */
   @Override
   public MethodVisitor visitMethod(
      final int access, final String name, final String methodDesc, String signature, String[] exceptions)
   {
      if ((access & INVALID_METHOD_ACCESSES) != 0) {
         return null;
      }

      if (
         !collectingFromSuperClass && enclosingClassDescriptor != null &&
         "<init>".equals(name) && methodDesc.equals(enclosingClassDescriptor)
      ) {
         mockMethods.setInnerMockClass(true);
         enclosingClassDescriptor = null;
      }

      return new EmptyVisitor()
      {
         @Override
         public AnnotationVisitor visitAnnotation(String desc, boolean visible)
         {
            if ("Lmockit/Mock;".equals(desc)) {
               String nameAndDesc =
                  mockMethods.addMethod(collectingFromSuperClass, name, methodDesc, Modifier.isStatic(access));

               if (nameAndDesc != null) {
                  return new MockAnnotationVisitor(nameAndDesc);
               }
            }

            return this;
         }
      };
   }

   private final class MockAnnotationVisitor extends EmptyVisitor
   {
      private final String mockNameAndDesc;
      private MockState mockState;

      private MockAnnotationVisitor(String mockNameAndDesc)
      {
         this.mockNameAndDesc = mockNameAndDesc;
      }

      @Override
      public void visit(String name, Object value)
      {
         if ("invocations".equals(name)) {
            getMockState().expectedInvocations = (Integer) value;
         }
         else if ("minInvocations".equals(name)) {
            getMockState().minExpectedInvocations = (Integer) value;
         }
         else if ("maxInvocations".equals(name)) {
            getMockState().maxExpectedInvocations = (Integer) value;
         }
         else {
            boolean reentrant = (Boolean) value;

            if (reentrant) {
               getMockState().makeReentrant();
            }
         }
      }

      private MockState getMockState()
      {
         if (mockState == null) {
            mockState = new MockState(mockMethods.realClass, mockNameAndDesc);
         }

         return mockState;
      }

      @Override
      public void visitEnd()
      {
         if (mockState != null) {
            mockMethods.addMockState(mockState);
         }
      }
   }
}
