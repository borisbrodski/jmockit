/*
 * JMockit Annotations
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
package mockit.internal.annotations;

import java.lang.reflect.*;

import org.objectweb.asm2.*;
import static org.objectweb.asm2.Opcodes.*;
import org.objectweb.asm2.Type;

import mockit.internal.*;
import mockit.internal.core.*;
import mockit.internal.filtering.*;

public final class AnnotationsModifier extends RealClassModifier
{
   private static final int IGNORED_ACCESS = Modifier.ABSTRACT + Modifier.NATIVE;
   private static final String CLASS_WITH_STATE = "mockit/internal/state/TestRun";

   private final AnnotatedMockMethods annotatedMocks;
   private final MockingConfiguration mockingCfg;

   private boolean useMockingBridgeForUpdatingMockState;
   private boolean mockIsReentrant;
   private Type mockClassType;

   public AnnotationsModifier(
      ClassReader cr, Class<?> realClass, Object mock, AnnotatedMockMethods mockMethods,
      String[] stubbingFilters, boolean filtersNotInverted, boolean forStartupMock)
   {
      this(
         cr, getItFieldDescriptor(realClass), mock, mockMethods,
         stubbingFilters, filtersNotInverted, forStartupMock);

      setUseMockingBridge(realClass.getClassLoader());
      useMockingBridgeForUpdatingMockState = useMockingBridge;

      if (
         !useMockingBridge && mock != null && mock.getClass().isAnonymousClass() && 
         realClass.getPackage() != mock.getClass().getPackage()
      ) {
         useMockingBridge = true;
      }
   }

   public AnnotationsModifier(
      ClassReader cr, String realClassDescriptor, Object mock, AnnotatedMockMethods mockMethods,
      String[] stubbingFilters, boolean filtersNotInverted, boolean forStartupMock)
   {
      super(cr, realClassDescriptor, mock, mockMethods, forStartupMock);
      annotatedMocks = mockMethods;
      mockingCfg =
         stubbingFilters == null || stubbingFilters.length == 0 ?
            null : new MockingConfiguration(stubbingFilters, filtersNotInverted);
   }

   public void useOneMockInstancePerMockedInstance(Class<?> mockClass)
   {
      mockClassType = Type.getType(mockClass);
   }

   @Override
   public void visit(
      int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access, name, signature, superName, interfaces);

      if (mockingCfg != null) {
         mockingCfg.setSuperClassName(superName);
      }
   }

   @Override
   protected boolean shouldCopyOriginalMethodBytecode(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      // TODO: shouldn't stub out native methods when running under JDK 1.6?
      if (
         (access & IGNORED_ACCESS) == 0 && mockingCfg != null &&
         mockingCfg.matchesFilters(name, desc)
      ) {
         mockName = name;
         startModifiedMethodVersion(access, name, desc, signature, exceptions);
         generateEmptyStubImplementation(desc);
         return false;
      }

      return true;
   }

   private void generateEmptyStubImplementation(String desc)
   {
      if ("<init>".equals(mockName)) {
         generateCallToSuper();
      }

      generateEmptyImplementation(desc);
   }

   @Override
   protected MethodVisitor getAlternativeMethodWriter(int access, String desc)
   {
      mockIsReentrant = annotatedMocks.isReentrant();

      if (!mockIsReentrant) {
         return null;
      }

      if (Modifier.isNative(access)) {
         throw new IllegalArgumentException(
            "Reentrant mocks for native methods are not supported: \"" + mockName + '\"');
      }

      generateCallToMock(access, desc);
      return new MethodAdapter(mw);
   }

   @Override
   protected void obtainMockInstanceForInvocation(int access, String mockClassName)
   {
      if (mockClassType == null || Modifier.isStatic(access)) {
         super.obtainMockInstanceForInvocation(access, mockClassName);
      }
      else {
         generateGetMockCallWithMockClassAndMockedInstance();
      }
   }

   private void generateGetMockCallWithMockClassAndMockedInstance()
   {
      mw.visitLdcInsn(mockClassType);
      mw.visitVarInsn(ALOAD, 0); // loads "this" onto the operand stack
      mw.visitMethodInsn(
         INVOKESTATIC, "mockit/internal/state/TestRun", "getMock",
         "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;");
      mw.visitTypeInsn(CHECKCAST, getMockClassInternalName());
   }

   @Override
   protected void generateCallToMock(int access, String desc)
   {
      Label afterCallToMock = generateCallToUpdateMockStateIfAny();
      Label l1 = null;
      Label l2 = null;
      Label l3 = null;

      if (afterCallToMock != null) {
         Label l0 = new Label();
         l1 = new Label();
         l2 = new Label();
         mw.visitTryCatchBlock(l0, l1, l2, null);
         l3 = new Label();
         mw.visitTryCatchBlock(l2, l3, l2, null);
         mw.visitLabel(l0);
      }

      super.generateCallToMock(access, desc);

      if (afterCallToMock != null) {
         mw.visitLabel(l1);
         generateCallToExitReentrantMock();
         generateMethodReturn(desc);
         mw.visitLabel(l2);
         mw.visitVarInsn(ASTORE, varIndex);
         mw.visitLabel(l3);
         generateCallToExitReentrantMock();
         mw.visitVarInsn(ALOAD, varIndex);
         mw.visitInsn(ATHROW);

         mw.visitLabel(afterCallToMock);
      }
   }

   private Label generateCallToUpdateMockStateIfAny()
   {
      int mockStateIndex = annotatedMocks.getIndexForMockExpectations();
      Label afterCallToMock = null;

      if (mockStateIndex >= 0) {
         String mockClassDesc = annotatedMocks.getMockClassInternalName();

         if (useMockingBridgeForUpdatingMockState) {
            generateCallToMockingBridge(
               MockingBridge.Target.UPDATE_MOCK_STATE, mockClassDesc, ACC_STATIC, null, null,
               mockStateIndex);
            mw.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
         }
         else {
            mw.visitLdcInsn(mockClassDesc);
            mw.visitIntInsn(SIPUSH, mockStateIndex);
            mw.visitMethodInsn(
               INVOKESTATIC, CLASS_WITH_STATE, "updateMockState", "(Ljava/lang/String;I)Z");
         }

         if (mockIsReentrant) {
            afterCallToMock = new Label();
            mw.visitJumpInsn(IFEQ, afterCallToMock);
         }
      }

      return afterCallToMock;
   }

   private void generateCallToExitReentrantMock()
   {
      String mockClassDesc = annotatedMocks.getMockClassInternalName();
      int mockStateIndex = annotatedMocks.getIndexForMockExpectations();

      if (useMockingBridgeForUpdatingMockState) {
         generateCallToMockingBridge(
            MockingBridge.Target.EXIT_REENTRANT_MOCK, mockClassDesc, ACC_STATIC, null, null,
            mockStateIndex);
         mw.visitInsn(POP);
      }
      else {
         mw.visitLdcInsn(mockClassDesc);
         mw.visitIntInsn(SIPUSH, mockStateIndex);
         mw.visitMethodInsn(
            INVOKESTATIC, CLASS_WITH_STATE, "exitReentrantMock", "(Ljava/lang/String;I)V");
      }
   }
}
