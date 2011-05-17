/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.annotations;

import java.lang.reflect.*;

import static mockit.external.asm.Opcodes.*;

import mockit.external.asm.*;
import mockit.external.asm.Type;
import mockit.internal.*;
import mockit.internal.filtering.*;
import mockit.internal.startup.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

/**
 * Responsible for generating all necessary bytecode in the redefined (real) class. Such code will
 * redirect calls made on "real" methods to equivalent calls on the corresponding "mock" methods.
 * The original code won't be executed by the running JVM until the class redefinition is undone.
 * <p/>
 * Methods in the real class which have no corresponding mock method are unaffected.
 * <p/>
 * Any fields (static or not) in the real class remain untouched.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class AnnotationsModifier extends BaseClassModifier
{
   private static final int IGNORED_ACCESS = Modifier.ABSTRACT + Modifier.NATIVE;
   private static final String CLASS_WITH_STATE = "mockit/internal/state/TestRun";

   private final String itFieldDesc;
   private final int mockInstanceIndex;
   private final boolean forStartupMock;
   private final AnnotatedMockMethods annotatedMocks;
   private final MockingConfiguration mockingCfg;

   private final boolean useMockingBridgeForUpdatingMockState;
   private boolean mockIsReentrant;
   private Type mockClassType;

   // Helper fields:
   private String realSuperClassName;
   private String mockName;
   private int varIndex;
   private boolean mockIsStatic;
   private String methodOrConstructorDesc;
   private int initialVar;

   /**
    * Initializes the modifier for a given real/mock class pair.
    * <p/>
    * If a mock instance is provided, it will receive calls for any instance methods defined in the mock class.
    * If not, a new instance will be created for such calls. In the first case, the mock instance will need to be
    * recovered by the modified bytecode inside the real method. To enable this, the given mock instance is added to the
    * end of a global list made available through {@link mockit.internal.state.TestRun#getMock(int)}.
    *
    * @param cr the class file reader for the real class
    * @param mock an instance of the mock class or null to create one
    * @param mockMethods contains the set of mock methods collected from the mock class; each mock method is identified
    * by a pair composed of "name" and "desc", where "name" is the method name, and "desc" is the JVM internal
    * description of the parameters; once the real class modification is complete this set will be empty, unless no
    * corresponding real method was found for any of its method identifiers
    *
    * @throws IllegalArgumentException if no mock instance is given but the mock class is an inner class, which cannot
    * be instantiated since the enclosing instance is not known
    */
   public AnnotationsModifier(
      ClassReader cr, Class<?> realClass, Object mock, AnnotatedMockMethods mockMethods,
      MockingConfiguration mockingConfiguration, boolean forStartupMock)
   {
      super(cr);

      itFieldDesc = getItFieldDescriptor(realClass);
      annotatedMocks = mockMethods;
      mockingCfg = mockingConfiguration;
      this.forStartupMock = forStartupMock;
      mockInstanceIndex = getMockInstanceIndex(mock);

      setUseMockingBridge(realClass.getClassLoader());
      useMockingBridgeForUpdatingMockState = useMockingBridge;

      if (
         !useMockingBridge && mock != null && Utilities.isAnonymousClass(mock.getClass()) &&
         realClass.getPackage() != mock.getClass().getPackage()
      ) {
         useMockingBridge = true;
      }
   }

   private String getItFieldDescriptor(Class<?> realClass)
   {
      if (Proxy.isProxyClass(realClass)) {
         //noinspection AssignmentToMethodParameter
         realClass = realClass.getInterfaces()[0];
      }

      return Type.getDescriptor(realClass);
   }

   private int getMockInstanceIndex(Object mock)
   {
      if (mock != null) {
         return TestRun.getMockClasses().getMocks(forStartupMock).addMock(mock);
      }
      else if (!annotatedMocks.isInnerMockClass()) {
         return -1;
      }

      throw new IllegalArgumentException(
         "An inner mock class cannot be instantiated without its enclosing instance; " +
         "you must either pass a mock instance, or make the class static");
   }

   public void useOneMockInstancePerMockedInstance(Class<?> mockClass)
   {
      mockClassType = Type.getType(mockClass);
   }

   @Override
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      super.visit(version, access, name, signature, superName, interfaces);
      realSuperClassName = superName;
   }

   /**
    * If the specified method has a mock definition, then generates bytecode to redirect calls made to it to the mock
    * method. If it has no mock, does nothing.
    *
    * @param access not relevant
    * @param name together with desc, used to identity the method in given set of mock methods
    * @param signature not relevant
    * @param exceptions not relevant
    *
    * @return null if the method was redefined, otherwise a MethodWriter that writes out the visited method code without
    * changes
    */
   @Override
   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
   {
      if ((access & ACC_SYNTHETIC) != 0) {
         return super.visitMethod(access, name, desc, signature, exceptions);
      }

      if (!hasMock(name, desc)) {
         return
            shouldCopyOriginalMethodBytecode(access, name, desc, signature, exceptions) ?
               super.visitMethod(access, name, desc, signature, exceptions) : null;
      }

      if ((access & ACC_NATIVE) != 0 && !Startup.isJava6OrLater()) {
         throw new IllegalArgumentException("Mocking of native methods not supported under JDK 1.5: \"" + name + '\"');
      }

      startModifiedMethodVersion(access, name, desc, signature, exceptions);

      MethodVisitor alternativeWriter = getAlternativeMethodWriter(access, desc);

      if (alternativeWriter == null) {
         // Uses the MethodWriter just created to produce bytecode that calls the mock method.
         generateCallsForMockExecution(access, desc);
         generateMethodReturn(desc);
         mw.visitMaxs(1, 0); // dummy values, real ones are calculated by ASM

         // All desired bytecode is written at this point, so returns null in order to avoid
         // appending the original method bytecode, which would happen if mv was returned.
         return null;
      }

      return alternativeWriter;
   }

   private boolean hasMock(String name, String desc)
   {
      if ("<init>".equals(name)) {
         mockName = "$init";
      }
      else if ("<clinit>".equals(name)) {
         mockName = "$clinit";
      }
      else {
         mockName = name;
      }

      boolean hasMock = annotatedMocks.containsMethod(mockName, desc);

      if (hasMock) {
         mockIsStatic = annotatedMocks.containsStaticMethod(mockName, desc);
      }

      return hasMock;
   }

   private boolean shouldCopyOriginalMethodBytecode(
      int access, String name, String desc, String signature, String[] exceptions)
   {
      if ((access & IGNORED_ACCESS) == 0 && mockingCfg != null && mockingCfg.matchesFilters(name, desc)) {
         startModifiedMethodVersion(access, name, desc, signature, exceptions);
         generateEmptyStubImplementation(name, desc);
         return false;
      }

      return true;
   }

   private void generateEmptyStubImplementation(String name, String desc)
   {
      if ("<init>".equals(name)) {
         generateCallToSuper();
      }

      generateEmptyImplementation(desc);
   }

   private MethodVisitor getAlternativeMethodWriter(int access, String desc)
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

      return new MethodAdapter(mw)
      {
         @Override
         public void visitLocalVariable(String name, String desc2, String signature, Label start, Label end, int index)
         {
            // Discards debug info with missing information, to avoid a ClassFormatError (happens with EMMA).
            if (end.position > 0) {
               mw.visitLocalVariable(name, desc2, signature, start, end, index);
            }
         }
      };
   }

   private void generateCallsForMockExecution(int access, String desc)
   {
      if ("$init".equals(mockName)) {
         generateCallToSuper();
      }

      generateCallToMock(access, desc);
   }

   private void generateCallToSuper()
   {
      mw.visitVarInsn(ALOAD, 0);

      String constructorDesc = SuperConstructorCollector.INSTANCE.findConstructor(realSuperClassName);
      pushDefaultValuesForParameterTypes(constructorDesc);

      mw.visitMethodInsn(INVOKESPECIAL, realSuperClassName, "<init>", constructorDesc);
   }

   private void generateMockObjectInstantiation()
   {
      mw.visitTypeInsn(NEW, annotatedMocks.getMockClassInternalName());
      mw.visitInsn(DUP);
   }

   private void generateCallToMock(int access, String desc)
   {
      Label afterCallToMock = generateCallToUpdateMockStateIfAny(access);
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

      generateCallToMockMethod(access, desc);

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

   private Label generateCallToUpdateMockStateIfAny(int access)
   {
      int mockStateIndex = annotatedMocks.getIndexForMockExpectations();
      Label afterCallToMock = null;

      if (mockStateIndex >= 0) {
         String mockClassDesc = annotatedMocks.getMockClassInternalName();

         if (useMockingBridgeForUpdatingMockState) {
            generateCallToMockingBridge(
               MockingBridge.UPDATE_MOCK_STATE, mockClassDesc, access, null, null, null, null, mockStateIndex);
            mw.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
         }
         else {
            mw.visitLdcInsn(mockClassDesc);
            mw.visitIntInsn(SIPUSH, mockStateIndex);
            mw.visitMethodInsn(INVOKESTATIC, CLASS_WITH_STATE, "updateMockState", "(Ljava/lang/String;I)Z");
         }

         if (mockIsReentrant) {
            afterCallToMock = new Label();
            mw.visitJumpInsn(IFEQ, afterCallToMock);
         }
      }

      return afterCallToMock;
   }

   private void generateCallToMockMethod(int access, String desc)
   {
      methodOrConstructorDesc = desc;

      if (mockIsStatic) {
         generateStaticMethodCall(access);
      }
      else {
         generateInstanceMethodCall(access);
      }
   }

   private void generateStaticMethodCall(int access)
   {
      String mockClassName = annotatedMocks.getMockClassInternalName();

      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.CALL_STATIC_MOCK, mockClassName, access, mockName, methodOrConstructorDesc, null, null, null);
      }
      else {
         initialVar = initialLocalVariableIndexForRealMethod(access);
         String desc = generateInvocationArgumentIfNeeded();
         generateMethodOrConstructorArguments();
         mw.visitMethodInsn(INVOKESTATIC, mockClassName, mockName, desc);
      }
   }

   private void generateInstanceMethodCall(int access)
   {
      if (useMockingBridge) {
         generateCallToMockingBridge(
            MockingBridge.CALL_INSTANCE_MOCK, annotatedMocks.getMockClassInternalName(), access,
            mockName, methodOrConstructorDesc, null, null, mockInstanceIndex);
         return;
      }

      if (mockInstanceIndex < 0) {
         // No mock instance available yet.
         obtainMockInstanceForInvocation(access);
      }
      else {
         // A mock instance is available, so retrieve it from the global list.
         generateGetMockCallWithMockInstanceIndex();
      }

      if ((access & ACC_STATIC) == 0 && annotatedMocks.isWithItField()) {
         generateItFieldSetting();
      }

      generateMockInstanceMethodInvocationWithRealMethodArgs(access);
   }

   private void obtainMockInstanceForInvocation(int access)
   {
      if (mockClassType == null || Modifier.isStatic(access)) {
         generateMockObjectInstantiation();
         mw.visitMethodInsn(INVOKESPECIAL, annotatedMocks.getMockClassInternalName(), "<init>", "()V");
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
      mw.visitTypeInsn(CHECKCAST, annotatedMocks.getMockClassInternalName());
   }

   private void generateGetMockCallWithMockInstanceIndex()
   {
      mw.visitIntInsn(SIPUSH, mockInstanceIndex);
      String methodName = forStartupMock ? "getStartupMock" : "getMock";
      mw.visitMethodInsn(INVOKESTATIC, "mockit/internal/state/TestRun", methodName, "(I)Ljava/lang/Object;");
      mw.visitTypeInsn(CHECKCAST, annotatedMocks.getMockClassInternalName());
   }

   private void generateItFieldSetting()
   {
      Type[] argTypes = Type.getArgumentTypes(methodOrConstructorDesc);
      int var = 1;

      for (Type argType : argTypes) {
         var += argType.getSize();
      }

      mw.visitVarInsn(ASTORE, var); // stores the mock instance into local variable
      mw.visitVarInsn(ALOAD, var);  // loads the mock instance onto the operand stack
      mw.visitVarInsn(ALOAD, 0); // loads "this" onto the operand stack
      mw.visitFieldInsn(PUTFIELD, annotatedMocks.getMockClassInternalName(), "it", itFieldDesc);
      mw.visitVarInsn(ALOAD, var);  // again loads the mock instance onto the stack
   }

   private void generateMockInstanceMethodInvocationWithRealMethodArgs(int access)
   {
      initialVar = initialLocalVariableIndexForRealMethod(access);
      String desc = generateInvocationArgumentIfNeeded();
      generateMethodOrConstructorArguments();
      mw.visitMethodInsn(INVOKEVIRTUAL, annotatedMocks.getMockClassInternalName(), mockName, desc);
   }

   private int initialLocalVariableIndexForRealMethod(int access)
   {
      return (access & ACC_STATIC) == 0 ? 1 : 0;
   }

   private String generateInvocationArgumentIfNeeded()
   {
      if (annotatedMocks.isWithInvocationParameter()) {
         mw.visitInsn(ACONST_NULL);
         return "(Lmockit/Invocation;" + methodOrConstructorDesc.substring(1);
      }

      return methodOrConstructorDesc;
   }

   private void generateMethodOrConstructorArguments()
   {
      Type[] argTypes = Type.getArgumentTypes(methodOrConstructorDesc);
      varIndex = initialVar;

      for (Type argType : argTypes) {
         int opcode = argType.getOpcode(ILOAD);
         mw.visitVarInsn(opcode, varIndex);
         varIndex += argType.getSize();
      }
   }

   private void generateMethodReturn(String desc)
   {
      if (useMockingBridge) {
         generateReturnWithObjectAtTopOfTheStack(desc);
      }
      else {
         Type returnType = Type.getReturnType(desc);
         mw.visitInsn(returnType.getOpcode(IRETURN));
      }
   }

   private void generateCallToExitReentrantMock()
   {
      String mockClassDesc = annotatedMocks.getMockClassInternalName();
      int mockStateIndex = annotatedMocks.getIndexForMockExpectations();

      if (useMockingBridgeForUpdatingMockState) {
         generateCallToMockingBridge(
            MockingBridge.EXIT_REENTRANT_MOCK, mockClassDesc, ACC_STATIC, null, null, null, null, mockStateIndex);
         mw.visitInsn(POP);
      }
      else {
         mw.visitLdcInsn(mockClassDesc);
         mw.visitIntInsn(SIPUSH, mockStateIndex);
         mw.visitMethodInsn(INVOKESTATIC, CLASS_WITH_STATE, "exitReentrantMock", "(Ljava/lang/String;I)V");
      }
   }
}
